package com.calisthenia.domain.usecase

import com.calisthenia.core.model.UserProfile
import com.calisthenia.domain.repository.ProfileRepository

class UpsertUserProfileUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(profile: UserProfile) {
        profileRepository.upsertProfile(profile)
    }
}
