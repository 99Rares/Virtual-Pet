package com.rares.virtualpetpompi.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Design tokens for the Liquid Glass look. Colour carries meaning; surfaces stay neutral. */
@Immutable
data class PetColors(
    val base: Color,
    val washIndigo: Color,
    val washTeal: Color,
    val washMagenta: Color,
    val text: Color,
    val textMuted: Color,
    /** Fill for floating chrome (tab bar, header tiles) drawn over a blurred backdrop. */
    val glassTint: Color,
    /** Fill for content panels sitting directly on the backdrop. */
    val panel: Color,
    val panelStrong: Color,
    val rimBright: Color,
    val rimFaint: Color,
    val accent: Color,
    val positive: Color,
    val negative: Color,
    val solid: Color,
    val isDark: Boolean,
)

val DarkPetColors = PetColors(
    base = Color(0xFF07080D),
    washIndigo = Color(0x665B5BD6),
    washTeal = Color(0x552BB5A0),
    washMagenta = Color(0x44C04BB5),
    text = Color(0xFFF4F5F9),
    textMuted = Color(0xB3F4F5F9),
    glassTint = Color(0x73141620),
    panel = Color(0x14FFFFFF),
    panelStrong = Color(0x24FFFFFF),
    rimBright = Color(0x66FFFFFF),
    rimFaint = Color(0x14FFFFFF),
    accent = Color(0xFF8C8CF0),
    positive = Color(0xFF3DD6B0),
    negative = Color(0xFFFF9A4D),
    solid = Color(0xFF1B1D29),
    isDark = true,
)

val LightPetColors = PetColors(
    base = Color(0xFFEFF1F8),
    washIndigo = Color(0x55878CF2),
    washTeal = Color(0x4458D3BD),
    washMagenta = Color(0x33E08AD6),
    text = Color(0xFF12131A),
    textMuted = Color(0xA612131A),
    glassTint = Color(0x80FFFFFF),
    panel = Color(0x99FFFFFF),
    panelStrong = Color(0xCCFFFFFF),
    rimBright = Color(0xE6FFFFFF),
    rimFaint = Color(0x33FFFFFF),
    accent = Color(0xFF4F4FC9),
    positive = Color(0xFF0E9F82),
    negative = Color(0xFFD9680F),
    solid = Color(0xFFF7F8FC),
    isDark = false,
)

val LocalPetColors = compositionLocalOf { DarkPetColors }

/** True when the user turned system animations off; motion then degrades to plain fades. */
val LocalReduceMotion = compositionLocalOf { false }

object PetTheme {
    val colors: PetColors @Composable @ReadOnlyComposable get() = LocalPetColors.current
}

/** Strong ease-out for entrances and feedback; never ease-in. */
val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

private val tabular = "tnum"
private val system = FontFamily.Default

/** Tracking tightens as text grows, as in the system type scale. */
val PetTypography = Typography(
    displayLarge = TextStyle(fontFamily = system, fontWeight = FontWeight.Bold, fontSize = 64.sp, lineHeight = 64.sp, letterSpacing = (-0.04).em, fontFeatureSettings = tabular),
    displayMedium = TextStyle(fontFamily = system, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-0.04).em, fontFeatureSettings = tabular),
    headlineMedium = TextStyle(fontFamily = system, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.03).em),
    titleLarge = TextStyle(fontFamily = system, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.025).em),
    titleMedium = TextStyle(fontFamily = system, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.01).em, fontFeatureSettings = tabular),
    bodyLarge = TextStyle(fontFamily = system, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = (-0.01).em),
    bodyMedium = TextStyle(fontFamily = system, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = (-0.005).em),
    labelLarge = TextStyle(fontFamily = system, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp, fontFeatureSettings = tabular),
    labelMedium = TextStyle(fontFamily = system, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.01.em, fontFeatureSettings = tabular),
    labelSmall = TextStyle(fontFamily = system, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.05.em),
)

@Composable
fun PetTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) DarkPetColors else LightPetColors
    val context = LocalContext.current
    val reduceMotion = remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
    val scheme = if (dark) {
        darkColorScheme(primary = colors.accent, background = colors.base, surface = colors.solid, onSurface = colors.text, onBackground = colors.text)
    } else {
        lightColorScheme(primary = colors.accent, background = colors.base, surface = colors.solid, onSurface = colors.text, onBackground = colors.text)
    }
    CompositionLocalProvider(LocalPetColors provides colors, LocalReduceMotion provides reduceMotion) {
        MaterialTheme(colorScheme = scheme, typography = PetTypography, content = content)
    }
}

/** The fixed soft backdrop the glass refracts: three colour washes over a near-black (or pale) base. */
@Composable
fun Backdrop(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    val c = PetTheme.colors
    Box(
        modifier
            .fillMaxSize()
            .background(c.base)
            .drawBehind {
                val w = size.width
                val h = size.height
                drawRect(Brush.radialGradient(listOf(c.washIndigo, Color.Transparent), Offset(w * 0.15f, h * 0.05f), w * 1.1f))
                drawRect(Brush.radialGradient(listOf(c.washTeal, Color.Transparent), Offset(w * 0.95f, h * 0.45f), w * 1.0f))
                drawRect(Brush.radialGradient(listOf(c.washMagenta, Color.Transparent), Offset(w * 0.2f, h * 0.95f), w * 1.0f))
            },
    ) { content() }
}
