package com.calisthenia.data.repository

import com.calisthenia.core.model.ExperienceLevel
import com.calisthenia.core.model.TrainingFocus
import com.calisthenia.core.model.WorkoutExercise
import com.calisthenia.core.model.WorkoutPlan
import com.calisthenia.core.model.WorkoutSession
import com.calisthenia.domain.repository.WorkoutGenerationRequest
import com.calisthenia.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultWorkoutRepository @Inject constructor() : WorkoutRepository {

    private val planState = MutableStateFlow<WorkoutPlan?>(null)

    override fun observeActivePlan(): Flow<WorkoutPlan?> = planState.asStateFlow()

    override suspend fun generatePlan(request: WorkoutGenerationRequest): WorkoutPlan {
        // TODO reemplazar por motor de generaci?n real basado en perfil
        val mockExercises = listOf(
            WorkoutExercise(
                id = UUID.randomUUID().toString(),
                name = "Flexiones controladas",
                primaryMuscles = listOf(TrainingFocus.FULL_BODY.toMuscleGroup()),
                sets = 4,
                reps = 12,
                durationSeconds = null,
                restSeconds = 60,
                mediaUrl = null,
            ),
            WorkoutExercise(
                id = UUID.randomUUID().toString(),
                name = "Dominadas asistidas",
                primaryMuscles = listOf(TrainingFocus.UPPER_BODY.toMuscleGroup()),
                sets = 3,
                reps = 8,
                durationSeconds = null,
                restSeconds = 90,
                mediaUrl = null,
            ),
        )

        val session = WorkoutSession(
            dayOfWeek = 1,
            exercises = mockExercises,
            notes = "Calentamiento 5 minutos + estiramientos finales",
        )

        return WorkoutPlan(
            id = UUID.randomUUID().toString(),
            name = "Plan inicial",
            focus = TrainingFocus.FULL_BODY,
            level = ExperienceLevel.BEGINNER,
            sessions = List(request.sessionsPerWeek.coerceIn(2, 5)) { session },
            lastUpdated = Clock.System.now(),
        )
    }

    override suspend fun savePlan(plan: WorkoutPlan) {
        planState.value = plan
        // TODO persistir en Room y sincronizar con Firestore
    }

    private fun TrainingFocus.toMuscleGroup() = when (this) {
        TrainingFocus.FULL_BODY -> com.calisthenia.core.model.MuscleGroup.FULL_BODY
        TrainingFocus.UPPER_BODY -> com.calisthenia.core.model.MuscleGroup.BACK
        TrainingFocus.LOWER_BODY -> com.calisthenia.core.model.MuscleGroup.LEGS
        TrainingFocus.CORE -> com.calisthenia.core.model.MuscleGroup.CORE
        TrainingFocus.SKILL -> com.calisthenia.core.model.MuscleGroup.SHOULDERS
    }
}
