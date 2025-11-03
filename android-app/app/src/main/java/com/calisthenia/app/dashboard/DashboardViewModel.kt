package com.calisthenia.app.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calisthenia.core.model.ProgressEntry
import com.calisthenia.core.model.Recipe
import com.calisthenia.core.model.UserProfile
import com.calisthenia.core.model.WorkoutPlan
import com.calisthenia.core.model.WorkoutSession
import com.calisthenia.domain.repository.WorkoutGenerationRequest
import com.calisthenia.domain.usecase.GenerateWorkoutPlanUseCase
import com.calisthenia.domain.usecase.GetUserProfileUseCase
import com.calisthenia.domain.usecase.ObserveProgressUseCase
import com.calisthenia.domain.usecase.ObserveRecipesUseCase
import com.calisthenia.domain.usecase.ObserveWorkoutPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getUserProfile: GetUserProfileUseCase,
    private val observeWorkoutPlan: ObserveWorkoutPlanUseCase,
    private val generateWorkoutPlan: GenerateWorkoutPlanUseCase,
    private val observeRecipes: ObserveRecipesUseCase,
    private val observeProgress: ObserveProgressUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var cachedProfile: UserProfile? = null

    init {
        viewModelScope.launch {
            combine(
                getUserProfile(),
                observeWorkoutPlan(),
                observeRecipes(),
                observeProgress(),
            ) { profile, plan, recipes, progress ->
                DashboardData(profile, plan, recipes, progress)
            }.collect { data ->
                cachedProfile = data.profile ?: cachedProfile
                if (data.plan == null && data.profile != null) {
                    ensurePlan(data.profile, data.plan)
                }
                _uiState.update { state ->
                    state.copy(
                        userName = data.profile?.displayName.orEmpty(),
                        plan = data.plan,
                        nextSession = data.plan?.sessions?.firstOrNull(),
                        featuredRecipe = data.recipes.firstOrNull(),
                        weeklySessions = data.plan?.sessions?.size ?: 0,
                        progressSummary = buildProgressSummary(data.progress),
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun ensurePlan(profile: UserProfile, currentPlan: WorkoutPlan?) {
        if (currentPlan != null) return
        viewModelScope.launch {
            val request = WorkoutGenerationRequest(
                userId = profile.id,
                goal = profile.primaryGoal.name.lowercase(),
                sessionsPerWeek = DEFAULT_SESSIONS,
            )
            try {
                generateWorkoutPlan(request)
            } catch (_: Exception) {
                // ignore for dashboard; workouts screen manejar? errores
            }
        }
    }

    private fun buildProgressSummary(entries: List<ProgressEntry>): ProgressSummary {
        if (entries.isEmpty()) return ProgressSummary()
        val sorted = entries.sortedBy { it.timestamp }
        val first = sorted.first()
        val last = sorted.last()
        val delta = if (first.weightKg != null && last.weightKg != null) {
            last.weightKg - first.weightKg
        } else null
        val streak = sorted.groupBy { it.timestamp.dateKey() }.size
        return ProgressSummary(
            latestWeight = last.weightKg,
            weightDelta = delta,
            entriesLogged = entries.size,
            activeDays = streak,
        )
    }

    private fun kotlinx.datetime.Instant.dateKey(): String {
        val dateTime = toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return "${dateTime.year}-${dateTime.monthNumber}-${dateTime.dayOfMonth}"
    }

    data class DashboardData(
        val profile: UserProfile?,
        val plan: WorkoutPlan?,
        val recipes: List<Recipe>,
        val progress: List<ProgressEntry>,
    )

    companion object {
        private const val DEFAULT_SESSIONS = 4
    }
}

data class DashboardUiState(
    val userName: String = "",
    val plan: WorkoutPlan? = null,
    val nextSession: WorkoutSession? = null,
    val featuredRecipe: Recipe? = null,
    val weeklySessions: Int = 0,
    val progressSummary: ProgressSummary = ProgressSummary(),
    val isLoading: Boolean = true,
)

data class ProgressSummary(
    val latestWeight: Double? = null,
    val weightDelta: Double? = null,
    val entriesLogged: Int = 0,
    val activeDays: Int = 0,
)
