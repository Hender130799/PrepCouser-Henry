package com.calisthenia.data.repository

import com.calisthenia.core.model.ProgressEntry
import com.calisthenia.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultProgressRepository @Inject constructor() : ProgressRepository {

    private val entries = MutableStateFlow<List<ProgressEntry>>(emptyList())

    override fun observeProgress(): Flow<List<ProgressEntry>> = entries.asStateFlow()

    override suspend fun addEntry(entry: ProgressEntry) {
        entries.value = entries.value + entry
        // TODO persistir en base local y subir a Firestore
    }

    override suspend fun deleteEntry(id: String) {
        entries.value = entries.value.filterNot { it.id == id }
    }
}
