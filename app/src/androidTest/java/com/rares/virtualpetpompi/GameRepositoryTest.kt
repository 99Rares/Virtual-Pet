package com.rares.virtualpetpompi

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rares.virtualpetpompi.data.BuyResult
import com.rares.virtualpetpompi.data.GameRepository
import com.rares.virtualpetpompi.data.GameStore
import com.rares.virtualpetpompi.data.PetDatabase
import com.rares.virtualpetpompi.domain.Difficulty
import com.rares.virtualpetpompi.domain.PetRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.ZoneId

/** Runs the real Room + DataStore stack against a throwaway database and a controllable clock. */
@RunWith(AndroidJUnit4::class)
class GameRepositoryTest {
    private lateinit var db: PetDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var file: File
    private lateinit var repo: GameRepository
    private var now = 1_800_000_000_000L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PetDatabase::class.java).build()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        file = File.createTempFile("game", ".preferences_pb", context.cacheDir).also { it.delete() }
        val store = GameStore(PreferenceDataStoreFactory.create(scope = scope) { file })
        repo = GameRepository(store, db, nowMillis = { now }, zone = ZoneId.of("UTC"))
    }

    @After
    fun tearDown() {
        scope.cancel()
        db.close()
        file.delete()
    }

    private suspend fun state() = repo.state.first()

    @Test
    fun stepsBecomeCoins() = runBlocking {
        repo.onboard(Difficulty.NORMAL)
        repo.recordCounter(50_000)       // baseline only
        repo.recordCounter(52_500)       // +2500 steps
        val s = state()
        assertEquals(2_500, s.todaySteps)
        assertEquals(5 + 25, s.coins)
    }

    @Test
    fun rebootCounterKeepsCountingForward() = runBlocking {
        repo.onboard(Difficulty.NORMAL)
        repo.recordCounter(9_000)
        repo.recordCounter(9_400)        // +400
        repo.recordCounter(120)          // phone rebooted: 120 new steps
        assertEquals(520, state().todaySteps)
    }

    @Test
    fun cannotBuyWithoutCoins() = runBlocking {
        repo.onboard(Difficulty.NORMAL)   // 5 starter coins
        assertEquals(BuyResult.NOT_ENOUGH_COINS, repo.buyFood("steak1"))   // costs 35
        assertEquals(5, state().coins)
        assertTrue(state().pantry.isEmpty())
    }

    @Test
    fun buyThenFeedRestoresHunger() = runBlocking {
        repo.onboard(Difficulty.NORMAL)
        assertEquals(BuyResult.OK, repo.buyFood("rice"))                    // 5 coins, +5 fullness
        assertEquals(0, state().coins)
        assertEquals(1, state().pantry["rice"])

        now += 10 * PetRules.MILLIS_PER_HUNGER_POINT                         // 10 points hungrier
        repo.settle()
        assertEquals(90, state().pet.hunger)

        assertTrue(repo.feed("rice"))
        assertEquals(95, state().pet.hunger)
        assertTrue(state().pantry.isEmpty())
        assertFalse("nothing left to eat", repo.feed("rice"))
    }

    @Test
    fun fullPetRefusesFoodAndKeepsIt() = runBlocking {
        repo.onboard(Difficulty.EASY)
        assertEquals(BuyResult.OK, repo.buyFood("cola"))
        assertFalse(repo.feed("cola"))
        assertEquals(1, state().pantry["cola"])
    }

    @Test
    fun starvingCostsLivesAndEventuallyKills() = runBlocking {
        repo.onboard(Difficulty.NORMAL)
        now += 100 * PetRules.MILLIS_PER_HUNGER_POINT
        repo.settle()
        assertEquals(4, state().pet.lives)
        now += 4 * 100 * PetRules.MILLIS_PER_HUNGER_POINT
        repo.settle()
        assertTrue(state().pet.isDead)
        assertFalse(repo.feed("rice"))
    }

    @Test
    fun roomsCanBeBoughtOnceAndSelected() = runBlocking {
        repo.onboard(Difficulty.EASY)                                        // 100 starter coins
        assertEquals(BuyResult.OK, repo.buyRoom("bg1"))                      // 45
        assertEquals("bg1", state().selectedRoom)
        assertEquals(55, state().coins)
        assertEquals(BuyResult.ALREADY_OWNED, repo.buyRoom("bg1"))
        repo.selectRoom("bg0")
        assertEquals("bg0", state().selectedRoom)
        repo.selectRoom("bg3")                                               // not owned: ignored
        assertEquals("bg0", state().selectedRoom)
    }

    @Test
    fun restartWipesTheGameButKeepsStepHistory() = runBlocking {
        repo.onboard(Difficulty.NORMAL)
        repo.recordCounter(1_000)
        repo.recordCounter(1_300)
        repo.buyFood("cirese")
        repo.restart()
        val s = state()
        assertFalse(s.isOnboarded)
        assertTrue(s.pantry.isEmpty())
        assertEquals(300, s.todaySteps)
    }
}
