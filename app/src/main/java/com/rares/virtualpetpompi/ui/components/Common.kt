package com.rares.virtualpetpompi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.ui.theme.PetTheme
import java.text.NumberFormat

/** Height of the floating tab bar plus its gap; scrolling content leaves this much room below. */
val TabBarInset = 56.dp + 12.dp + 6.dp * 2 + 16.dp

fun formatInt(value: Number): String = NumberFormat.getIntegerInstance().format(value)

/** Page shell: large title on the backdrop, scrolling body, room for the system bars and tab bar. */
@Composable
fun ScreenColumn(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            color = PetTheme.colors.text,
            modifier = Modifier.materialize(0),
        )
        Spacer(Modifier.height(16.dp))
        content()
        Spacer(Modifier.height(TabBarInset))
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
fun IconValue(
    icon: Int,
    value: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
    color: androidx.compose.ui.graphics.Color = PetTheme.colors.text,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(iconSize))
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, maxLines = 1, softWrap = false)
    }
}

val ScreenPadding = PaddingValues(horizontal = 20.dp)

/** Some of the original food art has an opaque white background, so every dish sits on a white plate. */
@Composable
fun FoodImage(@androidx.annotation.DrawableRes res: Int, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier
            .roundedPlate()
            .background(androidx.compose.ui.graphics.Color.White),
    ) {
        Image(painterResource(res), null, Modifier.fillMaxSize().padding(4.dp))
    }
}

private fun Modifier.roundedPlate(): Modifier =
    this.clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
