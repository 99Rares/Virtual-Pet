package com.rares.virtualpetpompi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.R
import com.rares.virtualpetpompi.data.BuyResult
import com.rares.virtualpetpompi.data.GameState
import com.rares.virtualpetpompi.domain.Catalog
import com.rares.virtualpetpompi.domain.Food
import com.rares.virtualpetpompi.domain.FoodCategory
import com.rares.virtualpetpompi.ui.components.FoodImage
import com.rares.virtualpetpompi.ui.components.IconValue
import com.rares.virtualpetpompi.ui.components.ScreenColumn
import com.rares.virtualpetpompi.ui.components.Segmented
import com.rares.virtualpetpompi.ui.components.formatInt
import com.rares.virtualpetpompi.ui.components.glass
import com.rares.virtualpetpompi.ui.components.materialize
import com.rares.virtualpetpompi.ui.components.panel
import com.rares.virtualpetpompi.ui.components.pressable
import com.rares.virtualpetpompi.ui.theme.EaseOut
import com.rares.virtualpetpompi.ui.theme.PetTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(
    state: GameState,
    onBuyFood: suspend (String) -> BuyResult,
    onBuyRoom: suspend (String) -> BuyResult,
    onSelectRoom: (String) -> Unit,
) {
    val c = PetTheme.colors
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var section by rememberSaveable { mutableIntStateOf(0) }
    var toast by remember { mutableStateOf<Toast?>(null) }
    val notEnough = stringResource(R.string.not_enough_coins)
    val bought = stringResource(R.string.bought)

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1_600)
            toast = null
        }
    }

    Box {
        ScreenColumn(stringResource(R.string.tab_shop)) {
            Column(Modifier.materialize(1)) {
                Text(formatInt(state.coins), style = MaterialTheme.typography.displayMedium, color = c.text)
                Text(stringResource(R.string.coins), style = MaterialTheme.typography.bodyLarge, color = c.textMuted)
            }
            Spacer(Modifier.height(20.dp))
            Segmented(
                options = listOf(stringResource(R.string.shop_food), stringResource(R.string.shop_rooms)),
                selected = section,
                onSelect = { section = it },
                modifier = Modifier.fillMaxWidth().materialize(2),
            )
            Spacer(Modifier.height(20.dp))
            if (section == 0) {
                FoodSection(state) { food ->
                    scope.launch {
                        when (onBuyFood(food.id)) {
                            BuyResult.OK -> { haptics.performHapticFeedback(HapticFeedbackType.Confirm); toast = Toast("+1 ${'·'}", true) }
                            else -> { haptics.performHapticFeedback(HapticFeedbackType.Reject); toast = Toast(notEnough, false) }
                        }
                    }
                }
            } else {
                RoomSection(state, onBuyRoom = { id ->
                    scope.launch {
                        if (onBuyRoom(id) == BuyResult.NOT_ENOUGH_COINS) {
                            haptics.performHapticFeedback(HapticFeedbackType.Reject)
                            toast = Toast(notEnough, false)
                        } else haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    }
                }, onSelect = onSelectRoom)
            }
        }

        // Transient status for a purchase; it appears and leaves, so it fades instead of travelling.
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.TopCenter) {
            AnimatedVisibility(toast != null, enter = fadeIn(tween(160, easing = EaseOut)), exit = fadeOut(tween(120))) {
                val t = toast
                if (t != null) {
                    Text(
                        t.text,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (t.ok) c.positive else c.negative,
                        modifier = Modifier.glass(CircleShape).padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

private data class Toast(val text: String, val ok: Boolean)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodSection(state: GameState, onBuy: (Food) -> Unit) {
    val c = PetTheme.colors
    FoodCategory.entries.forEachIndexed { sectionIndex, category ->
        Text(
            stringResource(
                when (category) {
                    FoodCategory.BREAKFAST -> R.string.category_breakfast
                    FoodCategory.DINNER -> R.string.category_dinner
                    FoodCategory.SNACKS -> R.string.category_snacks
                },
            ),
            style = MaterialTheme.typography.titleLarge,
            color = c.text,
            modifier = Modifier.padding(top = if (sectionIndex == 0) 0.dp else 24.dp, bottom = 12.dp),
        )
        val foods = Catalog.foods.filter { it.category == category }
        // Two per row; FlowRow keeps this inside the page's single scroll.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2,
        ) {
            foods.forEachIndexed { i, food ->
                FoodCard(food, state.pantry[food.id] ?: 0, affordable = state.coins >= food.price, Modifier.weight(1f).materialize(sectionIndex * 3 + i)) { onBuy(food) }
            }
            if (foods.size % 2 == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun FoodCard(food: Food, owned: Int, affordable: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = PetTheme.colors
    Column(
        modifier
            .panel(RoundedCornerShape(24.dp))
            .pressable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            FoodImage(food.image, Modifier.size(104.dp))
            if (owned > 0) {
                Text(
                    "×$owned",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.text,
                    modifier = Modifier.clip(CircleShape).background(c.solid).padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(food.name), style = MaterialTheme.typography.titleMedium, color = c.text, maxLines = 1)
        Text("+${food.fullness}", style = MaterialTheme.typography.labelMedium, color = c.positive)
        Spacer(Modifier.height(10.dp))
        PriceTag(food.price, affordable)
    }
}

@Composable
private fun PriceTag(price: Int, affordable: Boolean) {
    val c = PetTheme.colors
    IconValue(
        R.drawable.coinimage,
        formatInt(price),
        modifier = Modifier.clip(CircleShape).background(c.panelStrong).padding(horizontal = 12.dp, vertical = 6.dp),
        iconSize = 18.dp,
        color = if (affordable) c.text else c.negative,
    )
}

@Composable
private fun RoomSection(state: GameState, onBuyRoom: (String) -> Unit, onSelect: (String) -> Unit) {
    val c = PetTheme.colors
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 2,
    ) {
        Catalog.rooms.forEachIndexed { i, room ->
            val owned = room.id in state.ownedRooms
            val selected = room.id == state.selectedRoom
            Column(
                Modifier
                    .weight(1f)
                    .materialize(i)
                    .panel(RoundedCornerShape(24.dp))
                    .pressable { if (owned) onSelect(room.id) else onBuyRoom(room.id) }
                    .padding(10.dp),
            ) {
                Image(
                    painterResource(room.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.75f).clip(RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(room.name), style = MaterialTheme.typography.titleMedium, color = c.text)
                    when {
                        selected -> Text(stringResource(R.string.equipped), style = MaterialTheme.typography.labelMedium, color = c.positive)
                        owned -> Text(stringResource(R.string.equip), style = MaterialTheme.typography.labelMedium, color = c.textMuted)
                        else -> PriceTag(room.price, state.coins >= room.price)
                    }
                }
            }
        }
    }
}
