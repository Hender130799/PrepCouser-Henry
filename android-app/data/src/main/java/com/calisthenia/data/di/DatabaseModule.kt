package com.calisthenia.data.di

import android.content.Context
import androidx.room.Room
import com.calisthenia.core.database.AppDatabase
import com.calisthenia.core.database.dao.ProgressEntryDao
import com.calisthenia.core.database.dao.RecipeDao
import com.calisthenia.core.database.dao.UserProfileDao
import com.calisthenia.core.database.dao.WorkoutPlanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "calistenia.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideWorkoutPlanDao(database: AppDatabase): WorkoutPlanDao = database.workoutPlanDao()

    @Provides
    fun provideRecipeDao(database: AppDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideProgressEntryDao(database: AppDatabase): ProgressEntryDao = database.progressEntryDao()
}
