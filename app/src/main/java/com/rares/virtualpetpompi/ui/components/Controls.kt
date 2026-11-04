package com.rares.virtualpetpompi.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.clickable
import com.rares.virtualpetpompi.ui.theme.PetTheme

/** One thumb that glides between [count] equal slots with a critically damped spring. */
@Composable
private fun SlidingThumb(slotWidth: Dp, index: Int, height: Dp, modifier: Modifier = Modifier) {
    val x by animateDpAsState(
        targetValue = slotWidth * index,
        animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow),
        label = "thumb",
    )
    Box(
        modifier
            .offset(x = x)
            .width(slotWidth)
            .height(height)
            .clip(CircleShape)
            .background(PetTheme.colors.panelStrong),
    )
}

data class TabItem(val label: String, val icon: @Composable (tint: Color) -> Unit)

/** Floating glass capsule tab bar, shown above the system navigation area. */
@Composable
fun GlassTabBar(
    items: List<TabItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PetTheme.colors
    BoxWithConstraints(modifier.glass(CircleShape).padding(6.dp)) {
        val slot = maxWidth / items.size
        SlidingThumb(slot, selected, 56.dp)
        Row {
            items.forEachIndexed { i, item ->
                val tint by animateColorAsState(if (i == selected) c.text else c.textMuted, label = "tint")
                Column(
                    Modifier
                        .width(slot)
                        .height(56.dp)
                        .clip(CircleShape)
                        .semantics { role = Role.Tab; this.selected = i == selected }
                        .clickable(role = Role.Tab, onClick = { onSelect(i) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    item.icon(tint)
                    Text(item.label, style = MaterialTheme.typography.labelMedium, color = tint)
                }
            }
        }
    }
}

/** Sub-navigation: a segmented control on a content panel (not glass, to avoid glass on glass). */
@Composable
fun Segmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PetTheme.colors
    BoxWithConstraints(modifier.panel(CircleShape).padding(4.dp)) {
        val slot = maxWidth / options.size
        SlidingThumb(slot, selected, 36.dp)
        Row {
            options.forEachIndexed { i, label ->
                val tint by animateColorAsState(if (i == selected) c.text else c.textMuted, label = "tint")
                Box(
                    Modifier
                        .width(slot)
                        .height(36.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Tab, onClick = { onSelect(i) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = tint, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** A secondary figure: small label over a value, on a content panel. */
@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = PetTheme.colors.text) {
    Column(modifier.panel(RoundedCornerShape(20.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = PetTheme.colors.textMuted)
        Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor)
    }
}

@Composable
fun Icon(vector: ImageVector, tint: Color, contentDescription: String? = null, modifier: Modifier = Modifier) {
    androidx.compose.material3.Icon(vector, contentDescription, modifier.height(22.dp), tint = tint)
}
