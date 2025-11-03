package com.calisthenia.data.repository

import com.calisthenia.core.model.UserProfile
import com.calisthenia.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultProfileRepository @Inject constructor() : ProfileRepository {

    private val profileState = MutableStateFlow<UserProfile?>(null)

    override fun getUserProfile(): Flow<UserProfile?> = profileState.asStateFlow()

    override suspend fun upsertProfile(profile: UserProfile) {
        profileState.value = profile
        // TODO guardar en Room y sincronizar con Firestore
    }
}
