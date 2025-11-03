package com.calisthenia.core.model

import kotlinx.datetime.Instant

data class WorkoutPlan(
    val id: String,
    val name: String,
    val focus: TrainingFocus,
    val level: ExperienceLevel,
    val sessions: List<WorkoutSession>,
    val lastUpdated: Instant,
)

data class WorkoutSession(
    val dayOfWeek: Int,
    val exercises: List<WorkoutExercise>,
    val notes: String? = null,
)

data class WorkoutExercise(
    val id: String,
    val name: String,
    val primaryMuscles: List<MuscleGroup>,
    val sets: Int,
    val reps: Int?,
    val durationSeconds: Int?,
    val restSeconds: Int,
    val mediaUrl: String?,
)

enum class TrainingFocus { FULL_BODY, UPPER_BODY, LOWER_BODY, CORE, SKILL }

enum class MuscleGroup { CHEST, BACK, SHOULDERS, ARMS, CORE, LEGS, FULL_BODY }
