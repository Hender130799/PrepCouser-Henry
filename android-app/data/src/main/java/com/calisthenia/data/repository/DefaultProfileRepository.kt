package com.calisthenia.data.repository

import com.calisthenia.core.database.dao.UserProfileDao
import com.calisthenia.core.database.entity.UserProfileEntity
import com.calisthenia.core.model.DietaryPreference
import com.calisthenia.core.model.ExperienceLevel
import com.calisthenia.core.model.TrainingGoal
import com.calisthenia.core.model.UserProfile
import com.calisthenia.core.network.FirebaseSources
import com.calisthenia.domain.repository.ProfileRepository
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val firebaseSources: FirebaseSources,
) : ProfileRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            runCatching { fetchRemoteProfile()?.let { userProfileDao.upsert(it) } }
        }
    }

    override fun getUserProfile(): Flow<UserProfile?> =
        userProfileDao.observeProfile().map { entity -> entity?.toDomain() }

    override suspend fun upsertProfile(profile: UserProfile) {
        val entity = profile.toEntity()
        userProfileDao.upsert(entity)
        scope.launch { runCatching { pushRemoteProfile(entity) } }
    }

    private suspend fun fetchRemoteProfile(): UserProfileEntity? {
        val snapshot = firebaseSources.firestore
            .collection(COLLECTION_USERS)
            .document(PROFILE_DOC)
            .get()
            .await()
        return snapshot.toUserProfileEntity()
    }

    private suspend fun pushRemoteProfile(entity: UserProfileEntity) {
        firebaseSources.firestore
            .collection(COLLECTION_USERS)
            .document(PROFILE_DOC)
            .set(entity.toRemoteMap())
            .await()
    }

    private fun DocumentSnapshot.toUserProfileEntity(): UserProfileEntity? {
        if (!exists()) return null
        val id = getString(Field.ID) ?: return null
        val displayName = getString(Field.DISPLAY_NAME) ?: ""
        val email = getString(Field.EMAIL)
        val weight = getDouble(Field.WEIGHT)?.takeIf { !it.isNaN() } ?: return null
        val height = getDouble(Field.HEIGHT)?.takeIf { !it.isNaN() } ?: return null
        val birthDate = getString(Field.BIRTH_DATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val experienceLevel = getString(Field.EXPERIENCE_LEVEL)?.let { runCatching { ExperienceLevel.valueOf(it) }.getOrNull() }
            ?: ExperienceLevel.BEGINNER
        val primaryGoal = getString(Field.PRIMARY_GOAL)?.let { runCatching { TrainingGoal.valueOf(it) }.getOrNull() }
            ?: TrainingGoal.HYPERTROPHY
        val preferences = get(Field.DIETARY_PREFERENCES) as? List<*>
        val dietarySet = preferences
            ?.mapNotNull { pref -> (pref as? String)?.let { runCatching { DietaryPreference.valueOf(it) }.getOrNull() } }
            ?.toSet()
            ?.ifEmpty { setOf(DietaryPreference.NONE) }
            ?: setOf(DietaryPreference.NONE)
        val reminders = getBoolean(Field.REMINDERS_ENABLED) ?: true

        return UserProfileEntity(
            id = id,
            displayName = displayName,
            email = email,
            weightKg = weight,
            heightCm = height,
            birthDate = birthDate,
            experienceLevel = experienceLevel,
            primaryGoal = primaryGoal,
            dietaryPreferences = dietarySet,
            remindersEnabled = reminders,
        )
    }

    private fun UserProfileEntity.toRemoteMap(): Map<String, Any?> = mapOf(
        Field.ID to id,
        Field.DISPLAY_NAME to displayName,
        Field.EMAIL to email,
        Field.WEIGHT to weightKg,
        Field.HEIGHT to heightCm,
        Field.BIRTH_DATE to birthDate?.toString(),
        Field.EXPERIENCE_LEVEL to experienceLevel.name,
        Field.PRIMARY_GOAL to primaryGoal.name,
        Field.DIETARY_PREFERENCES to dietaryPreferences.map { it.name },
        Field.REMINDERS_ENABLED to remindersEnabled,
    )

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

    private companion object {
        const val COLLECTION_USERS = "users"
        const val PROFILE_DOC = "default"

        object Field {
            const val ID = "id"
            const val DISPLAY_NAME = "displayName"
            const val EMAIL = "email"
            const val WEIGHT = "weightKg"
            const val HEIGHT = "heightCm"
            const val BIRTH_DATE = "birthDate"
            const val EXPERIENCE_LEVEL = "experienceLevel"
            const val PRIMARY_GOAL = "primaryGoal"
            const val DIETARY_PREFERENCES = "dietaryPreferences"
            const val REMINDERS_ENABLED = "remindersEnabled"
        }
    }
}
