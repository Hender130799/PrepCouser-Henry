package com.calisthenia.domain.repository

import com.calisthenia.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun upsertProfile(profile: UserProfile)
}
