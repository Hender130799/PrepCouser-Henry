package com.calisthenia.app.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.calisthenia.app.R
import com.calisthenia.core.designsystem.CalisteniaTheme
import com.calisthenia.core.model.Recipe
import com.calisthenia.core.model.WorkoutSession
import com.calisthenia.core.ui.LocalSpacing
import com.calisthenia.core.ui.components.CalisteniaScaffold
import com.calisthenia.core.ui.components.MetricChip
import com.calisthenia.core.ui.components.SectionCard
import com.calisthenia.core.ui.components.Tag

@Composable
fun DashboardRoute(
    onNavigateToWorkouts: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        onNavigateToWorkouts = onNavigateToWorkouts,
        onNavigateToNutrition = onNavigateToNutrition,
        onNavigateToProgress = onNavigateToProgress,
        onNavigateToSettings = onNavigateToSettings,
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    CalisteniaTheme {
        val spacing = LocalSpacing.current
        CalisteniaScaffold(title = stringResource(id = R.string.dashboard_title)) { paddingValues ->
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(id = R.string.dashboard_loading),
                            modifier = Modifier.padding(top = spacing.small),
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(
                            start = spacing.large,
                            end = spacing.large,
                            top = spacing.large,
                            bottom = spacing.xLarge,
                        ),
                        verticalArrangement = Arrangement.spacedBy(spacing.large),
                    ) {
                        item { GreetingSection(name = state.userName, weeklySessions = state.weeklySessions) }

                        state.nextSession?.let { session ->
                            item { NextSessionCard(session = session, onStart = onNavigateToWorkouts) }
                        }

                        state.featuredRecipe?.let { recipe ->
                            item { FeaturedRecipeCard(recipe = recipe, onOpen = onNavigateToNutrition) }
                        }

                        item { ProgressSummaryCard(summary = state.progressSummary, onOpenProgress = onNavigateToProgress) }

                        item {
                            QuickActionsSection(
                                onNavigateToWorkouts = onNavigateToWorkouts,
                                onNavigateToNutrition = onNavigateToNutrition,
                                onNavigateToProgress = onNavigateToProgress,
                                onNavigateToSettings = onNavigateToSettings,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingSection(name: String, weeklySessions: Int) {
    val spacing = LocalSpacing.current
    SectionCard(title = stringResource(id = R.string.dashboard_greeting_title, name.ifBlank { stringResource(id = R.string.dashboard_generic_name) })) {
        Text(
            text = stringResource(id = R.string.dashboard_greeting_body, weeklySessions),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NextSessionCard(session: WorkoutSession, onStart: () -> Unit) {
    val spacing = LocalSpacing.current
    SectionCard(
        title = stringResource(id = R.string.dashboard_next_session_title, session.dayOfWeek),
        subtitle = session.notes,
        onRefresh = null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            session.exercises.take(3).forEach { exercise ->
                Text(
                    text = "? ${exercise.name} ? ${exercise.sets}x${exercise.reps ?: "tiempo"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (session.exercises.size > 3) {
                Text(
                    text = stringResource(id = R.string.dashboard_next_session_more, session.exercises.size - 3),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onStart, modifier = Modifier.padding(top = spacing.small)) {
                Text(text = stringResource(id = R.string.dashboard_next_session_cta))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeaturedRecipeCard(recipe: Recipe, onOpen: () -> Unit) {
    val spacing = LocalSpacing.current
    SectionCard(
        title = stringResource(id = R.string.dashboard_recipe_title, recipe.title),
        subtitle = recipe.description,
    ) {
        AsyncImage(
            model = recipe.imageUrl,
            contentDescription = recipe.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.medium),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_recipe_placeholder),
            error = painterResource(id = R.drawable.ic_recipe_placeholder),
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
            Tag(text = recipe.mealType.label)
            Tag(text = stringResource(id = R.string.dashboard_recipe_time, recipe.prepTimeMinutes + recipe.cookTimeMinutes))
            Tag(text = stringResource(id = R.string.dashboard_recipe_servings, recipe.servings))
        }

        Button(onClick = onOpen, modifier = Modifier.padding(top = spacing.medium)) {
            Text(text = stringResource(id = R.string.dashboard_recipe_cta))
        }
    }
}

@Composable
private fun ProgressSummaryCard(summary: ProgressSummary, onOpenProgress: () -> Unit) {
    val spacing = LocalSpacing.current
    SectionCard(title = stringResource(id = R.string.dashboard_progress_title)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
            MetricChip(
                label = stringResource(id = R.string.dashboard_progress_weight),
                value = summary.latestWeight?.let { "${String.format("%.1f", it)} kg" } ?: "--",
            )
            MetricChip(
                label = stringResource(id = R.string.dashboard_progress_delta),
                value = summary.weightDelta?.let { delta ->
                    val sign = if (delta > 0) "+" else ""
                    "$sign${String.format("%.1f", delta)} kg"
                } ?: "--",
            )
            MetricChip(
                label = stringResource(id = R.string.dashboard_progress_sessions),
                value = summary.activeDays.toString(),
            )
        }
        OutlinedButton(onClick = onOpenProgress, modifier = Modifier.padding(top = spacing.medium)) {
            Text(text = stringResource(id = R.string.dashboard_progress_cta))
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToWorkouts: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val spacing = LocalSpacing.current
    SectionCard(title = stringResource(id = R.string.dashboard_quick_actions)) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            Button(onClick = onNavigateToWorkouts, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(id = R.string.dashboard_goto_workouts))
            }
            Button(onClick = onNavigateToNutrition, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(id = R.string.dashboard_goto_nutrition))
            }
            Button(onClick = onNavigateToProgress, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(id = R.string.dashboard_goto_progress))
            }
            OutlinedButton(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(id = R.string.dashboard_goto_settings))
            }
        }
    }
}
