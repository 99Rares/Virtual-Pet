package com.rares.virtualpetpompi.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.ui.theme.EaseOut
import com.rares.virtualpetpompi.ui.theme.LocalReduceMotion
import kotlinx.coroutines.delay

/** Pressable with immediate feedback: scale to 0.97 in 120ms on touch-down, committed on release. */
@Composable
fun Modifier.pressable(
    enabled: Boolean = true,
    role: Role = Role.Button,
    onClick: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val reduce = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !reduce) 0.97f else 1f,
        animationSpec = tween(120, easing = EaseOut),
        label = "press",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, enabled = enabled, role = role, onClick = onClick)
}

/**
 * Content materialises once when it arrives: fade plus a 6dp rise, 360ms ease-out, staggered 30ms
 * per [index]. With reduced motion it is a plain fade. Not for anything that re-renders often.
 */
@Composable
fun Modifier.materialize(index: Int = 0): Modifier {
    val reduce = LocalReduceMotion.current
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(12) * 30L)
        shown = true
    }
    val progress by animateFloatAsState(if (shown) 1f else 0f, tween(360, easing = EaseOut), label = "materialize")
    val rise = with(LocalDensity.current) { 6.dp.toPx() }
    return this.graphicsLayer {
        alpha = progress
        if (!reduce) translationY = (1f - progress) * rise
    }
}
