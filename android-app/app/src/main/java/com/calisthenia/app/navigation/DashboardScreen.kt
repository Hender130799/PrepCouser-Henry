package com.calisthenia.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calisthenia.app.R

@Composable
fun DashboardRoute(
    onNavigateToWorkouts: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    DashboardScreen(
        onNavigateToWorkouts = onNavigateToWorkouts,
        onNavigateToNutrition = onNavigateToNutrition,
        onNavigateToProgress = onNavigateToProgress,
        onNavigateToSettings = onNavigateToSettings,
    )
}

@Composable
fun DashboardScreen(
    onNavigateToWorkouts: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(id = R.string.dashboard_title))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNavigateToWorkouts) {
            Text(text = stringResource(id = R.string.dashboard_goto_workouts))
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNavigateToNutrition) {
            Text(text = stringResource(id = R.string.dashboard_goto_nutrition))
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNavigateToProgress) {
            Text(text = stringResource(id = R.string.dashboard_goto_progress))
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNavigateToSettings) {
            Text(text = stringResource(id = R.string.dashboard_goto_settings))
        }
    }
}
