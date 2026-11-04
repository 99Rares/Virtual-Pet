package com.rares.virtualpetpompi.domain

/** Game difficulty. Decides how fast steps turn into coins and how forgiving the pet is. */
enum class Difficulty(val stepsPerCoin: Int, val startLives: Int, val starterCoins: Int) {
    EASY(stepsPerCoin = 10, startLives = 1000, starterCoins = 100),
    NORMAL(stepsPerCoin = 100, startLives = 5, starterCoins = 5),
}
