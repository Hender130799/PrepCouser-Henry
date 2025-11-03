package com.calisthenia.data.repository

import com.calisthenia.core.database.dao.WorkoutPlanDao
import com.calisthenia.core.database.entity.WorkoutExerciseEntity
import com.calisthenia.core.database.entity.WorkoutPlanEntity
import com.calisthenia.core.database.entity.WorkoutPlanWithSessions
import com.calisthenia.core.database.entity.WorkoutSessionEntity
import com.calisthenia.core.database.entity.WorkoutSessionWithExercises
import com.calisthenia.core.model.ExerciseCatalog
import com.calisthenia.core.model.ExerciseDetail
import com.calisthenia.core.model.ExerciseDifficulty
import com.calisthenia.core.model.ExperienceLevel
import com.calisthenia.core.model.MuscleGroup
import com.calisthenia.core.model.TrainingFocus
import com.calisthenia.core.model.WorkoutExercise
import com.calisthenia.core.model.WorkoutPlan
import com.calisthenia.core.model.WorkoutSession
import com.calisthenia.core.network.FirebaseSources
import com.calisthenia.domain.repository.WorkoutGenerationRequest
import com.calisthenia.domain.repository.WorkoutRepository
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toEpochMilliseconds
import java.util.UUID

@Singleton
class DefaultWorkoutRepository @Inject constructor(
    private val workoutPlanDao: WorkoutPlanDao,
    private val firebaseSources: FirebaseSources,
) : WorkoutRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            runCatching { fetchRemotePlan()?.let { plan -> savePlanLocally(plan) } }
        }
    }

    override fun observeActivePlan(): Flow<WorkoutPlan?> =
        workoutPlanDao.observePlan().map { wrapper -> wrapper?.toDomain() }

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
        savePlanLocally(plan)
        scope.launch { runCatching { pushRemotePlan(plan) } }
    }

    private suspend fun savePlanLocally(plan: WorkoutPlan) {
        val (planEntity, sessionEntities, exerciseEntities) = plan.toEntities()
        workoutPlanDao.replacePlan(planEntity, sessionEntities, exerciseEntities)
    }

    private suspend fun fetchRemotePlan(): WorkoutPlan? {
        val snapshot = firebaseSources.firestore
            .collection(COLLECTION_PLANS)
            .document(PLAN_DOC)
            .get()
            .await()
        return snapshot.toWorkoutPlan()
    }

    private suspend fun pushRemotePlan(plan: WorkoutPlan) {
        firebaseSources.firestore
            .collection(COLLECTION_PLANS)
            .document(PLAN_DOC)
            .set(plan.toRemoteMap())
            .await()
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
        "definici?n", "perder grasa", "definicion" -> ExperienceLevel.INTERMEDIATE
        else -> ExperienceLevel.BEGINNER
    }

    private fun planNameForGoal(goal: String): String = when (goal.lowercase()) {
        "hipertrofia", "masa", "musculo", "m?sculo" -> "Hipertrofia funcional"
        "fuerza" -> "Fuerza y control"
        "definici?n", "perder grasa", "definicion" -> "Definici?n con calistenia"
        else -> "Plan integral de calistenia"
    }

    private fun WorkoutPlanWithSessions.toDomain(): WorkoutPlan {
        val sessionDomains = sessions
            .sortedBy { it.session.orderIndex }
            .map { sessionWith -> sessionWith.toDomain() }

        return WorkoutPlan(
            id = plan.id,
            name = plan.name,
            focus = plan.focus,
            level = plan.level,
            sessions = sessionDomains,
            lastUpdated = plan.lastUpdated,
        )
    }

    private fun WorkoutSessionWithExercises.toDomain(): WorkoutSession =
        WorkoutSession(
            dayOfWeek = session.dayOfWeek,
            notes = session.notes,
            exercises = exercises
                .sortedBy { it.orderIndex }
                .map { entity ->
                    WorkoutExercise(
                        id = entity.exerciseRef,
                        name = entity.name,
                        primaryMuscles = entity.primaryMuscles,
                        sets = entity.sets,
                        reps = entity.reps,
                        durationSeconds = entity.durationSeconds,
                        restSeconds = entity.restSeconds,
                        mediaUrl = entity.mediaUrl,
                    )
                },
        )

    private fun WorkoutPlan.toEntities(): Triple<WorkoutPlanEntity, List<WorkoutSessionEntity>, List<WorkoutExerciseEntity>> {
        val planEntity = WorkoutPlanEntity(
            id = id,
            name = name,
            focus = focus,
            level = level,
            lastUpdated = lastUpdated,
        )

        val sessionEntities = mutableListOf<WorkoutSessionEntity>()
        val exerciseEntities = mutableListOf<WorkoutExerciseEntity>()

        sessions.forEachIndexed { index, session ->
            val sessionId = "$id-$index"
            sessionEntities += WorkoutSessionEntity(
                sessionId = sessionId,
                planId = id,
                dayOfWeek = session.dayOfWeek,
                notes = session.notes,
                orderIndex = index,
            )

            session.exercises.forEachIndexed { exerciseIndex, exercise ->
                exerciseEntities += WorkoutExerciseEntity(
                    sessionId = sessionId,
                    orderIndex = exerciseIndex,
                    exerciseRef = exercise.id,
                    name = exercise.name,
                    primaryMuscles = exercise.primaryMuscles,
                    sets = exercise.sets,
                    reps = exercise.reps,
                    durationSeconds = exercise.durationSeconds,
                    restSeconds = exercise.restSeconds,
                    mediaUrl = exercise.mediaUrl,
                )
            }
        }

        return Triple(planEntity, sessionEntities, exerciseEntities)
    }

    private fun WorkoutPlan.toRemoteMap(): Map<String, Any?> = mapOf(
        Field.ID to id,
        Field.NAME to name,
        Field.FOCUS to focus.name,
        Field.LEVEL to level.name,
        Field.LAST_UPDATED to lastUpdated.toEpochMilliseconds(),
        Field.SESSIONS to sessions.mapIndexed { index, session ->
            mapOf(
                Field.DAY_OF_WEEK to session.dayOfWeek,
                Field.NOTES to session.notes,
                Field.ORDER_INDEX to index,
                Field.EXERCISES to session.exercises.mapIndexed { exerciseIndex, exercise ->
                    mapOf(
                        Field.EXERCISE_REF to exercise.id,
                        Field.EXERCISE_NAME to exercise.name,
                        Field.PRIMARY_MUSCLES to exercise.primaryMuscles.map { it.name },
                        Field.SETS to exercise.sets,
                        Field.REPS to exercise.reps,
                        Field.DURATION to exercise.durationSeconds,
                        Field.REST to exercise.restSeconds,
                        Field.MEDIA_URL to exercise.mediaUrl,
                        Field.EXERCISE_ORDER to exerciseIndex,
                    )
                },
            )
        },
    )

    private fun DocumentSnapshot.toWorkoutPlan(): WorkoutPlan? {
        if (!exists()) return null
        val id = getString(Field.ID) ?: UUID.randomUUID().toString()
        val name = getString(Field.NAME) ?: "Plan"
        val focus = getString(Field.FOCUS)?.let { runCatching { TrainingFocus.valueOf(it) }.getOrNull() } ?: TrainingFocus.FULL_BODY
        val level = getString(Field.LEVEL)?.let { runCatching { ExperienceLevel.valueOf(it) }.getOrNull() } ?: ExperienceLevel.BEGINNER
        val lastUpdatedMillis = getLong(Field.LAST_UPDATED) ?: Clock.System.now().toEpochMilliseconds()
        val sessionMaps = get(Field.SESSIONS) as? List<*>

        val sessionDomains = sessionMaps
            ?.mapIndexedNotNull { index, sessionAny ->
                (sessionAny as? Map<*, *>)?.let { map ->
                    val dayOfWeek = (map[Field.DAY_OF_WEEK] as? Number)?.toInt() ?: index + 1
                    val notes = map[Field.NOTES] as? String
                    val exercises = (map[Field.EXERCISES] as? List<*>)
                        ?.mapIndexedNotNull { exIndex, exAny ->
                            (exAny as? Map<*, *>)?.let { exMap ->
                                val ref = exMap[Field.EXERCISE_REF] as? String ?: UUID.randomUUID().toString()
                                val exName = exMap[Field.EXERCISE_NAME] as? String ?: "Ejercicio"
                                val muscles = (exMap[Field.PRIMARY_MUSCLES] as? List<*>)
                                    ?.mapNotNull { (it as? String)?.let { name -> runCatching { MuscleGroup.valueOf(name) }.getOrNull() } }
                                    ?.takeIf { it.isNotEmpty() }
                                    ?: listOf(MuscleGroup.FULL_BODY)
                                val sets = (exMap[Field.SETS] as? Number)?.toInt() ?: 3
                                val reps = (exMap[Field.REPS] as? Number)?.toInt()
                                val duration = (exMap[Field.DURATION] as? Number)?.toInt()
                                val rest = (exMap[Field.REST] as? Number)?.toInt() ?: 60
                                val mediaUrl = exMap[Field.MEDIA_URL] as? String
                                WorkoutExercise(
                                    id = ref,
                                    name = exName,
                                    primaryMuscles = muscles,
                                    sets = sets,
                                    reps = reps,
                                    durationSeconds = duration,
                                    restSeconds = rest,
                                    mediaUrl = mediaUrl,
                                )
                            }
                        }
                        ?: emptyList()
                    WorkoutSession(dayOfWeek = dayOfWeek, notes = notes, exercises = exercises)
                }
            }
            ?: emptyList()

        return WorkoutPlan(
            id = id,
            name = name,
            focus = focus,
            level = level,
            sessions = sessionDomains,
            lastUpdated = Instant.fromEpochMilliseconds(lastUpdatedMillis),
        )
    }

    private companion object {
        const val EXERCISES_PER_SESSION = 4
        val ISOMETRIC_EXERCISES = setOf(
            "plancha-prona",
            "hollow-hold",
            "dragon-flag",
        )

        const val COLLECTION_PLANS = "plans"
        const val PLAN_DOC = "default"

        object Field {
            const val ID = "id"
            const val NAME = "name"
            const val FOCUS = "focus"
            const val LEVEL = "level"
            const val LAST_UPDATED = "lastUpdated"
            const val SESSIONS = "sessions"
            const val DAY_OF_WEEK = "dayOfWeek"
            const val NOTES = "notes"
            const val ORDER_INDEX = "orderIndex"
            const val EXERCISES = "exercises"
            const val EXERCISE_REF = "exerciseRef"
            const val EXERCISE_NAME = "exerciseName"
            const val PRIMARY_MUSCLES = "primaryMuscles"
            const val SETS = "sets"
            const val REPS = "reps"
            const val DURATION = "durationSeconds"
            const val REST = "restSeconds"
            const val MEDIA_URL = "mediaUrl"
            const val EXERCISE_ORDER = "exerciseOrder"
        }
    }
}
