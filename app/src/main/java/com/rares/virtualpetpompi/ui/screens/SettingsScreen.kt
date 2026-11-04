package com.rares.virtualpetpompi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.BuildConfig
import com.rares.virtualpetpompi.R
import com.rares.virtualpetpompi.data.GameState
import com.rares.virtualpetpompi.domain.Difficulty
import com.rares.virtualpetpompi.ui.components.ScreenColumn
import com.rares.virtualpetpompi.ui.components.formatInt
import com.rares.virtualpetpompi.ui.components.materialize
import com.rares.virtualpetpompi.ui.components.panel
import com.rares.virtualpetpompi.ui.components.pressable
import com.rares.virtualpetpompi.ui.theme.PetTheme

@Composable
fun SettingsScreen(state: GameState, onGoalChange: (Int) -> Unit, onRestart: () -> Unit) {
    val c = PetTheme.colors
    var goal by remember(state.dailyGoal) { mutableFloatStateOf(state.dailyGoal.toFloat()) }
    var confirmRestart by remember { mutableStateOf(false) }

    ScreenColumn(stringResource(R.string.tab_settings)) {
        Section(stringResource(R.string.settings_goal), 1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(formatInt(goal.toInt()), style = MaterialTheme.typography.titleLarge, color = c.text)
                Text(stringResource(R.string.settings_goal_desc), style = MaterialTheme.typography.labelMedium, color = c.textMuted, modifier = Modifier.weight(1f).padding(start = 16.dp))
            }
            Slider(
                value = goal,
                onValueChange = { goal = (it / 500f).toInt() * 500f },
                onValueChangeFinished = { onGoalChange(goal.toInt()) },
                valueRange = 2_000f..30_000f,
                
                colors = SliderDefaults.colors(thumbColor = c.text, activeTrackColor = c.accent, inactiveTrackColor = c.panelStrong),
            )
        }

        Section(stringResource(R.string.settings_game), 2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.settings_difficulty), style = MaterialTheme.typography.bodyLarge, color = c.text)
                Text(
                    stringResource(if (state.difficulty == Difficulty.EASY) R.string.difficulty_easy else R.string.difficulty_normal),
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.textMuted,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.settings_restart_desc), style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.settings_restart),
                style = MaterialTheme.typography.labelLarge,
                color = c.negative,
                modifier = Modifier.clip(CircleShape).background(c.panelStrong).pressable { confirmRestart = true }.padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }

        Section(stringResource(R.string.settings_privacy), 3) {
            Text(stringResource(R.string.settings_privacy_body), style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
        }

        Section(stringResource(R.string.settings_about), 4) {
            Text(stringResource(R.string.settings_credits), style = MaterialTheme.typography.bodyLarge, color = c.text)
            Text(stringResource(R.string.version_format, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
        }
    }

    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            containerColor = c.solid,
            title = { Text(stringResource(R.string.settings_restart_confirm_title), color = c.text) },
            text = { Text(stringResource(R.string.settings_restart_confirm_body), color = c.textMuted) },
            confirmButton = {
                TextButton({ confirmRestart = false; onRestart() }) { Text(stringResource(R.string.confirm), color = c.negative) }
            },
            dismissButton = {
                TextButton({ confirmRestart = false }) { Text(stringResource(R.string.cancel), color = c.text) }
            },
        )
    }
}

@Composable
private fun Section(title: String, index: Int, content: @Composable () -> Unit) {
    val c = PetTheme.colors
    Text(title, style = MaterialTheme.typography.labelSmall, color = c.textMuted, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = if (index == 1) 0.dp else 20.dp))
    Column(Modifier.fillMaxWidth().materialize(index).panel(RoundedCornerShape(22.dp)).padding(16.dp)) { content() }
}
