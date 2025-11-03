package com.calisthenia.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.calisthenia.core.model.MuscleGroup
import com.calisthenia.core.model.TrainingFocus
import com.calisthenia.core.model.ExperienceLevel
import kotlinx.datetime.Instant

@Entity(tableName = "workout_plan")
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val focus: TrainingFocus,
    val level: ExperienceLevel,
    val lastUpdated: Instant,
)

@Entity(tableName = "workout_session")
data class WorkoutSessionEntity(
    @PrimaryKey val sessionId: String,
    val planId: String,
    val dayOfWeek: Int,
    val notes: String?,
    val orderIndex: Int,
)

@Entity(tableName = "workout_exercise")
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val orderIndex: Int,
    val exerciseRef: String,
    val name: String,
    val primaryMuscles: List<MuscleGroup>,
    val sets: Int,
    val reps: Int?,
    val durationSeconds: Int?,
    val restSeconds: Int,
    val mediaUrl: String?,
)

data class WorkoutSessionWithExercises(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId",
    )
    val exercises: List<WorkoutExerciseEntity>,
)

data class WorkoutPlanWithSessions(
    @Embedded val plan: WorkoutPlanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "planId",
        entity = WorkoutSessionEntity::class,
    )
    val sessions: List<WorkoutSessionWithExercises>,
)
