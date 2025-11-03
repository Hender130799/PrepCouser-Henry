package com.calisthenia.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.calisthenia.core.database.dao.ProgressEntryDao
import com.calisthenia.core.database.dao.RecipeDao
import com.calisthenia.core.database.dao.UserProfileDao
import com.calisthenia.core.database.dao.WorkoutPlanDao
import com.calisthenia.core.database.entity.ProgressEntryEntity
import com.calisthenia.core.database.entity.RecipeEntity
import com.calisthenia.core.database.entity.UserProfileEntity
import com.calisthenia.core.database.entity.WorkoutExerciseEntity
import com.calisthenia.core.database.entity.WorkoutPlanEntity
import com.calisthenia.core.database.entity.WorkoutSessionEntity

@Database(
    entities = [
        UserProfileEntity::class,
        WorkoutPlanEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        RecipeEntity::class,
        ProgressEntryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun recipeDao(): RecipeDao
    abstract fun progressEntryDao(): ProgressEntryDao
}
