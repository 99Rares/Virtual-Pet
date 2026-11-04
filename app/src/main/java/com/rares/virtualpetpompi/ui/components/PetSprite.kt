package com.rares.virtualpetpompi.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.rares.virtualpetpompi.R
import com.rares.virtualpetpompi.domain.Skin
import kotlinx.coroutines.delay

private class Frame(@DrawableRes val res: Int, val millis: Int)

private fun frames(ms: Int, vararg res: Int) = res.map { Frame(it, ms) }

/** The timed clips of one skin; frame order and timings are the same for every skin. */
private class Clips(skin: Skin) {
    val idle = frames(220, *skin.idle.toIntArray())
    val hungry = frames(250, *skin.hungry.toIntArray())
    val wake = frames(110, *skin.wake.toIntArray())
    val dance = skin.dance.let { (a, b, c) -> frames(130, a, b, c, b, c, b, a, b, c, b, a) }
    val feed = skin.feed.let { (a, b) -> frames(200, a, b, a, b, a, b) }
}

/** One-off animations layered over the idle loop. [token] makes repeating the same kind restart it. */
enum class SpriteKind { WAKE, DANCE, FEED }

data class SpriteAction(val kind: SpriteKind, val token: Int)

/**
 * Pompi in the given [skin]. Loops idle (or hungry when [hungry]) and plays
 * an [action] once whenever a new one arrives, then falls back to the loop.
 */
@Composable
fun PetSprite(skin: Skin, hungry: Boolean, action: SpriteAction?, modifier: Modifier = Modifier) {
    val clips = remember(skin) { Clips(skin) }
    var frame by remember(skin) { mutableIntStateOf(skin.idle.first()) }
    LaunchedEffect(skin, hungry, action) {
        action?.let {
            val clip = when (it.kind) {
                SpriteKind.WAKE -> clips.wake
                SpriteKind.DANCE -> clips.dance
                SpriteKind.FEED -> clips.feed
            }
            for (f in clip) {
                frame = f.res
                delay(f.millis.toLong())
            }
        }
        val loop = if (hungry) clips.hungry else clips.idle
        while (true) {
            for (f in loop) {
                frame = f.res
                delay(f.millis.toLong())
            }
        }
    }
    Image(
        painter = painterResource(frame),
        contentDescription = stringResource(R.string.tab_pet),
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
