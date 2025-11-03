package com.calisthenia.domain.usecase

import com.calisthenia.core.model.ProgressEntry
import com.calisthenia.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow

class ObserveProgressUseCase(
    private val progressRepository: ProgressRepository,
) {
    operator fun invoke(): Flow<List<ProgressEntry>> = progressRepository.observeProgress()
}
