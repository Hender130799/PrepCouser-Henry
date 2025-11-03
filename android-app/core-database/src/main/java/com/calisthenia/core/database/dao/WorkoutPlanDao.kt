package com.calisthenia.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.calisthenia.core.database.entity.WorkoutExerciseEntity
import com.calisthenia.core.database.entity.WorkoutPlanEntity
import com.calisthenia.core.database.entity.WorkoutPlanWithSessions
import com.calisthenia.core.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {

    @Transaction
    @Query("SELECT * FROM workout_plan LIMIT 1")
    fun observePlan(): Flow<WorkoutPlanWithSessions?>

    @Transaction
    suspend fun replacePlan(
        plan: WorkoutPlanEntity,
        sessions: List<WorkoutSessionEntity>,
        exercises: List<WorkoutExerciseEntity>,
    ) {
        deleteAll()
        insertPlan(plan)
        insertSessions(sessions)
        insertExercises(exercises)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WorkoutPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<WorkoutSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<WorkoutExerciseEntity>)

    @Query("DELETE FROM workout_plan")
    suspend fun deletePlan()

    @Query("DELETE FROM workout_session")
    suspend fun deleteSessions()

    @Query("DELETE FROM workout_exercise")
    suspend fun deleteExercises()

    @Transaction
    suspend fun deleteAll() {
        deletePlan()
        deleteSessions()
        deleteExercises()
    }
}
