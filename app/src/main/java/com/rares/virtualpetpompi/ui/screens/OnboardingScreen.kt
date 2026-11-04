package com.rares.virtualpetpompi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rares.virtualpetpompi.R
import com.rares.virtualpetpompi.domain.Difficulty
import com.rares.virtualpetpompi.ui.components.PetSprite
import com.rares.virtualpetpompi.ui.components.SpriteAction
import com.rares.virtualpetpompi.ui.components.SpriteKind
import com.rares.virtualpetpompi.ui.components.materialize
import com.rares.virtualpetpompi.ui.components.panel
import com.rares.virtualpetpompi.ui.components.pressable
import com.rares.virtualpetpompi.ui.theme.PetTheme

/** First run: pick a difficulty. Choosing one also asks for the permissions the game needs. */
@Composable
fun OnboardingScreen(onChoose: (Difficulty) -> Unit) {
    val c = PetTheme.colors
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        PetSprite(hungry = false, action = SpriteAction(SpriteKind.WAKE, 0), modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(600f / 650f))
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            color = c.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.materialize(0),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.onboarding_body),
            style = MaterialTheme.typography.bodyLarge,
            color = c.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.materialize(1),
        )
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DifficultyCard(R.string.difficulty_normal, R.string.difficulty_normal_desc, Modifier.materialize(2)) { onChoose(Difficulty.NORMAL) }
            DifficultyCard(R.string.difficulty_easy, R.string.difficulty_easy_desc, Modifier.materialize(3)) { onChoose(Difficulty.EASY) }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.onboarding_permission_note),
            style = MaterialTheme.typography.labelMedium,
            color = c.textMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(0.6f))
    }
}

@Composable
private fun DifficultyCard(title: Int, body: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = PetTheme.colors
    Column(
        modifier.fillMaxWidth().panel(RoundedCornerShape(24.dp), strong = true).pressable(onClick = onClick).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge, color = c.text)
        Text(stringResource(body), style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
    }
}
