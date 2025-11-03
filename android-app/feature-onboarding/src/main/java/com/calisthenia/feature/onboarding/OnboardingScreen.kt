package com.calisthenia.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calisthenia.core.model.DietaryPreference
import com.calisthenia.core.model.ExperienceLevel
import com.calisthenia.core.model.TrainingGoal
import com.calisthenia.core.model.label
import com.calisthenia.core.designsystem.CalisteniaTheme
import com.calisthenia.core.ui.LocalSpacing
import com.calisthenia.core.ui.components.CalisteniaScaffold
import com.calisthenia.core.ui.components.FilterChipRow
import com.calisthenia.core.ui.components.SectionCard
import com.calisthenia.feature.onboarding.R

@Composable
fun OnboardingRoute(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.completed) {
        if (state.completed) onFinish()
    }

    OnboardingScreen(
        state = state,
        onNameChange = viewModel::onNameChanged,
        onWeightChange = viewModel::onWeightChanged,
        onHeightChange = viewModel::onHeightChanged,
        onGoalSelected = viewModel::onGoalSelected,
        onExperienceSelected = viewModel::onExperienceSelected,
        onPreferenceToggled = viewModel::onDietaryPreferenceToggled,
        onSessionsChanged = viewModel::onSessionsChanged,
        onRemindersChanged = viewModel::onRemindersChanged,
        onSubmit = viewModel::submitProfile,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onGoalSelected: (TrainingGoal) -> Unit,
    onExperienceSelected: (ExperienceLevel) -> Unit,
    onPreferenceToggled: (DietaryPreference) -> Unit,
    onSessionsChanged: (Int) -> Unit,
    onRemindersChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit,
) {
    CalisteniaTheme {
        val spacing = LocalSpacing.current
        val focusManager = LocalFocusManager.current
        val scrollState = rememberScrollState()

        CalisteniaScaffold(
            title = stringResource(id = R.string.onboarding_title),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = spacing.large, vertical = spacing.medium)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(spacing.large),
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_subtitle),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                )

                SectionCard(title = stringResource(id = R.string.onboarding_section_profile)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(id = R.string.onboarding_field_name)) },
                        singleLine = true,
                        supportingText = { Text(stringResource(id = R.string.onboarding_field_name_helper)) },
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.weight,
                        onValueChange = onWeightChange,
                        label = { Text(stringResource(id = R.string.onboarding_field_weight)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text(text = stringResource(id = R.string.onboarding_unit_kg)) },
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.height,
                        onValueChange = onHeightChange,
                        label = { Text(stringResource(id = R.string.onboarding_field_height)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text(text = stringResource(id = R.string.onboarding_unit_cm)) },
                    )
                }

                SectionCard(title = stringResource(id = R.string.onboarding_section_goals)) {
                    Text(
                        text = stringResource(id = R.string.onboarding_goal_prompt),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val goalOptions = TrainingGoal.entries.toList()
                    FilterChipRow(
                        items = goalOptions.map { it.label },
                        selected = state.goal?.let { setOf(it.label) } ?: emptySet(),
                        onSelectionChanged = { label ->
                            val selected = goalOptions.firstOrNull { it.label == label }
                            if (selected != null && selected != state.goal) {
                                onGoalSelected(selected)
                            }
                        },
                    )

                    Spacer(modifier = Modifier.padding(top = spacing.small))

                    Text(
                        text = stringResource(id = R.string.onboarding_experience_prompt),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val experienceOptions = ExperienceLevel.entries.toList()
                    FilterChipRow(
                        items = experienceOptions.map { it.label },
                        selected = state.experienceLevel?.let { setOf(it.label) } ?: emptySet(),
                        onSelectionChanged = { label ->
                            val level = experienceOptions.firstOrNull { it.label == label }
                            if (level != null && level != state.experienceLevel) {
                                onExperienceSelected(level)
                            }
                        },
                    )
                }

                SectionCard(title = stringResource(id = R.string.onboarding_section_plan)) {
                    Text(
                        text = stringResource(id = R.string.onboarding_sessions_title, state.sessionsPerWeek),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    )
                    Slider(
                        value = state.sessionsPerWeek.toFloat(),
                        onValueChange = { onSessionsChanged(it.toInt()) },
                        valueRange = 2f..6f,
                        steps = 3,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "2")
                        Text(text = "6")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.onboarding_reminder_title),
                                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = stringResource(id = R.string.onboarding_reminder_subtitle),
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = state.remindersEnabled, onCheckedChange = onRemindersChanged)
                    }
                }

                SectionCard(title = stringResource(id = R.string.onboarding_section_preferences)) {
                    Text(
                        text = stringResource(id = R.string.onboarding_preferences_hint),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val preferences = DietaryPreference.entries.toList()
                    FilterChipRow(
                        items = preferences.map { it.label },
                        selected = state.dietaryPreferences.map { it.label }.toSet(),
                        onSelectionChanged = { label ->
                            preferences.firstOrNull { it.label == label }?.let(onPreferenceToggled)
                        },
                    )

                    TextButton(onClick = { onPreferenceToggled(DietaryPreference.NONE) }) {
                        Text(text = stringResource(id = R.string.onboarding_preference_clear))
                    }
                }

                AnimatedVisibility(visible = state.errorMessage != null) {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing.large),
                    onClick = {
                        focusManager.clearFocus()
                        onSubmit()
                    },
                    enabled = !state.isLoading,
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp), strokeWidth = 2.dp)
                    }
                    Text(text = stringResource(id = R.string.onboarding_continue))
                }
            }
        }
    }
}
