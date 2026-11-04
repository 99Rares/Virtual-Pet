package com.rares.virtualpetpompi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rares.virtualpetpompi.domain.Catalog
import com.rares.virtualpetpompi.domain.Difficulty
import com.rares.virtualpetpompi.domain.PetState
import com.rares.virtualpetpompi.domain.Skins
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "game")

fun gameDataStore(context: Context): DataStore<Preferences> = context.applicationContext.gameDataStore

/** Snapshot of everything small that the game remembers. */
data class GameSnapshot(
    val difficulty: Difficulty?,
    val pet: PetState,
    /** Millis timestamp that hunger has been aged up to. */
    val hungerAnchor: Long,
    val stepsEarned: Long,
    val spentCoins: Int,
    /** Net coins won (positive) or lost (negative) in the mini-game. */
    val bonusCoins: Int,
    val gameWinsEpochDay: Long,
    val gameWinsToday: Int,
    val lastCounter: Long?,
    val ownedRooms: Set<String>,
    val selectedRoom: String,
    val ownedSkins: Set<String>,
    val selectedSkin: String,
    val dailyGoal: Int,
    val reminderSent: Boolean,
) {
    val isOnboarded: Boolean get() = difficulty != null
}

class GameStore(private val store: DataStore<Preferences>) {

    val snapshot: Flow<GameSnapshot> = store.data.map { it.toSnapshot() }

    suspend fun edit(block: (MutablePrefs) -> Unit) {
        store.edit { block(MutablePrefs(it)) }
    }

    suspend fun current(): GameSnapshot = store.data.map { it.toSnapshot() }.first()

    private fun Preferences.toSnapshot(): GameSnapshot {
        val difficulty = this[DIFFICULTY]?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
        return GameSnapshot(
            difficulty = difficulty,
            pet = PetState(
                hunger = this[HUNGER] ?: PetState.MAX_HUNGER,
                lives = this[LIVES] ?: (difficulty ?: Difficulty.NORMAL).startLives,
            ),
            hungerAnchor = this[HUNGER_ANCHOR] ?: 0L,
            stepsEarned = this[STEPS_EARNED] ?: 0L,
            spentCoins = this[SPENT_COINS] ?: 0,
            bonusCoins = this[BONUS_COINS] ?: 0,
            gameWinsEpochDay = this[GAME_WINS_DAY] ?: 0L,
            gameWinsToday = this[GAME_WINS_TODAY] ?: 0,
            lastCounter = this[LAST_COUNTER],
            ownedRooms = this[OWNED_ROOMS] ?: setOf(Catalog.DEFAULT_ROOM_ID),
            selectedRoom = this[SELECTED_ROOM] ?: Catalog.DEFAULT_ROOM_ID,
            ownedSkins = this[OWNED_SKINS] ?: setOf(Skins.DEFAULT_ID),
            selectedSkin = this[SELECTED_SKIN] ?: Skins.DEFAULT_ID,
            dailyGoal = this[DAILY_GOAL] ?: DEFAULT_GOAL,
            reminderSent = this[REMINDER_SENT] ?: false,
        )
    }

    /** Typed write access used inside [edit]. */
    class MutablePrefs(private val p: androidx.datastore.preferences.core.MutablePreferences) {
        fun setDifficulty(value: Difficulty) { p[DIFFICULTY] = value.name }
        fun setPet(pet: PetState, anchor: Long) {
            p[HUNGER] = pet.hunger
            p[LIVES] = pet.lives
            p[HUNGER_ANCHOR] = anchor
        }
        fun setStepsEarned(value: Long) { p[STEPS_EARNED] = value }
        fun setSpentCoins(value: Int) { p[SPENT_COINS] = value }
        fun setBonusCoins(value: Int) { p[BONUS_COINS] = value }
        fun setGameWins(epochDay: Long, wins: Int) {
            p[GAME_WINS_DAY] = epochDay
            p[GAME_WINS_TODAY] = wins
        }
        fun setLastCounter(value: Long) { p[LAST_COUNTER] = value }
        fun setOwnedRooms(value: Set<String>) { p[OWNED_ROOMS] = value }
        fun setSelectedRoom(value: String) { p[SELECTED_ROOM] = value }
        fun setOwnedSkins(value: Set<String>) { p[OWNED_SKINS] = value }
        fun setSelectedSkin(value: String) { p[SELECTED_SKIN] = value }
        fun setDailyGoal(value: Int) { p[DAILY_GOAL] = value }
        fun setReminderSent(value: Boolean) { p[REMINDER_SENT] = value }

        /** Forget the game but keep the step counter baseline, goal and total step history. */
        fun resetGame() {
            val keepCounter = p[LAST_COUNTER]
            val keepGoal = p[DAILY_GOAL]
            p.clear()
            keepCounter?.let { p[LAST_COUNTER] = it }
            keepGoal?.let { p[DAILY_GOAL] = it }
        }
    }

    companion object {
        const val DEFAULT_GOAL = 10_000
        private val DIFFICULTY = stringPreferencesKey("difficulty")
        private val HUNGER = intPreferencesKey("hunger")
        private val LIVES = intPreferencesKey("lives")
        private val HUNGER_ANCHOR = longPreferencesKey("hunger_anchor")
        private val STEPS_EARNED = longPreferencesKey("steps_earned")
        private val SPENT_COINS = intPreferencesKey("spent_coins")
        private val BONUS_COINS = intPreferencesKey("bonus_coins")
        private val GAME_WINS_DAY = longPreferencesKey("game_wins_day")
        private val GAME_WINS_TODAY = intPreferencesKey("game_wins_today")
        private val LAST_COUNTER = longPreferencesKey("last_counter")
        private val OWNED_ROOMS = stringSetPreferencesKey("owned_rooms")
        private val SELECTED_ROOM = stringPreferencesKey("selected_room")
        private val OWNED_SKINS = stringSetPreferencesKey("owned_skins")
        private val SELECTED_SKIN = stringPreferencesKey("selected_skin")
        private val DAILY_GOAL = intPreferencesKey("daily_goal")
        private val REMINDER_SENT = booleanPreferencesKey("reminder_sent")
    }
}
