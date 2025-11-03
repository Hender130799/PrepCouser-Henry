package com.calisthenia.domain.usecase

import com.calisthenia.core.model.WorkoutPlan
import com.calisthenia.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutPlanUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    operator fun invoke(): Flow<WorkoutPlan?> = workoutRepository.observeActivePlan()
}
