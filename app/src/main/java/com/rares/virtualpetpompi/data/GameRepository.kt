package com.rares.virtualpetpompi.data

import com.rares.virtualpetpompi.domain.Catalog
import com.rares.virtualpetpompi.domain.CoinRules
import com.rares.virtualpetpompi.domain.Difficulty
import com.rares.virtualpetpompi.domain.PetRules
import com.rares.virtualpetpompi.domain.PetState
import com.rares.virtualpetpompi.domain.StepRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** What the UI needs to draw the game. */
data class GameState(
    val difficulty: Difficulty?,
    val pet: PetState,
    val coins: Int,
    val selectedRoom: String,
    val ownedRooms: Set<String>,
    val pantry: Map<String, Int>,
    val dailyGoal: Int,
    val todaySteps: Int,
) {
    val isOnboarded: Boolean get() = difficulty != null
}

/** Step statistics for the Progress screen. */
data class StepStats(
    val today: Int,
    val goal: Int,
    /** Last seven days, oldest first, including empty days. */
    val week: List<DayTotal>,
    val total: Long,
    val record: Int,
) {
    val weekTotal: Int get() = week.sumOf { it.steps }
    val weekAverage: Int get() = week.sumOf { it.steps } / week.size.coerceAtLeast(1)
}

data class DayTotal(val date: LocalDate, val steps: Int)

enum class BuyResult { OK, NOT_ENOUGH_COINS, ALREADY_OWNED }

class GameRepository(
    private val store: GameStore,
    private val db: PetDatabase,
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val zone: ZoneId = ZoneId.systemDefault(),
) {
    private val mutex = Mutex()

    private fun today(): LocalDate = Instant.ofEpochMilli(nowMillis()).atZone(zone).toLocalDate()

    /** Emits the current date now and again whenever midnight passes. */
    private val dateFlow: Flow<LocalDate> = flow {
        while (true) {
            val now = Instant.ofEpochMilli(nowMillis()).atZone(zone)
            emit(now.toLocalDate())
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            delay((nextMidnight - nowMillis()).coerceAtLeast(1_000))
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<GameState> = dateFlow.flatMapLatest { date ->
        combine(
            store.snapshot,
            db.pantry().observeAll(),
            db.stepDays().observeSince(date.toEpochDay()),
        ) { snap, pantry, days ->
            GameState(
                difficulty = snap.difficulty,
                pet = snap.pet,
                coins = snap.difficulty?.let { CoinRules.balance(snap.stepsEarned, it, snap.spentCoins) } ?: 0,
                selectedRoom = snap.selectedRoom,
                ownedRooms = snap.ownedRooms,
                pantry = pantry.associate { it.foodId to it.count },
                dailyGoal = snap.dailyGoal,
                todaySteps = days.firstOrNull { it.epochDay == date.toEpochDay() }?.steps ?: 0,
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val stats: Flow<StepStats> = dateFlow.flatMapLatest { date ->
        val from = date.minusDays(6)
        combine(
            db.stepDays().observeSince(from.toEpochDay()),
            db.stepDays().observeTotal(),
            db.stepDays().observeRecord(),
            store.snapshot,
        ) { days, total, record, snap ->
            val byDay = days.associate { it.epochDay to it.steps }
            val week = (0..6).map { offset ->
                val d = from.plusDays(offset.toLong())
                DayTotal(d, byDay[d.toEpochDay()] ?: 0)
            }
            StepStats(week.last().steps, snap.dailyGoal, week, total, record)
        }
    }

    suspend fun onboard(difficulty: Difficulty) = mutex.withLock {
        store.edit {
            it.setDifficulty(difficulty)
            it.setPet(PetState.fresh(difficulty), nowMillis())
            it.setStepsEarned(0)
            it.setSpentCoins(0)
        }
    }

    /** Ages the pet to the present moment. Cheap to call often. */
    suspend fun settle() = mutex.withLock { settleLocked() }

    private suspend fun settleLocked() {
        val snap = store.current()
        if (!snap.isOnboarded) return
        val now = nowMillis()
        val anchor = if (snap.hungerAnchor == 0L) now else snap.hungerAnchor
        val points = PetRules.pointsFor(now - anchor)
        if (points == 0) {
            if (snap.hungerAnchor == 0L) store.edit { it.setPet(snap.pet, anchor) }
            return
        }
        val aged = PetRules.advance(snap.pet, points)
        val newAnchor = if (aged.isDead) now else anchor + points * PetRules.MILLIS_PER_HUNGER_POINT
        store.edit { it.setPet(aged, newAnchor) }
    }

    /** Feeds a new step-counter reading into the step history and the coin total. */
    suspend fun recordCounter(counter: Long) = mutex.withLock {
        val snap = store.current()
        val delta = StepRules.delta(snap.lastCounter, counter)
        store.edit {
            it.setLastCounter(counter)
            if (delta > 0 && snap.isOnboarded) it.setStepsEarned(snap.stepsEarned + delta)
        }
        if (delta > 0) db.stepDays().addSteps(today().toEpochDay(), delta.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    }

    suspend fun buyFood(foodId: String): BuyResult = mutex.withLock {
        val food = Catalog.food(foodId) ?: return BuyResult.NOT_ENOUGH_COINS
        val snap = store.current()
        val difficulty = snap.difficulty ?: return BuyResult.NOT_ENOUGH_COINS
        if (CoinRules.balance(snap.stepsEarned, difficulty, snap.spentCoins) < food.price) {
            return BuyResult.NOT_ENOUGH_COINS
        }
        store.edit { it.setSpentCoins(snap.spentCoins + food.price) }
        db.pantry().add(foodId)
        BuyResult.OK
    }

    suspend fun buyRoom(roomId: String): BuyResult = mutex.withLock {
        val room = Catalog.rooms.firstOrNull { it.id == roomId } ?: return BuyResult.NOT_ENOUGH_COINS
        val snap = store.current()
        val difficulty = snap.difficulty ?: return BuyResult.NOT_ENOUGH_COINS
        if (room.id in snap.ownedRooms) return BuyResult.ALREADY_OWNED
        if (CoinRules.balance(snap.stepsEarned, difficulty, snap.spentCoins) < room.price) {
            return BuyResult.NOT_ENOUGH_COINS
        }
        store.edit {
            it.setSpentCoins(snap.spentCoins + room.price)
            it.setOwnedRooms(snap.ownedRooms + room.id)
            it.setSelectedRoom(room.id)
        }
        BuyResult.OK
    }

    suspend fun selectRoom(roomId: String) = mutex.withLock {
        val snap = store.current()
        if (roomId in snap.ownedRooms) store.edit { it.setSelectedRoom(roomId) }
    }

    /** Eats one portion of [foodId] from the pantry. Returns false if there was none. */
    suspend fun feed(foodId: String): Boolean = mutex.withLock {
        settleLocked()
        val food = Catalog.food(foodId) ?: return false
        val snap = store.current()
        if (snap.pet.isDead || snap.pet.hunger >= PetState.MAX_HUNGER) return false
        if (db.pantry().consume(foodId) == 0) return false
        val fed = PetRules.feed(snap.pet, food.fullness)
        store.edit {
            it.setPet(fed, if (snap.hungerAnchor == 0L) nowMillis() else snap.hungerAnchor)
            it.setReminderSent(false)
        }
        true
    }

    suspend fun setDailyGoal(goal: Int) = mutex.withLock {
        store.edit { it.setDailyGoal(goal.coerceIn(1_000, 50_000)) }
    }

    suspend fun setReminderSent(sent: Boolean) = mutex.withLock { store.edit { it.setReminderSent(sent) } }

    suspend fun currentSnapshot(): GameSnapshot = store.current()

    /** Wipes the game (coins, food, rooms, lives). Step history stays. */
    suspend fun restart() = mutex.withLock {
        store.edit { it.resetGame() }
        db.pantry().clear()
    }
}
