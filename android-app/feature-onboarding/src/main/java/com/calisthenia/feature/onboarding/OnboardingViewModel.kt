package com.calisthenia.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calisthenia.core.model.DietaryPreference
import com.calisthenia.core.model.ExperienceLevel
import com.calisthenia.core.model.TrainingGoal
import com.calisthenia.core.model.UserProfile
import com.calisthenia.domain.repository.WorkoutGenerationRequest
import com.calisthenia.domain.usecase.GenerateWorkoutPlanUseCase
import com.calisthenia.domain.usecase.GetUserProfileUseCase
import com.calisthenia.domain.usecase.UpsertUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getUserProfile: GetUserProfileUseCase,
    private val upsertUserProfile: UpsertUserProfileUseCase,
    private val generateWorkoutPlan: GenerateWorkoutPlanUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var currentProfileId: String? = null

    init {
        viewModelScope.launch {
            getUserProfile().collect { profile ->
                currentProfileId = profile?.id
                if (profile != null) {
                    _uiState.update { state ->
                        state.copy(
                            name = profile.displayName,
                            weight = profile.weightKg.toString(),
                            height = profile.heightCm.toString(),
                            experienceLevel = profile.experienceLevel,
                            goal = profile.primaryGoal,
                            dietaryPreferences = profile.dietaryPreferences,
                            remindersEnabled = profile.remindersEnabled,
                        )
                    }
                }
            }
        }
    }

    fun onNameChanged(value: String) = updateState { copy(name = value, errorMessage = null) }

    fun onWeightChanged(value: String) = updateState { copy(weight = value, errorMessage = null) }

    fun onHeightChanged(value: String) = updateState { copy(height = value, errorMessage = null) }

    fun onGoalSelected(goal: TrainingGoal) = updateState { copy(goal = goal, errorMessage = null) }

    fun onExperienceSelected(level: ExperienceLevel) = updateState { copy(experienceLevel = level, errorMessage = null) }

    fun onDietaryPreferenceToggled(preference: DietaryPreference) = updateState {
        val updated = when (preference) {
            DietaryPreference.NONE -> setOf(DietaryPreference.NONE)
            else -> {
                val current = dietaryPreferences.toMutableSet().apply { remove(DietaryPreference.NONE) }
                if (current.contains(preference)) current.remove(preference) else current.add(preference)
                current
            }
        }
        copy(dietaryPreferences = updated)
    }

    fun onSessionsChanged(sessions: Int) = updateState { copy(sessionsPerWeek = sessions) }

    fun onRemindersChanged(enabled: Boolean) = updateState { copy(remindersEnabled = enabled) }

    fun submitProfile() {
        val state = _uiState.value
        val weight = state.weight.replace(',', '.').toDoubleOrNull()
        val height = state.height.replace(',', '.').toDoubleOrNull()
        val level = state.experienceLevel
        val goal = state.goal

        if (state.name.isBlank() || weight == null || height == null || level == null || goal == null) {
            _uiState.update { it.copy(errorMessage = ERROR_INCOMPLETE) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val profileId = currentProfileId ?: UUID.randomUUID().toString()
            val dietaryPrefs = state.dietaryPreferences.ifEmpty { setOf(DietaryPreference.NONE) }

            val profile = UserProfile(
                id = profileId,
                displayName = state.name.trim(),
                email = null,
                weightKg = weight,
                heightCm = height,
                birthDate = null,
                experienceLevel = level,
                primaryGoal = goal,
                dietaryPreferences = dietaryPrefs,
                remindersEnabled = state.remindersEnabled,
            )

            try {
                upsertUserProfile(profile)
                generateWorkoutPlan(
                    WorkoutGenerationRequest(
                        userId = profile.id,
                        goal = goal.name.lowercase(),
                        sessionsPerWeek = state.sessionsPerWeek,
                    ),
                )
                _uiState.update { it.copy(isLoading = false, completed = true) }
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = ERROR_UNKNOWN) }
            }
        }
    }

    private fun updateState(block: OnboardingUiState.() -> OnboardingUiState) {
        _uiState.update(block)
    }

    private companion object {
        const val ERROR_INCOMPLETE = "Revisa tu informaci?n. Peso, estatura, objetivo y nivel son obligatorios."
        const val ERROR_UNKNOWN = "No pudimos guardar tu perfil. Intenta nuevamente."
    }
}

data class OnboardingUiState(
    val name: String = "",
    val weight: String = "",
    val height: String = "",
    val experienceLevel: ExperienceLevel? = null,
    val goal: TrainingGoal? = null,
    val dietaryPreferences: Set<DietaryPreference> = emptySet(),
    val sessionsPerWeek: Int = 4,
    val remindersEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val completed: Boolean = false,
)
