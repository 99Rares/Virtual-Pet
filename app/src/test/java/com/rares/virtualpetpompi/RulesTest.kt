package com.rares.virtualpetpompi

import com.rares.virtualpetpompi.domain.CoinRules
import com.rares.virtualpetpompi.domain.Difficulty
import com.rares.virtualpetpompi.domain.PetRules
import com.rares.virtualpetpompi.domain.PetState
import com.rares.virtualpetpompi.domain.StepRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StepRulesTest {
    @Test fun `first reading only sets a baseline`() = assertEquals(0L, StepRules.delta(null, 5_000))
    @Test fun `counter grows`() = assertEquals(120L, StepRules.delta(1_000, 1_120))
    @Test fun `no movement`() = assertEquals(0L, StepRules.delta(1_000, 1_000))
    @Test fun `reboot restarts the counter so the new reading is all new steps`() =
        assertEquals(35L, StepRules.delta(9_000, 35))
}

class CoinRulesTest {
    @Test fun `normal mode pays one coin per hundred steps plus starter coins`() =
        assertEquals(5 + 25, CoinRules.balance(2_500, Difficulty.NORMAL, 0))

    @Test fun `easy mode pays one coin per ten steps`() =
        assertEquals(100 + 25, CoinRules.balance(250, Difficulty.EASY, 0))

    @Test fun `spent coins are subtracted`() =
        assertEquals(20, CoinRules.balance(2_500, Difficulty.NORMAL, 10))

    @Test fun `balance never goes negative`() =
        assertEquals(0, CoinRules.balance(0, Difficulty.NORMAL, 999))

    @Test fun `partial coin does not count`() =
        assertEquals(5, CoinRules.balance(99, Difficulty.NORMAL, 0))
}

class PetRulesTest {
    private val full = PetState(hunger = 100, lives = 5)

    @Test fun `less than a point of time changes nothing`() =
        assertEquals(0, PetRules.pointsFor(19 * 60_000L))

    @Test fun `one point per twenty minutes`() =
        assertEquals(3, PetRules.pointsFor(61 * 60_000L))

    @Test fun `negative elapsed time is ignored`() = assertEquals(0, PetRules.pointsFor(-5_000))

    @Test fun `hunger drops without losing a life`() =
        assertEquals(PetState(70, 5), PetRules.advance(full, 30))

    @Test fun `reaching zero costs a life and refills`() =
        assertEquals(PetState(100, 4), PetRules.advance(full, 100))

    @Test fun `a long absence can cost several lives`() {
        // 250 points: two full tanks (200) then 50 more
        assertEquals(PetState(50, 3), PetRules.advance(full, 250))
    }

    @Test fun `last life lost kills the pet`() {
        val dead = PetRules.advance(PetState(10, 1), 10)
        assertEquals(PetState(0, 0), dead)
        assertTrue(dead.isDead)
    }

    @Test fun `a dead pet stays dead and cannot be fed`() {
        val dead = PetState(0, 0)
        assertEquals(dead, PetRules.advance(dead, 50))
        assertEquals(dead, PetRules.feed(dead, 30))
    }

    @Test fun `feeding is capped at the maximum`() =
        assertEquals(100, PetRules.feed(PetState(90, 5), 30).hunger)

    @Test fun `feeding adds fullness`() =
        assertEquals(60, PetRules.feed(PetState(30, 5), 30).hunger)

    @Test fun `fresh pet is full and alive`() {
        val pet = PetState.fresh(Difficulty.EASY)
        assertEquals(100, pet.hunger)
        assertEquals(1000, pet.lives)
        assertFalse(pet.isDead)
    }
}
