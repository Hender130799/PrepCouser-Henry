package com.calisthenia.data.di

import com.calisthenia.domain.repository.NutritionRepository
import com.calisthenia.domain.repository.ProfileRepository
import com.calisthenia.domain.repository.ProgressRepository
import com.calisthenia.domain.repository.WorkoutRepository
import com.calisthenia.domain.usecase.AddProgressEntryUseCase
import com.calisthenia.domain.usecase.DeleteProgressEntryUseCase
import com.calisthenia.domain.usecase.GenerateWorkoutPlanUseCase
import com.calisthenia.domain.usecase.GetUserProfileUseCase
import com.calisthenia.domain.usecase.ObserveProgressUseCase
import com.calisthenia.domain.usecase.ObserveRecipesUseCase
import com.calisthenia.domain.usecase.ObserveWorkoutPlanUseCase
import com.calisthenia.domain.usecase.UpsertUserProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetUserProfileUseCase(
        profileRepository: ProfileRepository,
    ): GetUserProfileUseCase = GetUserProfileUseCase(profileRepository)

    @Provides
    @Singleton
    fun provideUpsertUserProfileUseCase(
        profileRepository: ProfileRepository,
    ): UpsertUserProfileUseCase = UpsertUserProfileUseCase(profileRepository)

    @Provides
    @Singleton
    fun provideGenerateWorkoutPlanUseCase(
        workoutRepository: WorkoutRepository,
    ): GenerateWorkoutPlanUseCase = GenerateWorkoutPlanUseCase(workoutRepository)

    @Provides
    @Singleton
    fun provideObserveWorkoutPlanUseCase(
        workoutRepository: WorkoutRepository,
    ): ObserveWorkoutPlanUseCase = ObserveWorkoutPlanUseCase(workoutRepository)

    @Provides
    @Singleton
    fun provideObserveRecipesUseCase(
        nutritionRepository: NutritionRepository,
    ): ObserveRecipesUseCase = ObserveRecipesUseCase(nutritionRepository)

    @Provides
    @Singleton
    fun provideObserveProgressUseCase(
        progressRepository: ProgressRepository,
    ): ObserveProgressUseCase = ObserveProgressUseCase(progressRepository)

    @Provides
    @Singleton
    fun provideAddProgressEntryUseCase(
        progressRepository: ProgressRepository,
    ): AddProgressEntryUseCase = AddProgressEntryUseCase(progressRepository)

    @Provides
    @Singleton
    fun provideDeleteProgressEntryUseCase(
        progressRepository: ProgressRepository,
    ): DeleteProgressEntryUseCase = DeleteProgressEntryUseCase(progressRepository)
}
