package com.rares.virtualpetpompi.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rares.virtualpetpompi.R

/**
 * A complete set of Pompi animation frames. Every skin has the same frame counts and canvas
 * (600x650, feet on one baseline), so the sprite can swap sets without repositioning.
 * [id] is stored in DataStore, so never rename one.
 */
data class Skin(
    val id: String,
    @StringRes val name: Int,
    @StringRes val description: Int,
    val price: Int,
    @DrawableRes val preview: Int,
    val idle: List<Int>,
    val hungry: List<Int>,
    val wake: List<Int>,
    val dance: List<Int>,
    val feed: List<Int>,
)

object Skins {
    const val DEFAULT_ID = "classic"

    val all: List<Skin> = listOf(
        Skin(
            "classic", R.string.skin_classic, R.string.skin_classic_desc, 0, R.drawable.classic_preview,
            idle = listOf(R.drawable.idle1, R.drawable.idle2, R.drawable.idle3),
            hungry = listOf(R.drawable.hungry1, R.drawable.hungry2),
            wake = listOf(R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4, R.drawable.a5, R.drawable.a6, R.drawable.a7, R.drawable.a8, R.drawable.a9, R.drawable.a10),
            dance = listOf(R.drawable.dance1, R.drawable.dance2, R.drawable.dance3),
            feed = listOf(R.drawable.feed1, R.drawable.feed2),
        ),
        Skin(
            "red_panda", R.string.skin_red_panda, R.string.skin_red_panda_desc, 60, R.drawable.red_panda_preview,
            idle = listOf(R.drawable.red_panda_idle1, R.drawable.red_panda_idle2, R.drawable.red_panda_idle3),
            hungry = listOf(R.drawable.red_panda_hungry1, R.drawable.red_panda_hungry2),
            wake = listOf(R.drawable.red_panda_wake1, R.drawable.red_panda_wake2, R.drawable.red_panda_wake3, R.drawable.red_panda_wake4, R.drawable.red_panda_wake5, R.drawable.red_panda_wake6, R.drawable.red_panda_wake7, R.drawable.red_panda_wake8, R.drawable.red_panda_wake9, R.drawable.red_panda_wake10),
            dance = listOf(R.drawable.red_panda_dance1, R.drawable.red_panda_dance2, R.drawable.red_panda_dance3),
            feed = listOf(R.drawable.red_panda_feed1, R.drawable.red_panda_feed2),
        ),
        Skin(
            "polar_bear", R.string.skin_polar_bear, R.string.skin_polar_bear_desc, 80, R.drawable.polar_bear_preview,
            idle = listOf(R.drawable.polar_bear_idle1, R.drawable.polar_bear_idle2, R.drawable.polar_bear_idle3),
            hungry = listOf(R.drawable.polar_bear_hungry1, R.drawable.polar_bear_hungry2),
            wake = listOf(R.drawable.polar_bear_wake1, R.drawable.polar_bear_wake2, R.drawable.polar_bear_wake3, R.drawable.polar_bear_wake4, R.drawable.polar_bear_wake5, R.drawable.polar_bear_wake6, R.drawable.polar_bear_wake7, R.drawable.polar_bear_wake8, R.drawable.polar_bear_wake9, R.drawable.polar_bear_wake10),
            dance = listOf(R.drawable.polar_bear_dance1, R.drawable.polar_bear_dance2, R.drawable.polar_bear_dance3),
            feed = listOf(R.drawable.polar_bear_feed1, R.drawable.polar_bear_feed2),
        ),
        Skin(
            "cozy", R.string.skin_cozy, R.string.skin_cozy_desc, 100, R.drawable.cozy_preview,
            idle = listOf(R.drawable.cozy_idle1, R.drawable.cozy_idle2, R.drawable.cozy_idle3),
            hungry = listOf(R.drawable.cozy_hungry1, R.drawable.cozy_hungry2),
            wake = listOf(R.drawable.cozy_wake1, R.drawable.cozy_wake2, R.drawable.cozy_wake3, R.drawable.cozy_wake4, R.drawable.cozy_wake5, R.drawable.cozy_wake6, R.drawable.cozy_wake7, R.drawable.cozy_wake8, R.drawable.cozy_wake9, R.drawable.cozy_wake10),
            dance = listOf(R.drawable.cozy_dance1, R.drawable.cozy_dance2, R.drawable.cozy_dance3),
            feed = listOf(R.drawable.cozy_feed1, R.drawable.cozy_feed2),
        ),
        Skin(
            "astronaut", R.string.skin_astronaut, R.string.skin_astronaut_desc, 120, R.drawable.astronaut_preview,
            idle = listOf(R.drawable.astronaut_idle1, R.drawable.astronaut_idle2, R.drawable.astronaut_idle3),
            hungry = listOf(R.drawable.astronaut_hungry1, R.drawable.astronaut_hungry2),
            wake = listOf(R.drawable.astronaut_wake1, R.drawable.astronaut_wake2, R.drawable.astronaut_wake3, R.drawable.astronaut_wake4, R.drawable.astronaut_wake5, R.drawable.astronaut_wake6, R.drawable.astronaut_wake7, R.drawable.astronaut_wake8, R.drawable.astronaut_wake9, R.drawable.astronaut_wake10),
            dance = listOf(R.drawable.astronaut_dance1, R.drawable.astronaut_dance2, R.drawable.astronaut_dance3),
            feed = listOf(R.drawable.astronaut_feed1, R.drawable.astronaut_feed2),
        ),
    )

    fun byId(id: String): Skin = all.firstOrNull { it.id == id } ?: all.first()
}
