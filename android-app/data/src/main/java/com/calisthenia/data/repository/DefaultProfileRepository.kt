package com.calisthenia.data.repository

import com.calisthenia.core.database.dao.UserProfileDao
import com.calisthenia.core.database.entity.UserProfileEntity
import com.calisthenia.core.model.DietaryPreference
import com.calisthenia.core.model.UserProfile
import com.calisthenia.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
) : ProfileRepository {

    override fun getUserProfile(): Flow<UserProfile?> =
        userProfileDao.observeProfile().map { entity -> entity?.toDomain() }

    override suspend fun upsertProfile(profile: UserProfile) {
        userProfileDao.upsert(profile.toEntity())
        // TODO sincronizar con Firestore
    }
}

private fun UserProfileEntity.toDomain(): UserProfile =
    UserProfile(
        id = id,
        displayName = displayName,
        email = email,
        weightKg = weightKg,
        heightCm = heightCm,
        birthDate = birthDate,
        experienceLevel = experienceLevel,
        primaryGoal = primaryGoal,
        dietaryPreferences = dietaryPreferences.ifEmpty { setOf(DietaryPreference.NONE) },
        remindersEnabled = remindersEnabled,
    )

private fun UserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        id = id,
        displayName = displayName,
        email = email,
        weightKg = weightKg,
        heightCm = heightCm,
        birthDate = birthDate,
        experienceLevel = experienceLevel,
        primaryGoal = primaryGoal,
        dietaryPreferences = dietaryPreferences
            .filterNot { it == DietaryPreference.NONE }
            .toSet()
            .ifEmpty { setOf(DietaryPreference.NONE) },
        remindersEnabled = remindersEnabled,
    )
