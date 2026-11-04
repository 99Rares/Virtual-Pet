package com.rares.virtualpetpompi.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.rares.virtualpetpompi.AppContainer
import com.rares.virtualpetpompi.PetViewModel
import com.rares.virtualpetpompi.R
import com.rares.virtualpetpompi.domain.Difficulty
import com.rares.virtualpetpompi.ui.components.GlassTabBar
import com.rares.virtualpetpompi.ui.components.Icon
import com.rares.virtualpetpompi.ui.components.LocalHazeState
import com.rares.virtualpetpompi.ui.components.TabItem
import com.rares.virtualpetpompi.ui.screens.OnboardingScreen
import com.rares.virtualpetpompi.ui.screens.PetScreen
import com.rares.virtualpetpompi.ui.screens.SettingsScreen
import com.rares.virtualpetpompi.ui.screens.ShopScreen
import com.rares.virtualpetpompi.ui.screens.StatsScreen
import com.rares.virtualpetpompi.ui.theme.Backdrop
import com.rares.virtualpetpompi.ui.theme.PetTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun AppRoot(container: AppContainer, vm: PetViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hazeState = rememberHazeState()
    var tab by rememberSaveable { mutableStateOf(0) }
    var hasActivity by remember { mutableStateOf(hasActivityPermission(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { hasActivity = hasActivityPermission(context) }

    // While the app is on screen: follow the step sensor live and keep the pet aged to now.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch { container.stepCounter.updates().sample(2_000).collect { container.repository.recordCounter(it) } }
            launch {
                while (true) {
                    container.repository.settle()
                    delay(30_000)
                }
            }
        }
    }

    var pendingDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasActivity = hasActivityPermission(context)
        pendingDifficulty?.let { vm.onboard(it) }
        pendingDifficulty = null
    }
    fun requestPermissions() {
        val wanted = buildList {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.launch(wanted.toTypedArray())
    }

    PetTheme {
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            Backdrop {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
                        val s = state
                        when {
                            s == null -> Unit
                            !s.isOnboarded -> OnboardingScreen { difficulty ->
                                pendingDifficulty = difficulty
                                requestPermissions()
                            }
                            else -> when (tab) {
                                0 -> PetScreen(
                                    state = s,
                                    playWake = !vm.wakePlayed,
                                    onWakePlayed = { vm.wakePlayed = true },
                                    hasActivityPermission = hasActivity,
                                    stepSensorAvailable = container.stepCounter.isAvailable,
                                    onRequestPermission = { requestPermissions() },
                                    onFeed = vm::feed,
                                    onRevive = { vm.revive() },
                                    onRestart = vm::restart,
                                )
                                1 -> ShopScreen(s, vm::buyFood, vm::buyRoom, vm::selectRoom)
                                2 -> StatsScreen(stats)
                                else -> SettingsScreen(s, vm::setDailyGoal, vm::restart)
                            }
                        }
                    }
                    if (state?.isOnboarded == true) {
                        GlassTabBar(
                            items = listOf(
                                TabItem(stringResource(R.string.tab_pet)) { Icon(Icons.Filled.Favorite, it) },
                                TabItem(stringResource(R.string.tab_shop)) { Icon(Icons.Filled.ShoppingCart, it) },
                                TabItem(stringResource(R.string.tab_stats)) { StatsIcon(it) },
                                TabItem(stringResource(R.string.tab_settings)) { Icon(Icons.Filled.Settings, it) },
                            ),
                            selected = tab,
                            onSelect = { tab = it },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

private fun hasActivityPermission(context: android.content.Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

/** Three bars; the core material icon pack has no chart glyph. */
@Composable
private fun StatsIcon(tint: androidx.compose.ui.graphics.Color) {
    androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
        val bar = size.width * 0.2f
        val gap = (size.width - bar * 3) / 2
        listOf(0.55f, 1f, 0.75f).forEachIndexed { i, h ->
            drawRoundRect(
                tint,
                topLeft = androidx.compose.ui.geometry.Offset(i * (bar + gap), size.height * (1 - h)),
                size = androidx.compose.ui.geometry.Size(bar, size.height * h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(bar / 2),
            )
        }
    }
}
