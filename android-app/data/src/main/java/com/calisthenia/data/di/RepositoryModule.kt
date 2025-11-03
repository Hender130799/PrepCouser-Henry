package com.calisthenia.data.di

import com.calisthenia.data.repository.DefaultNutritionRepository
import com.calisthenia.data.repository.DefaultProfileRepository
import com.calisthenia.data.repository.DefaultProgressRepository
import com.calisthenia.data.repository.DefaultWorkoutRepository
import com.calisthenia.domain.repository.NutritionRepository
import com.calisthenia.domain.repository.ProfileRepository
import com.calisthenia.domain.repository.ProgressRepository
import com.calisthenia.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindProfileRepository(impl: DefaultProfileRepository): ProfileRepository

    @Binds
    abstract fun bindWorkoutRepository(impl: DefaultWorkoutRepository): WorkoutRepository

    @Binds
    abstract fun bindNutritionRepository(impl: DefaultNutritionRepository): NutritionRepository

    @Binds
    abstract fun bindProgressRepository(impl: DefaultProgressRepository): ProgressRepository
}
