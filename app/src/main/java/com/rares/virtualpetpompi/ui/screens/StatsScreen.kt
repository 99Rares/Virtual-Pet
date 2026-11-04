package com.rares.virtualpetpompi.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.R
import com.rares.virtualpetpompi.data.StepStats
import com.rares.virtualpetpompi.ui.components.ScreenColumn
import com.rares.virtualpetpompi.ui.components.StatTile
import com.rares.virtualpetpompi.ui.components.formatInt
import com.rares.virtualpetpompi.ui.components.materialize
import com.rares.virtualpetpompi.ui.components.panel
import com.rares.virtualpetpompi.ui.theme.EaseOut
import com.rares.virtualpetpompi.ui.theme.LocalReduceMotion
import com.rares.virtualpetpompi.ui.theme.PetTheme
import java.time.format.TextStyle
import kotlin.math.max

@Composable
fun StatsScreen(stats: StepStats?) {
    val c = PetTheme.colors
    ScreenColumn(stringResource(R.string.tab_stats)) {
        if (stats == null) return@ScreenColumn
        val reached = stats.today >= stats.goal

        Row(Modifier.fillMaxWidth().materialize(1), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ProgressRing(stats.today, stats.goal, Modifier.size(132.dp))
            Column {
                Text(formatInt(stats.today), style = MaterialTheme.typography.displayMedium, color = c.text)
                Text(stringResource(R.string.steps_today), style = MaterialTheme.typography.bodyLarge, color = c.textMuted)
                Text(
                    stringResource(R.string.goal_of, formatInt(stats.goal)),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (reached) c.positive else c.textMuted,
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        Column(Modifier.fillMaxWidth().materialize(2).panel(RoundedCornerShape(24.dp)).padding(16.dp)) {
            Text(stringResource(R.string.last_7_days), style = MaterialTheme.typography.titleMedium, color = c.text)
            Spacer(Modifier.height(12.dp))
            WeekChart(stats, Modifier.fillMaxWidth().height(180.dp))
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth().materialize(3), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(stringResource(R.string.this_week), formatInt(stats.weekTotal), Modifier.weight(1f))
            StatTile(stringResource(R.string.average), formatInt(stats.weekAverage), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().materialize(4), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(stringResource(R.string.record), formatInt(stats.record), Modifier.weight(1f))
            StatTile(stringResource(R.string.total), formatInt(stats.total), Modifier.weight(1f))
        }
    }
}

/** Sweeps in once on first appearance; reduced motion skips the sweep. */
@Composable
private fun rememberSweep(): Float {
    val reduce = LocalReduceMotion.current
    val sweep = remember { Animatable(if (reduce) 1f else 0f) }
    LaunchedEffect(Unit) { sweep.animateTo(1f, tween(700, easing = EaseOut)) }
    return sweep.value
}

@Composable
private fun ProgressRing(steps: Int, goal: Int, modifier: Modifier = Modifier) {
    val c = PetTheme.colors
    val sweep = rememberSweep()
    val fraction = (steps.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val color = if (steps >= goal) c.positive else c.accent
    Canvas(modifier) {
        val stroke = 14.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(c.panelStrong, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        if (fraction > 0f) {
            drawArc(color, -90f, 360f * fraction * sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun WeekChart(stats: StepStats, modifier: Modifier = Modifier) {
    val c = PetTheme.colors
    val sweep = rememberSweep()
    val scale = max(stats.goal, stats.week.maxOf { it.steps }).coerceAtLeast(1)
    val locale = LocalConfiguration.current.locales[0]
    val labels = stats.week.map { it.date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale) }
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val slot = size.width / stats.week.size
            val barWidth = slot * 0.5f
            clipRect(right = size.width * sweep) {
                stats.week.forEachIndexed { i, day ->
                    val h = size.height * day.steps / scale
                    val x = slot * i + (slot - barWidth) / 2
                    val isToday = i == stats.week.lastIndex
                    val hit = day.steps >= stats.goal
                    val base = if (hit) c.positive else c.accent
                    drawRoundRect(
                        color = base.copy(alpha = if (isToday) 1f else 0.55f),
                        topLeft = Offset(x, size.height - h),
                        size = Size(barWidth, h.coerceAtLeast(2.dp.toPx())),
                        cornerRadius = CornerRadius(8.dp.toPx()),
                    )
                }
            }
            // Goal line stays on top and never animates; it is the reference.
            val y = size.height - size.height * stats.goal / scale
            drawLine(
                c.textMuted, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx())),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (i == labels.lastIndex) c.text else c.textMuted,
                )
            }
        }
    }
}
