package com.calisthenia.data.repository

import com.calisthenia.core.database.dao.ProgressEntryDao
import com.calisthenia.core.database.entity.ProgressEntryEntity
import com.calisthenia.core.model.BodyMeasurements
import com.calisthenia.core.model.ProgressEntry
import com.calisthenia.domain.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultProgressRepository @Inject constructor(
    private val progressEntryDao: ProgressEntryDao,
) : ProgressRepository {

    override fun observeProgress(): Flow<List<ProgressEntry>> =
        progressEntryDao.observeEntries().map { list -> list.map { it.toDomain() } }

    override suspend fun addEntry(entry: ProgressEntry) {
        progressEntryDao.insert(entry.toEntity())
    }

    override suspend fun deleteEntry(id: String) {
        progressEntryDao.deleteById(id)
    }
}

private fun ProgressEntryEntity.toDomain(): ProgressEntry {
    val measurements = listOf(chestCm, waistCm, hipCm, armCm, thighCm)
    val bodyMeasurements = if (measurements.all { it == null }) {
        null
    } else {
        BodyMeasurements(
            chestCm = chestCm,
            waistCm = waistCm,
            hipCm = hipCm,
            armCm = armCm,
            thighCm = thighCm,
        )
    }

    return ProgressEntry(
        id = id,
        userId = userId,
        timestamp = timestamp,
        weightKg = weightKg,
        bodyFatPercentage = bodyFatPercentage,
        measurements = bodyMeasurements,
        workoutNotes = workoutNotes,
        imageUrl = imageUrl,
    )
}

private fun ProgressEntry.toEntity(): ProgressEntryEntity = ProgressEntryEntity(
    id = id,
    userId = userId,
    timestamp = timestamp,
    weightKg = weightKg,
    bodyFatPercentage = bodyFatPercentage,
    chestCm = measurements?.chestCm,
    waistCm = measurements?.waistCm,
    hipCm = measurements?.hipCm,
    armCm = measurements?.armCm,
    thighCm = measurements?.thighCm,
    workoutNotes = workoutNotes,
    imageUrl = imageUrl,
)
