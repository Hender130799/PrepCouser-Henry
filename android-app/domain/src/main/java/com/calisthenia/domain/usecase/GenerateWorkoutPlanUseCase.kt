package com.calisthenia.domain.usecase

import com.calisthenia.core.model.WorkoutPlan
import com.calisthenia.domain.repository.WorkoutGenerationRequest
import com.calisthenia.domain.repository.WorkoutRepository

class GenerateWorkoutPlanUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(request: WorkoutGenerationRequest): WorkoutPlan {
        val plan = workoutRepository.generatePlan(request)
        workoutRepository.savePlan(plan)
        return plan
    }
}
