package com.calisthenia.domain.usecase

import com.calisthenia.core.model.UserProfile
import com.calisthenia.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class GetUserProfileUseCase(
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(): Flow<UserProfile?> = profileRepository.getUserProfile()
}
