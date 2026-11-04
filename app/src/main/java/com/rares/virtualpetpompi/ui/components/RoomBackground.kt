package com.rares.virtualpetpompi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.rares.virtualpetpompi.domain.Room
import com.rares.virtualpetpompi.domain.RoomArt
import com.rares.virtualpetpompi.domain.SceneKind
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Draws a room, cropped to fill [modifier]. Painted scenes scale with the available size. */
@Composable
fun RoomBackground(room: Room, modifier: Modifier = Modifier) {
    when (val art = room.art) {
        is RoomArt.Image -> Image(painterResource(art.res), null, modifier, contentScale = ContentScale.Crop)
        is RoomArt.Scene -> {
            // Stars and petals are scattered once per scene so they do not jump when recomposed.
            val scatter = remember(art.kind) { List(90) { Triple(it, Random(it).nextFloat(), Random(it + 1000).nextFloat()) } }
            Canvas(modifier) { drawScene(art.kind, scatter) }
        }
    }
}

private fun DrawScope.drawScene(kind: SceneKind, scatter: List<Triple<Int, Float, Float>>) {
    val w = size.width
    val h = size.height
    when (kind) {
        SceneKind.SUNSET -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF2B1B4D), Color(0xFFB5416B), Color(0xFFFF8A4C), Color(0xFFFFC46B))))
            val sun = Offset(w * 0.5f, h * 0.62f)
            drawCircle(Brush.radialGradient(listOf(Color(0x66FFE9A8), Color.Transparent), sun, w * 0.7f), w * 0.7f, sun)
            drawCircle(Color(0xFFFFE9A8), w * 0.22f, sun)
            hill(Color(0xFF4A2358), h * 0.80f, h * 0.06f, 0.0f)
            hill(Color(0xFF2A1238), h * 0.88f, h * 0.05f, 1.7f)
        }
        SceneKind.NIGHT -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF050A24), Color(0xFF14214F), Color(0xFF2A3F7A))))
            scatter.forEach { (i, x, y) ->
                val r = (0.45f + (i % 4) * 0.3f) * density.coerceAtLeast(1f)
                drawCircle(Color.White.copy(alpha = 0.35f + (i % 5) * 0.13f), r, Offset(x * w, y * h * 0.85f))
            }
            val moon = Offset(w * 0.74f, h * 0.17f)
            drawCircle(Brush.radialGradient(listOf(Color(0x55FFF6D6), Color.Transparent), moon, w * 0.35f), w * 0.35f, moon)
            drawCircle(Color(0xFFFFF6D6), w * 0.09f, moon)
            hill(Color(0xFF0B1235), h * 0.9f, h * 0.04f, 0.6f)
        }
        SceneKind.OCEAN -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF0A4D68), Color(0xFF088395), Color(0xFF7ED4D1))))
            for (i in 0 until 5) {
                wave(Color.White.copy(alpha = 0.10f), h * (0.35f + i * 0.13f), h * 0.018f, i * 1.3f)
            }
        }
        SceneKind.BLOSSOM -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFFFD6E0), Color(0xFFFFA5BE), Color(0xFFE87AA0))))
            scatter.take(45).forEach { (i, x, y) ->
                val r = w * (0.012f + (i % 5) * 0.008f)
                drawCircle(Color.White.copy(alpha = 0.22f + (i % 4) * 0.08f), r, Offset(x * w, y * h))
            }
        }
    }
}

private fun DrawScope.hill(color: Color, baseline: Float, amplitude: Float, phase: Float) {
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, baseline)
        val steps = 24
        for (s in 0..steps) {
            val t = s / steps.toFloat()
            lineTo(t * size.width, baseline + amplitude * sin(t * 2 * PI.toFloat() + phase))
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.wave(color: Color, baseline: Float, amplitude: Float, phase: Float) {
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, baseline)
        val steps = 40
        for (s in 0..steps) {
            val t = s / steps.toFloat()
            lineTo(t * size.width, baseline + amplitude * sin(t * 4 * PI.toFloat() + phase))
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}
