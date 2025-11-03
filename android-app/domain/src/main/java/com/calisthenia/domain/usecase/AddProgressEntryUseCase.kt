package com.calisthenia.domain.usecase

import com.calisthenia.core.model.ProgressEntry
import com.calisthenia.domain.repository.ProgressRepository

class AddProgressEntryUseCase(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(entry: ProgressEntry) {
        progressRepository.addEntry(entry)
    }
}
