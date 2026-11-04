package com.rares.virtualpetpompi.domain

import kotlin.math.max
import kotlin.math.min

/** Everything that defines Pompi's health. Pure data, no Android types. */
data class PetState(
    val hunger: Int,
    val lives: Int,
) {
    /** Pompi is dead once it has no lives left; it can only be revived by restarting the game. */
    val isDead: Boolean get() = lives <= 0

    companion object {
        const val MAX_HUNGER = 100
        fun fresh(difficulty: Difficulty) = PetState(MAX_HUNGER, difficulty.startLives)
    }
}

object PetRules {
    /** Hunger drops by one point every this many minutes. */
    const val MINUTES_PER_HUNGER_POINT = 20
    const val MILLIS_PER_HUNGER_POINT = MINUTES_PER_HUNGER_POINT * 60_000L

    /** Whole hunger points that elapsed in [elapsedMillis]. */
    fun pointsFor(elapsedMillis: Long): Int =
        (max(0L, elapsedMillis) / MILLIS_PER_HUNGER_POINT).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    /**
     * Ages the pet by [points] hunger points. Every time hunger reaches zero one life is spent
     * and hunger is refilled; when the last life goes the pet dies.
     */
    fun advance(state: PetState, points: Int): PetState {
        if (state.isDead || points <= 0) return state
        var hunger = state.hunger
        var lives = state.lives
        var remaining = points
        while (remaining > 0 && lives > 0) {
            if (hunger > remaining) {
                hunger -= remaining
                remaining = 0
            } else {
                remaining -= hunger
                lives--
                hunger = if (lives > 0) PetState.MAX_HUNGER else 0
            }
        }
        return PetState(hunger, lives)
    }

    /** A revived pet is full again with [Difficulty.reviveLives] lives. */
    fun revive(difficulty: Difficulty) = PetState(PetState.MAX_HUNGER, difficulty.reviveLives)

    fun feed(state: PetState, fullness: Int): PetState =
        if (state.isDead) state else state.copy(hunger = min(PetState.MAX_HUNGER, state.hunger + fullness))

    /** Hunger at or below this makes the pet look sad and triggers the reminder. */
    const val HUNGRY_THRESHOLD = 50
    const val REMINDER_THRESHOLD = 30
}

object CoinRules {
    /** Coins the player can spend right now. Never negative. */
    fun balance(stepsEarned: Long, difficulty: Difficulty, spentCoins: Int): Int {
        val earned = stepsEarned / difficulty.stepsPerCoin + difficulty.starterCoins
        return max(0L, earned - spentCoins).toInt()
    }
}

object StepRules {
    /**
     * Turns two readings of the cumulative step counter into new steps.
     * The sensor counts since boot, so a smaller value means the phone rebooted and the new
     * reading is itself the number of steps since then. No previous reading means no delta yet.
     */
    fun delta(lastCounter: Long?, counter: Long): Long = when {
        lastCounter == null -> 0L
        counter >= lastCounter -> counter - lastCounter
        else -> counter
    }
}
