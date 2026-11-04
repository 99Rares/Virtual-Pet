package com.rares.virtualpetpompi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.ui.theme.PetTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/** The blur source shared by everything that is glass. Set once at the app root. */
val LocalHazeState = staticCompositionLocalOf<HazeState> { error("No HazeState provided") }

/** 1dp specular rim: bright at the top-left, fading along the sides, a faint catch-light bottom-right. */
@Composable
private fun rimBrush(): Brush {
    val c = PetTheme.colors
    return Brush.linearGradient(
        0f to c.rimBright,
        0.35f to c.rimFaint,
        0.75f to c.rimFaint,
        1f to c.rimBright.copy(alpha = c.rimBright.alpha * 0.35f),
    )
}

/**
 * Glass is the navigation layer: floating chrome that blurs whatever scrolls beneath it.
 * Never nest glass in glass; controls inside use [panel] fills instead.
 */
@Composable
fun Modifier.glass(shape: Shape, tint: Color? = null): Modifier {
    val c = PetTheme.colors
    val haze = LocalHazeState.current
    val style = HazeBlurStyle {
        backgroundColor(c.solid)
        blurRadius(24.dp)
        noiseFactor(0f)
        colorEffects(listOf(HazeColorEffect.tint(tint ?: c.glassTint)))
    }
    return this
        .clip(shape)
        .hazeBlur(input = HazeInput.Sources(haze), style = style)
        .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = if (c.isDark) 0.10f else 0.35f), Color.Transparent)))
        .border(1.dp, rimBrush(), shape)
}

/** Content material: a light frosted fill directly on the backdrop. No blur, so it can sit anywhere. */
@Composable
fun Modifier.panel(shape: Shape, strong: Boolean = false): Modifier {
    val c = PetTheme.colors
    return this
        .clip(shape)
        .background(if (strong) c.panelStrong else c.panel)
        .border(1.dp, rimBrush(), shape)
}
