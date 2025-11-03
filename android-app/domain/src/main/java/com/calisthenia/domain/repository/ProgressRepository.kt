package com.calisthenia.domain.repository

import com.calisthenia.core.model.ProgressEntry
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun observeProgress(): Flow<List<ProgressEntry>>
    suspend fun addEntry(entry: ProgressEntry)
    suspend fun deleteEntry(id: String)
}
