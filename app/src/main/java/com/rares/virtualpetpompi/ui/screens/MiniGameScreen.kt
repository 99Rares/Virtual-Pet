package com.rares.virtualpetpompi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.R
import com.rares.virtualpetpompi.data.BuyResult
import com.rares.virtualpetpompi.data.GameState
import com.rares.virtualpetpompi.domain.Catalog
import com.rares.virtualpetpompi.domain.CatchGame
import com.rares.virtualpetpompi.domain.ItemKind
import com.rares.virtualpetpompi.domain.MiniGameRules
import com.rares.virtualpetpompi.ui.components.IconValue
import com.rares.virtualpetpompi.ui.components.formatInt
import com.rares.virtualpetpompi.ui.components.glass
import com.rares.virtualpetpompi.ui.components.materialize
import com.rares.virtualpetpompi.ui.components.panel
import com.rares.virtualpetpompi.ui.components.pressable
import com.rares.virtualpetpompi.ui.theme.PetTheme
import kotlinx.coroutines.launch

private enum class Phase { BET, PLAYING, RESULT }

private val Ink = Color(0xFF12131A)

/**
 * Full-screen catch game. Bet first (taken up front), play 30 seconds, then see the payout.
 * Covers the tab bar on purpose: it is a mode, not a destination.
 */
@Composable
fun MiniGameScreen(
    state: GameState,
    onStartRound: suspend (Int) -> BuyResult,
    onFinishRound: suspend (bet: Int, score: Int) -> MiniGameRules.Result,
    onExit: () -> Unit,
) {
    val c = PetTheme.colors
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var phase by remember { mutableStateOf(Phase.BET) }
    var bet by remember { mutableIntStateOf(MiniGameRules.bets.first()) }
    var game by remember { mutableStateOf<CatchGame?>(null) }
    var result by remember { mutableStateOf<MiniGameRules.Result?>(null) }
    var finalScore by remember { mutableIntStateOf(0) }
    var frame by remember { mutableIntStateOf(0) }

    // While playing, leaving would forfeit the bet silently; the system back gesture is ignored.
    BackHandler(enabled = phase == Phase.PLAYING) {}
    BackHandler(enabled = phase != Phase.PLAYING) { onExit() }

    LaunchedEffect(game) {
        val g = game ?: return@LaunchedEffect
        var last = 0L
        var caught = 0
        var lives = g.lives
        while (!g.isOver) {
            androidx.compose.runtime.withFrameNanos { now ->
                if (last != 0L) g.step(((now - last) / 1_000_000_000f).coerceAtMost(0.05f))
                last = now
                frame++
            }
            if (g.score != caught) { caught = g.score; haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
            if (g.lives != lives) { lives = g.lives; haptics.performHapticFeedback(HapticFeedbackType.Reject) }
        }
        finalScore = g.score
        result = onFinishRound(bet, g.score)
        phase = Phase.RESULT
    }

    Box(Modifier.fillMaxSize().background(c.base)) {
        Image(
            painterResource(Catalog.room(state.selectedRoom).image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))

        when (phase) {
            Phase.BET -> BetPanel(
                state = state,
                bet = bet,
                onBet = { bet = it },
                onStart = {
                    scope.launch {
                        if (onStartRound(bet) == BuyResult.OK) {
                            game = CatchGame()
                            phase = Phase.PLAYING
                        }
                    }
                },
                onClose = onExit,
            )
            Phase.PLAYING -> game?.let { PlayField(it, frame) }
            Phase.RESULT -> ResultPanel(
                bet = bet,
                score = finalScore,
                result = result,
                onAgain = { game = null; phase = Phase.BET },
                onDone = onExit,
            )
        }
    }
}

@Composable
private fun BetPanel(state: GameState, bet: Int, onBet: (Int) -> Unit, onStart: () -> Unit, onClose: () -> Unit) {
    val affordable = state.coins >= bet
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.game_title), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            IconValue(R.drawable.coinimage, formatInt(state.coins), color = Color.White)
        }
        Spacer(Modifier.weight(1f))
        Column(
            Modifier.fillMaxWidth().materialize(0).glass(RoundedCornerShape(28.dp), Color(0x66000000)).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.game_rules), style = MaterialTheme.typography.bodyLarge, color = Color.White, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.game_payouts, MiniGameRules.GOOD_CATCHES, MiniGameRules.GREAT_CATCHES, MiniGameRules.PERFECT_CATCHES),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.game_bet_label), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniGameRules.bets.forEach { option ->
                    val selected = option == bet
                    val canAfford = state.coins >= option
                    Text(
                        option.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) Ink else Color.White,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) Color.White else Color.White.copy(alpha = 0.18f))
                            .graphicsLayer { alpha = if (canAfford) 1f else 0.4f }
                            .pressable(enabled = canAfford) { onBet(option) }
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                    )
                }
            }
            Text(
                if (state.gameWinsLeftToday > 0) stringResource(R.string.game_cap_left, state.gameWinsLeftToday) else stringResource(R.string.game_cap_reached),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.weight(1f))
        PrimaryPill(stringResource(R.string.game_start), enabled = affordable, onClick = onStart)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.game_close),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.clip(CircleShape).pressable(onClick = onClose).padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun PlayField(game: CatchGame, frame: Int) {
    val foods = remember { Catalog.foods.take(8).map { it.image } }
    val foodBitmaps = foods.map { ImageBitmap.imageResource(it) }
    val skull = ImageBitmap.imageResource(R.drawable.skull)
    val pandaFrames = listOf(R.drawable.idle1, R.drawable.idle2, R.drawable.idle3).map { ImageBitmap.imageResource(it) }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(game) {
                detectDragGestures(
                    onDragStart = { game.movePlayerTo(it.x / size.width) },
                    onDrag = { change, _ -> game.movePlayerTo(change.position.x / size.width) },
                )
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (frame < 0) return@Canvas // reading it makes the canvas redraw every frame
            val w = size.width
            val h = size.height
            val itemSize = (CatchGame.ITEM_RADIUS * 2 * w).toInt()
            game.items.forEach { item ->
                val bmp = if (item.kind == ItemKind.SKULL) skull else foodBitmaps[item.variant % foodBitmaps.size]
                drawItem(bmp, (item.x * w - itemSize / 2f).toInt(), (item.y * h - itemSize / 2f).toInt(), itemSize, rounded = item.kind == ItemKind.FOOD)
            }
            val pandaSize = (CatchGame.PLAYER_HALF_WIDTH * 2 * w * 1.5f).toInt()
            val pandaBmp = pandaFrames[(game.elapsed * 4).toInt() % pandaFrames.size]
            drawBitmap(pandaBmp, (game.playerX * w - pandaSize / 2f).toInt(), (CatchGame.PLAYER_Y * h - pandaSize * 0.55f).toInt(), pandaSize)
        }
        Hud(game)
    }
}

private fun DrawScope.drawBitmap(bitmap: ImageBitmap, x: Int, y: Int, size: Int) {
    drawImage(bitmap, dstOffset = IntOffset(x, y), dstSize = IntSize(size, size))
}

/** Food art is on a white plate with rounded corners, matching the shop. */
private fun DrawScope.drawItem(bitmap: ImageBitmap, x: Int, y: Int, size: Int, rounded: Boolean) {
    if (!rounded) return drawBitmap(bitmap, x, y, size)
    val shape = Path().apply {
        addRoundRect(RoundRect(x.toFloat(), y.toFloat(), (x + size).toFloat(), (y + size).toFloat(), CornerRadius(size * 0.28f)))
    }
    clipPath(shape) { drawBitmap(bitmap, x, y, size) }
}

@Composable
private fun Hud(game: CatchGame) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(game.score.toString(), style = MaterialTheme.typography.displayMedium, color = Color.White)
            Text(stringResource(R.string.game_score_label), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(game.timeLeft.toInt().plus(1).coerceAtMost(game.durationSeconds.toInt()).toString() + "s", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(game.startLives) { i ->
                    Image(
                        painterResource(R.drawable.heart),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        alpha = if (i < game.lives) 1f else 0.25f,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultPanel(bet: Int, score: Int, result: MiniGameRules.Result?, onAgain: () -> Unit, onDone: () -> Unit) {
    val c = PetTheme.colors
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Column(
            Modifier.fillMaxWidth().materialize(0).glass(RoundedCornerShape(28.dp), Color(0x66000000)).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(score.toString(), style = MaterialTheme.typography.displayLarge, color = Color.White)
            Text(stringResource(R.string.game_score_label), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(12.dp))
            if (result != null) {
                val (text, color) = when {
                    result.net > 0 -> stringResource(R.string.game_result_won, result.net) to c.positive
                    result.net == 0 -> stringResource(R.string.game_result_even) to Color.White
                    else -> stringResource(R.string.game_result_lost, -result.net) to c.negative
                }
                Text(text, style = MaterialTheme.typography.titleLarge, color = color, textAlign = TextAlign.Center)
                if (result.capped) {
                    Text(stringResource(R.string.game_result_capped), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
                }
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryPill(stringResource(R.string.game_again), enabled = true, onClick = onAgain)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.game_done),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.clip(CircleShape).pressable(onClick = onDone).padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun PrimaryPill(label: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.titleMedium,
        color = Ink,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 1f else 0.35f))
            .pressable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
    )
}
