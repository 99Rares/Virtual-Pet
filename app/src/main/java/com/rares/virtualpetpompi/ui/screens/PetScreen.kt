package com.rares.virtualpetpompi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.R
import com.rares.virtualpetpompi.data.GameState
import com.rares.virtualpetpompi.domain.Catalog
import com.rares.virtualpetpompi.domain.PetRules
import com.rares.virtualpetpompi.ui.components.FoodImage
import com.rares.virtualpetpompi.ui.components.IconValue
import com.rares.virtualpetpompi.ui.components.PetSprite
import com.rares.virtualpetpompi.ui.components.RoomBackground
import com.rares.virtualpetpompi.ui.components.SpriteAction
import com.rares.virtualpetpompi.ui.components.SpriteKind
import com.rares.virtualpetpompi.ui.components.TabBarInset
import com.rares.virtualpetpompi.ui.components.formatInt
import com.rares.virtualpetpompi.ui.components.glass
import com.rares.virtualpetpompi.ui.components.materialize
import com.rares.virtualpetpompi.ui.components.panel
import com.rares.virtualpetpompi.ui.components.pressable
import com.rares.virtualpetpompi.ui.theme.PetTheme
import kotlinx.coroutines.launch

private val OnRoom = Color.White
/** Over the room artwork the chrome is always smoky, so white figures stay legible in both themes. */
private val RoomGlass = Color(0x59000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(
    state: GameState,
    playWake: Boolean,
    onWakePlayed: () -> Unit,
    hasActivityPermission: Boolean,
    stepSensorAvailable: Boolean,
    onRequestPermission: () -> Unit,
    onFeed: suspend (String) -> Boolean,
    onRevive: suspend () -> Unit,
    onPlay: () -> Unit,
    onRestart: () -> Unit,
) {
    val c = PetTheme.colors
    val scope = rememberCoroutineScope()
    var token by remember { mutableIntStateOf(0) }
    var action by remember { mutableStateOf(if (playWake) SpriteAction(SpriteKind.WAKE, 0) else null) }
    var showPantry by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { if (playWake) onWakePlayed() }

    val hungry = state.pet.hunger < PetRules.HUNGRY_THRESHOLD
    fun play(kind: SpriteKind) { action = SpriteAction(kind, ++token) }

    Box(Modifier.fillMaxSize()) {
        // The room Pompi lives in, full bleed, with scrims so the white figures stay readable.
        RoomBackground(Catalog.room(state.selectedRoom), Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.45f), 0.35f to Color.Transparent, 0.7f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.35f)),
            ),
        )

        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))
            // Headline figure: large on the backdrop, the secondary stats are tiles beneath it.
            Column(Modifier.fillMaxWidth().materialize(0)) {
                Text(
                    formatInt(state.todaySteps),
                    style = MaterialTheme.typography.displayLarge,
                    color = OnRoom,
                )
                Text(
                    stringResource(R.string.steps_today),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnRoom.copy(alpha = 0.8f),
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().materialize(1), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(Modifier.weight(0.9f)) { IconValue(R.drawable.coinimage, formatInt(state.coins), color = OnRoom) }
                StatChip(Modifier.weight(1.2f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        IconValue(R.drawable.rice, "${state.pet.hunger}%", color = OnRoom)
                        HungerBar(state.pet.hunger)
                    }
                }
                StatChip(Modifier.weight(1.1f)) { IconValue(R.drawable.heart, formatInt(state.pet.lives), color = OnRoom) }
            }

            if (!hasActivityPermission || !stepSensorAvailable) {
                Spacer(Modifier.height(12.dp))
                Banner(
                    text = stringResource(if (!stepSensorAvailable) R.string.no_step_sensor else R.string.permission_banner),
                    action = if (stepSensorAvailable) stringResource(R.string.permission_allow) else null,
                    onAction = onRequestPermission,
                )
            }

            Spacer(Modifier.weight(1f))
            if (!state.pet.isDead) {
                PetSprite(
                    hungry = hungry,
                    action = action,
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .aspectRatio(600f / 650f)
                        .pressable(role = androidx.compose.ui.semantics.Role.Button) { if (!hungry) play(SpriteKind.DANCE) },
                )
            }
            Spacer(Modifier.weight(0.5f))

            val pantryCount = state.pantry.values.sum()
            Row(Modifier.materialize(2), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier
                        .glass(CircleShape, RoomGlass)
                        .pressable { showPantry = true }
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(stringResource(R.string.feed), style = MaterialTheme.typography.titleMedium, color = OnRoom)
                    if (pantryCount > 0) {
                        Text(
                            pantryCount.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = OnRoom,
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.22f)).padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    stringResource(R.string.play),
                    style = MaterialTheme.typography.titleMedium,
                    color = OnRoom,
                    modifier = Modifier
                        .glass(CircleShape, RoomGlass)
                        .pressable(onClick = onPlay)
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                )
            }
            Spacer(Modifier.height(TabBarInset + 8.dp))
            Spacer(Modifier.navigationBarsPadding())
        }

        if (state.pet.isDead) {
            DeadOverlay(
                coins = state.coins,
                cost = state.difficulty?.reviveCost ?: 0,
                onRevive = { scope.launch { onRevive() } },
                onRestart = onRestart,
            )
        }
    }

    if (showPantry) {
        ModalBottomSheet(
            onDismissRequest = { showPantry = false },
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.solid.copy(alpha = 0.96f),
            contentColor = c.text,
        ) {
            Pantry(state) { foodId ->
                scope.launch {
                    if (onFeed(foodId)) {
                        showPantry = false
                        play(SpriteKind.FEED)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier.glass(RoundedCornerShape(20.dp), RoomGlass).padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) { content() }
}

@Composable
private fun HungerBar(hunger: Int) {
    val c = PetTheme.colors
    val color = when {
        hunger <= PetRules.REMINDER_THRESHOLD -> c.negative
        hunger < PetRules.HUNGRY_THRESHOLD -> Color.White.copy(alpha = 0.85f)
        else -> c.positive
    }
    Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f))) {
        Box(Modifier.fillMaxWidth(hunger / 100f).height(4.dp).clip(CircleShape).background(color))
    }
}

@Composable
private fun Banner(text: String, action: String?, onAction: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().glass(RoundedCornerShape(20.dp), RoomGlass).padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = OnRoom, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = PetTheme.colors.solid,
                modifier = Modifier.clip(CircleShape).background(Color.White).pressable { onAction() }.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun Pantry(state: GameState, onEat: (String) -> Unit) {
    val c = PetTheme.colors
    val owned = Catalog.foods.filter { (state.pantry[it.id] ?: 0) > 0 }
    val full = state.pet.hunger >= 100
    Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Text(stringResource(R.string.inventory), style = MaterialTheme.typography.titleLarge, color = c.text)
        if (full && owned.isNotEmpty()) {
            Text(stringResource(R.string.pantry_full), style = MaterialTheme.typography.bodyMedium, color = c.textMuted, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(12.dp))
        if (owned.isEmpty()) {
            Text(
                stringResource(R.string.inventory_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = c.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(((owned.size + 2) / 3 * 128).coerceAtMost(384).dp),
            ) {
                items(owned, key = { it.id }) { food ->
                    Column(
                        Modifier.fillMaxWidth().graphicsLayer { alpha = if (full) 0.45f else 1f }.panel(RoundedCornerShape(18.dp)).pressable(enabled = !full) { onEat(food.id) }.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        FoodImage(food.image, Modifier.size(64.dp))
                        Text(stringResource(food.name), style = MaterialTheme.typography.labelMedium, color = c.text, maxLines = 1)
                        Text(
                            "+${food.fullness} · ×${state.pantry[food.id]}",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.positive,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeadOverlay(coins: Int, cost: Int, onRevive: () -> Unit, onRestart: () -> Unit) {
    val affordable = coins >= cost
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(painterResource(R.drawable.skull), null, Modifier.size(112.dp))
            Text(stringResource(R.string.pet_dead_title), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(stringResource(R.string.pet_dead_body), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (affordable) 1f else 0.35f))
                    .pressable(enabled = affordable, onClick = onRevive)
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(stringResource(R.string.revive), style = MaterialTheme.typography.titleMedium, color = Color(0xFF12131A))
                IconValue(R.drawable.coinimage, formatInt(cost), iconSize = 18.dp, color = Color(0xFF12131A))
            }
            if (!affordable) {
                Text(
                    stringResource(R.string.revive_not_enough, formatInt(cost)),
                    style = MaterialTheme.typography.labelMedium,
                    color = PetTheme.colors.negative,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                stringResource(R.string.start_over),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.clip(CircleShape).pressable { onRestart() }.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}
