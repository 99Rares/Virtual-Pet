package com.rares.virtualpetpompi.domain

import kotlin.math.abs
import kotlin.random.Random

/**
 * Betting rules for the catch game. Winning is deliberately capped per day: otherwise a skilled
 * player could farm coins on the sofa and the app would stop rewarding walking.
 */
object MiniGameRules {
    val bets = listOf(5, 10, 20)

    /** Most coins of profit the game can pay out in one calendar day. Losses are unlimited. */
    const val DAILY_WIN_CAP = 30

    const val GOOD_CATCHES = 12
    const val GREAT_CATCHES = 20
    const val PERFECT_CATCHES = 30

    fun multiplier(score: Int): Float = when {
        score >= PERFECT_CATCHES -> 2f
        score >= GREAT_CATCHES -> 1.5f
        score >= GOOD_CATCHES -> 1f
        else -> 0f
    }

    /**
     * @property payout coins handed back after the round (the bet itself was taken at the start)
     * @property net payout minus bet: negative when the bet was lost
     * @property capped true when the daily win cap trimmed the profit
     */
    data class Result(val payout: Int, val net: Int, val capped: Boolean)

    fun settle(bet: Int, score: Int, winsToday: Int): Result {
        val raw = (bet * multiplier(score)).toInt()
        val profit = raw - bet
        if (profit <= 0) return Result(payout = raw, net = profit, capped = false)
        val room = (DAILY_WIN_CAP - winsToday).coerceAtLeast(0)
        val allowed = minOf(profit, room)
        return Result(payout = bet + allowed, net = allowed, capped = allowed < profit)
    }
}

enum class ItemKind { FOOD, SKULL }

/** A falling item. Coordinates are normalised: x across 0..1, y down 0..1. */
data class FallingItem(val kind: ItemKind, val variant: Int, val x: Float, val y: Float, val speed: Float)

/**
 * Catch the food, dodge the skulls. Pure simulation in normalised coordinates so the screen only
 * has to draw it; time is advanced explicitly with [step], which makes it deterministic to test.
 */
class CatchGame(
    private val random: Random = Random,
    val durationSeconds: Float = 30f,
    val startLives: Int = 3,
    private val variants: Int = 8,
) {
    var playerX = 0.5f
        private set
    var score = 0
        private set
    var lives = startLives
        private set
    var elapsed = 0f
        private set

    private val _items = mutableListOf<FallingItem>()
    val items: List<FallingItem> get() = _items
    private var untilNextSpawn = 0.6f

    val timeLeft: Float get() = (durationSeconds - elapsed).coerceAtLeast(0f)
    val isOver: Boolean get() = lives <= 0 || elapsed >= durationSeconds

    fun movePlayerTo(x: Float) {
        playerX = x.coerceIn(PLAYER_HALF_WIDTH, 1f - PLAYER_HALF_WIDTH)
    }

    fun step(dt: Float) {
        if (isOver) return
        elapsed += dt
        untilNextSpawn -= dt
        if (untilNextSpawn <= 0f) {
            spawn()
            // Spawns speed up from one every 0.9s to one every 0.45s.
            untilNextSpawn += 0.9f - 0.45f * (elapsed / durationSeconds).coerceIn(0f, 1f)
        }
        val iterator = _items.listIterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val moved = item.copy(y = item.y + item.speed * dt)
            val caught = moved.y in CATCH_TOP..CATCH_BOTTOM && abs(moved.x - playerX) <= PLAYER_HALF_WIDTH + ITEM_RADIUS * 0.5f
            when {
                caught -> {
                    iterator.remove()
                    if (item.kind == ItemKind.FOOD) score++ else lives--
                }
                moved.y > 1f + ITEM_RADIUS -> iterator.remove()
                else -> iterator.set(moved)
            }
            if (isOver) break
        }
    }

    private fun spawn() {
        val progress = (elapsed / durationSeconds).coerceIn(0f, 1f)
        val skullChance = 0.2f + 0.12f * progress
        val kind = if (random.nextFloat() < skullChance) ItemKind.SKULL else ItemKind.FOOD
        _items += FallingItem(
            kind = kind,
            variant = random.nextInt(variants),
            x = ITEM_RADIUS + random.nextFloat() * (1f - 2 * ITEM_RADIUS),
            y = -ITEM_RADIUS,
            speed = 0.32f + 0.22f * progress + random.nextFloat() * 0.06f,
        )
    }

    companion object {
        const val PLAYER_Y = 0.9f
        const val PLAYER_HALF_WIDTH = 0.13f
        const val ITEM_RADIUS = 0.055f
        private const val CATCH_TOP = PLAYER_Y - 0.06f
        private const val CATCH_BOTTOM = PLAYER_Y + 0.05f
    }
}
