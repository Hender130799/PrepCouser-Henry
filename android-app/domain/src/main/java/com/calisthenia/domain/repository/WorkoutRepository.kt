package com.calisthenia.domain.repository

import com.calisthenia.core.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeActivePlan(): Flow<WorkoutPlan?>
    suspend fun generatePlan(request: WorkoutGenerationRequest): WorkoutPlan
    suspend fun savePlan(plan: WorkoutPlan)
}

data class WorkoutGenerationRequest(
    val userId: String,
    val goal: String,
    val sessionsPerWeek: Int,
)
