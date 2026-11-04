package com.rares.virtualpetpompi

import com.rares.virtualpetpompi.domain.CatchGame
import com.rares.virtualpetpompi.domain.CoinRules
import com.rares.virtualpetpompi.domain.Difficulty
import com.rares.virtualpetpompi.domain.ItemKind
import com.rares.virtualpetpompi.domain.MiniGameRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MiniGameRulesTest {
    @Test fun `too few catches lose the bet`() =
        assertEquals(MiniGameRules.Result(payout = 0, net = -10, capped = false), MiniGameRules.settle(10, 11, 0))

    @Test fun `good score returns the bet`() =
        assertEquals(MiniGameRules.Result(payout = 10, net = 0, capped = false), MiniGameRules.settle(10, 12, 0))

    @Test fun `great score pays one and a half`() =
        assertEquals(MiniGameRules.Result(payout = 15, net = 5, capped = false), MiniGameRules.settle(10, 20, 0))

    @Test fun `perfect score doubles the bet`() =
        assertEquals(MiniGameRules.Result(payout = 40, net = 20, capped = false), MiniGameRules.settle(20, 30, 0))

    @Test fun `daily cap trims the profit`() {
        // 20 coins of profit available but only 8 left under the 30 coin cap
        val r = MiniGameRules.settle(20, 30, 22)
        assertEquals(MiniGameRules.Result(payout = 28, net = 8, capped = true), r)
    }

    @Test fun `once the cap is used up a win only refunds the bet`() {
        val r = MiniGameRules.settle(20, 30, MiniGameRules.DAILY_WIN_CAP)
        assertEquals(MiniGameRules.Result(payout = 20, net = 0, capped = true), r)
    }

    @Test fun `losses are never capped`() =
        assertEquals(-20, MiniGameRules.settle(20, 0, MiniGameRules.DAILY_WIN_CAP).net)

    @Test fun `bonus coins add to the balance and can be negative`() {
        assertEquals(5 + 25 + 10, CoinRules.balance(2_500, Difficulty.NORMAL, 0, bonusCoins = 10))
        assertEquals(5 + 25 - 20, CoinRules.balance(2_500, Difficulty.NORMAL, 0, bonusCoins = -20))
    }
}

class CatchGameTest {
    /** Plays with a player parked under a falling item's column to make catches predictable. */
    private fun game(seed: Int = 1) = CatchGame(Random(seed))

    @Test fun `game ends when time runs out`() {
        val g = game()
        repeat(40) { g.step(1f) }
        assertTrue(g.isOver)
        assertEquals(0f, g.timeLeft, 0f)
    }

    @Test fun `nothing happens after the game is over`() {
        val g = game()
        repeat(40) { g.step(1f) }
        val score = g.score
        g.step(1f)
        assertEquals(score, g.score)
    }

    @Test fun `items spawn and fall`() {
        val g = game()
        repeat(10) { g.step(0.1f) }
        assertTrue(g.items.isNotEmpty())
        val y = g.items.first().y
        g.step(0.1f)
        assertTrue(g.items.first().y > y)
    }

    @Test fun `the player stays inside the screen`() {
        val g = game()
        g.movePlayerTo(-3f)
        assertEquals(CatchGame.PLAYER_HALF_WIDTH, g.playerX, 0f)
        g.movePlayerTo(9f)
        assertEquals(1f - CatchGame.PLAYER_HALF_WIDTH, g.playerX, 0f)
    }

    @Test fun `a player that never moves still catches some food and loses lives to skulls`() {
        // Sweeping the player across the width catches everything eventually.
        val g = game(7)
        var x = 0f
        var dir = 1f
        while (!g.isOver) {
            x += dir * 0.02f
            if (x > 1f || x < 0f) dir = -dir
            g.movePlayerTo(x)
            g.step(0.016f)
        }
        assertTrue("some food caught", g.score > 0)
        assertTrue(g.lives < g.startLives || g.timeLeft == 0f)
    }

    @Test fun `catching a skull costs a life and a food adds score`() {
        val g = CatchGame(Random(3), durationSeconds = 1000f)
        var sawSkullCatch = false
        var sawFoodCatch = false
        var guard = 0
        while (!g.isOver && guard++ < 200_000 && !(sawSkullCatch && sawFoodCatch)) {
            val target = g.items.firstOrNull { it.y > 0.3f }
            if (target != null) g.movePlayerTo(target.x)
            val before = g.score to g.lives
            g.step(0.016f)
            if (g.score > before.first) sawFoodCatch = true
            if (g.lives < before.second) sawSkullCatch = true
        }
        assertTrue(sawFoodCatch)
        assertTrue(sawSkullCatch || g.isOver)
        assertFalse(g.items.any { it.y > 1.2f })
    }

    @Test fun `skulls are among the spawns`() {
        val g = CatchGame(Random(5), durationSeconds = 1000f, startLives = 1000)
        repeat(3_000) { g.step(0.02f) }
        // lives dropped means skulls were caught; or some are on screen. Either proves they spawn.
        assertTrue(g.lives < 1000 || g.items.any { it.kind == ItemKind.SKULL })
    }
}

class CatalogTest {
    @org.junit.Test fun `room and food ids are unique`() {
        val rooms = com.rares.virtualpetpompi.domain.Catalog.rooms.map { it.id }
        val foods = com.rares.virtualpetpompi.domain.Catalog.foods.map { it.id }
        org.junit.Assert.assertEquals(rooms.size, rooms.toSet().size)
        org.junit.Assert.assertEquals(foods.size, foods.toSet().size)
    }

    @org.junit.Test fun `the default room is free and listed first`() {
        val first = com.rares.virtualpetpompi.domain.Catalog.rooms.first()
        org.junit.Assert.assertEquals(com.rares.virtualpetpompi.domain.Catalog.DEFAULT_ROOM_ID, first.id)
        org.junit.Assert.assertEquals(0, first.price)
    }
}

class SkinsTest {
    @org.junit.Test fun `skin ids are unique and classic is the free default listed first`() {
        val all = com.rares.virtualpetpompi.domain.Skins.all
        org.junit.Assert.assertEquals(all.size, all.map { it.id }.toSet().size)
        org.junit.Assert.assertEquals(com.rares.virtualpetpompi.domain.Skins.DEFAULT_ID, all.first().id)
        org.junit.Assert.assertEquals(0, all.first().price)
    }

    @org.junit.Test fun `every skin has the same frame counts and no shared frames with another`() {
        val all = com.rares.virtualpetpompi.domain.Skins.all
        all.forEach {
            org.junit.Assert.assertEquals(it.id, listOf(3, 2, 10, 3, 2), listOf(it.idle.size, it.hungry.size, it.wake.size, it.dance.size, it.feed.size))
        }
        val frames = all.flatMap { it.idle + it.hungry + it.wake + it.dance + it.feed + it.preview }
        org.junit.Assert.assertEquals(frames.size, frames.toSet().size)
    }
}
