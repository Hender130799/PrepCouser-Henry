package com.calisthenia.feature.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.calisthenia.core.designsystem.CalisteniaTheme
import com.calisthenia.core.model.WorkoutExercise
import com.calisthenia.core.model.WorkoutPlan
import com.calisthenia.core.model.WorkoutSession
import com.calisthenia.core.model.label
import com.calisthenia.core.ui.LocalSpacing
import com.calisthenia.core.ui.components.CalisteniaScaffold
import com.calisthenia.core.ui.components.SectionCard
import com.calisthenia.core.ui.components.Tag
import com.calisthenia.feature.workouts.R
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun WorkoutsRoute(
    onBack: () -> Unit,
    viewModel: WorkoutsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutsScreen(
        state = state,
        onBack = onBack,
        onRegenerate = viewModel::regeneratePlan,
    )
}

@Composable
fun WorkoutsScreen(
    state: WorkoutsUiState,
    onBack: () -> Unit,
    onRegenerate: () -> Unit,
) {
    CalisteniaTheme {
        val spacing = LocalSpacing.current
        CalisteniaScaffold(
            title = stringResource(id = R.string.workouts_title),
            onNavigateBack = onBack,
            topBarActions = {
                IconButton(onClick = onRegenerate, enabled = !state.isRefreshing && !state.isLoading) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(id = R.string.workouts_action_refresh))
                }
            },
        ) { paddingValues ->
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
                            text = stringResource(id = R.string.workouts_loading),
                            modifier = Modifier.padding(top = spacing.small),
                        )
                    }
                }

                state.plan == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(spacing.large),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(id = R.string.workouts_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(id = R.string.workouts_empty_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = spacing.small),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        verticalArrangement = Arrangement.spacedBy(spacing.large),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = spacing.large,
                            end = spacing.large,
                            top = spacing.large,
                            bottom = spacing.xLarge,
                        ),
                    ) {
                        item {
                            PlanSummaryCard(plan = state.plan, isRefreshing = state.isRefreshing, onRefresh = onRegenerate)
                        }

                        if (state.errorMessage != null) {
                            item {
                                Text(
                                    text = state.errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        items(state.plan.sessions) { session ->
                            SessionCard(session = session)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanSummaryCard(plan: WorkoutPlan, isRefreshing: Boolean, onRefresh: () -> Unit) {
    val spacing = LocalSpacing.current
    SectionCard(
        title = plan.name,
        subtitle = stringResource(id = R.string.workouts_summary_subtitle, plan.level.label),
        onRefresh = if (isRefreshing) null else onRefresh,
    ) {
        Text(
            text = stringResource(id = R.string.workouts_summary_sessions, plan.sessions.size),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(id = R.string.workouts_summary_tip),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.small),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Tag(text = stringResource(id = R.string.workouts_tag_focus, plan.focus.label))
            Tag(text = stringResource(id = R.string.workouts_tag_updated, formatDate(plan.lastUpdated)))
        }
    }
}

@Composable
private fun SessionCard(session: WorkoutSession) {
    val spacing = LocalSpacing.current
    SectionCard(
        title = stringResource(id = R.string.workouts_session_title, session.dayOfWeek),
        subtitle = session.notes,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            session.exercises.forEach { exercise ->
                ExerciseRow(exercise = exercise)
            }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: WorkoutExercise) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        AsyncImage(
            model = exercise.mediaUrl,
            contentDescription = exercise.name,
            modifier = Modifier
                .height(72.dp)
                .weight(0.3f),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_workout_placeholder),
            error = painterResource(id = R.drawable.ic_workout_placeholder),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatExercisePrescription(exercise),
                style = MaterialTheme.typography.bodyMedium,
            )
            val rest = exercise.restSeconds
            Text(
                text = stringResource(id = R.string.workouts_rest_label, rest),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatExercisePrescription(exercise: WorkoutExercise): String {
    val sets = exercise.sets
    val repsText = exercise.reps?.let { "x${it}" } ?: "x tiempo"
    val duration = exercise.durationSeconds?.let { " (${it}s)" } ?: ""
    return "$sets series $repsText$duration"
}

private fun formatDate(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val year = dateTime.year
    return "$day/$month/$year"
}
