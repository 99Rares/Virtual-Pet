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
import kotlinx.coroutines.delay

private class Frame(@DrawableRes val res: Int, val millis: Int)

private fun frames(ms: Int, vararg res: Int) = res.map { Frame(it, ms) }

private object Frames {
    val idle = frames(220, R.drawable.idle1, R.drawable.idle2, R.drawable.idle3)
    val hungry = frames(250, R.drawable.hungry1, R.drawable.hungry2)
    val wake = frames(110, R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4, R.drawable.a5,
        R.drawable.a6, R.drawable.a7, R.drawable.a8, R.drawable.a9, R.drawable.a10)
    val dance = frames(130, R.drawable.dance1, R.drawable.dance2, R.drawable.dance3, R.drawable.dance2,
        R.drawable.dance3, R.drawable.dance2, R.drawable.dance1, R.drawable.dance2, R.drawable.dance3,
        R.drawable.dance2, R.drawable.dance1)
    val feed = frames(200, R.drawable.feed1, R.drawable.feed2, R.drawable.feed1, R.drawable.feed2,
        R.drawable.feed1, R.drawable.feed2)
}

/** One-off animations layered over the idle loop. [token] makes repeating the same kind restart it. */
enum class SpriteKind { WAKE, DANCE, FEED }

data class SpriteAction(val kind: SpriteKind, val token: Int)

/**
 * Pompi, drawn from the original hand-made frames. Loops idle (or hungry when [hungry]) and plays
 * an [action] once whenever a new one arrives, then falls back to the loop.
 */
@Composable
fun PetSprite(hungry: Boolean, action: SpriteAction?, modifier: Modifier = Modifier) {
    var frame by remember { mutableIntStateOf(R.drawable.idle1) }
    LaunchedEffect(hungry, action) {
        action?.let {
            val clip = when (it.kind) {
                SpriteKind.WAKE -> Frames.wake
                SpriteKind.DANCE -> Frames.dance
                SpriteKind.FEED -> Frames.feed
            }
            for (f in clip) {
                frame = f.res
                delay(f.millis.toLong())
            }
        }
        val loop = if (hungry) Frames.hungry else Frames.idle
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
