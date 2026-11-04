package com.rares.virtualpetpompi.domain

/** Game difficulty. Decides how fast steps turn into coins and how forgiving the pet is. */
enum class Difficulty(
    val stepsPerCoin: Int,
    val startLives: Int,
    val starterCoins: Int,
    /** What it costs to bring a dead pet back without losing food, rooms or coins. */
    val reviveCost: Int,
    /** Lives the pet comes back with. */
    val reviveLives: Int,
) {
    EASY(stepsPerCoin = 10, startLives = 1000, starterCoins = 100, reviveCost = 200, reviveLives = 500),
    NORMAL(stepsPerCoin = 100, startLives = 5, starterCoins = 5, reviveCost = 50, reviveLives = 3),
}
