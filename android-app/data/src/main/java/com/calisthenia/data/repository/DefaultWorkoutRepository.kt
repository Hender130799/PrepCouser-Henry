package com.calisthenia.data.repository

import com.calisthenia.core.model.ExerciseCatalog
import com.calisthenia.core.model.ExerciseDetail
import com.calisthenia.core.model.ExerciseDifficulty
import com.calisthenia.core.model.ExperienceLevel
import com.calisthenia.core.model.MuscleGroup
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
import kotlin.random.Random
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultWorkoutRepository @Inject constructor() : WorkoutRepository {

    private val planState = MutableStateFlow<WorkoutPlan?>(null)

    override fun observeActivePlan(): Flow<WorkoutPlan?> = planState.asStateFlow()

    override suspend fun generatePlan(request: WorkoutGenerationRequest): WorkoutPlan {
        val adjustedSessions = request.sessionsPerWeek.coerceIn(2, 5)
        val focusRotation = listOf(
            TrainingFocus.FULL_BODY,
            TrainingFocus.UPPER_BODY,
            TrainingFocus.LOWER_BODY,
            TrainingFocus.CORE,
        )

        val level = request.deriveExperienceLevel()
        val random = Random(request.userId.hashCode())

        val sessions = (0 until adjustedSessions).map { index ->
            val focus = focusRotation[index % focusRotation.size]
            createSessionForFocus(focus, level, random, dayOfWeek = index + 1)
        }

        return WorkoutPlan(
            id = UUID.randomUUID().toString(),
            name = planNameForGoal(request.goal),
            focus = TrainingFocus.FULL_BODY,
            level = level,
            sessions = sessions,
            lastUpdated = Clock.System.now(),
        )
    }

    override suspend fun savePlan(plan: WorkoutPlan) {
        planState.value = plan
        // TODO persistir en Room y sincronizar con Firestore
    }

    private fun createSessionForFocus(
        focus: TrainingFocus,
        level: ExperienceLevel,
        random: Random,
        dayOfWeek: Int,
    ): WorkoutSession {
        val pool = ExerciseCatalog.exercises.filter { exercise ->
            exercise.focus == focus || (focus == TrainingFocus.FULL_BODY && exercise.focus != TrainingFocus.SKILL)
        }

        val exercises = pool.shuffled(random)
            .take(EXERCISES_PER_SESSION)
            .map { detail -> detail.toWorkoutExercise(level) }

        val focusNote = when (focus) {
            TrainingFocus.UPPER_BODY -> "Enf?cate en empujes y tirones controlados."
            TrainingFocus.LOWER_BODY -> "Trabaja profundidad y tensi?n constante en piernas."
            TrainingFocus.CORE -> "Mant?n respiraci?n diafragm?tica y evita sobrecargar la zona lumbar."
            TrainingFocus.FULL_BODY -> "Integra movilidad y activaci?n general antes de iniciar."
            TrainingFocus.SKILL -> "Dedica tiempo a progresiones t?cnicas y mayor descanso."
        }

        return WorkoutSession(
            dayOfWeek = dayOfWeek,
            exercises = exercises,
            notes = focusNote,
        )
    }

    private fun ExerciseDetail.toWorkoutExercise(level: ExperienceLevel): WorkoutExercise {
        val isIsometric = id in ISOMETRIC_EXERCISES
        val targetReps = when (difficulty) {
            ExerciseDifficulty.BEGINNER -> 12
            ExerciseDifficulty.INTERMEDIATE -> 10
            ExerciseDifficulty.ADVANCED -> 8
        }

        val sets = when (level) {
            ExperienceLevel.BEGINNER -> 3
            ExperienceLevel.INTERMEDIATE -> 4
            ExperienceLevel.ADVANCED -> 5
        }

        val restSeconds = when (level) {
            ExperienceLevel.BEGINNER -> 60
            ExperienceLevel.INTERMEDIATE -> 75
            ExperienceLevel.ADVANCED -> 90
        }

        val durationSeconds = if (isIsometric) {
            when (level) {
                ExperienceLevel.BEGINNER -> 30
                ExperienceLevel.INTERMEDIATE -> 40
                ExperienceLevel.ADVANCED -> 50
            }
        } else null

        val reps = if (isIsometric) null else targetReps

        return WorkoutExercise(
            id = id,
            name = name,
            primaryMuscles = muscleGroups.ensureAtLeastOne(),
            sets = sets,
            reps = reps,
            durationSeconds = durationSeconds,
            restSeconds = restSeconds,
            mediaUrl = mediaUrl,
        )
    }

    private fun List<MuscleGroup>.ensureAtLeastOne(): List<MuscleGroup> =
        if (isEmpty()) listOf(MuscleGroup.FULL_BODY) else this

    private fun WorkoutGenerationRequest.deriveExperienceLevel(): ExperienceLevel = when (goal.lowercase()) {
        "hipertrofia", "masa", "musculo", "m?sculo" -> ExperienceLevel.INTERMEDIATE
        "fuerza" -> ExperienceLevel.ADVANCED
        "definicion", "definici?n", "perder grasa" -> ExperienceLevel.INTERMEDIATE
        else -> ExperienceLevel.BEGINNER
    }

    private fun planNameForGoal(goal: String): String = when (goal.lowercase()) {
        "hipertrofia", "masa", "musculo", "m?sculo" -> "Hipertrofia funcional"
        "fuerza" -> "Fuerza y control"
        "definicion", "definici?n", "perder grasa" -> "Definici?n con calistenia"
        else -> "Plan integral de calistenia"
    }

    private companion object {
        const val EXERCISES_PER_SESSION = 4
        val ISOMETRIC_EXERCISES = setOf(
            "plancha-prona",
            "hollow-hold",
            "dragon-flag",
        )
    }
}
