package com.calisthenia.feature.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calisthenia.core.model.TrainingGoal
import com.calisthenia.core.model.UserProfile
import com.calisthenia.core.model.WorkoutPlan
import com.calisthenia.domain.repository.WorkoutGenerationRequest
import com.calisthenia.domain.usecase.GenerateWorkoutPlanUseCase
import com.calisthenia.domain.usecase.GetUserProfileUseCase
import com.calisthenia.domain.usecase.ObserveWorkoutPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutsViewModel @Inject constructor(
    private val observeWorkoutPlan: ObserveWorkoutPlanUseCase,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val getUserProfile: GetUserProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutsUiState())
    val uiState: StateFlow<WorkoutsUiState> = _uiState.asStateFlow()

    private var cachedProfile: UserProfile? = null

    init {
        viewModelScope.launch {
            observeWorkoutPlan().collect { plan ->
                _uiState.update { state ->
                    state.copy(
                        plan = plan,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            }
        }

        viewModelScope.launch {
            getUserProfile()
                .filterNotNull()
                .collect { profile ->
                    cachedProfile = profile
                    if (_uiState.value.plan == null && !_uiState.value.isLoading) {
                        regeneratePlan()
                    }
                }
        }

        // attempt to load default plan if nothing exists
        regeneratePlan()
    }

    fun regeneratePlan() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

        viewModelScope.launch {
            val profile = cachedProfile
            val planSessions = _uiState.value.plan?.sessions?.size ?: DEFAULT_SESSIONS
            val request = WorkoutGenerationRequest(
                userId = profile?.id ?: DEFAULT_USER_ID,
                goal = (profile?.primaryGoal ?: TrainingGoal.HYPERTROPHY).name.lowercase(),
                sessionsPerWeek = planSessions,
            )
            try {
                generateWorkoutPlanUseCase(request)
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoading = false,
                        errorMessage = error.message ?: GENERIC_ERROR,
                    )
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_USER_ID = "demo-user"
        private const val DEFAULT_SESSIONS = 4
        private const val GENERIC_ERROR = "No pudimos actualizar tu plan. Int?ntalo nuevamente."
    }
}

data class WorkoutsUiState(
    val plan: WorkoutPlan? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
