package com.calisthenia.domain.usecase

import com.calisthenia.domain.repository.ProgressRepository

class DeleteProgressEntryUseCase(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(entryId: String) {
        progressRepository.deleteEntry(entryId)
    }
}
