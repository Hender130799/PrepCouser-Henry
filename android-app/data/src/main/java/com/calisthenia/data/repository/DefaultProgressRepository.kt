package com.calisthenia.data.repository

import com.calisthenia.core.database.dao.ProgressEntryDao
import com.calisthenia.core.database.entity.ProgressEntryEntity
import com.calisthenia.core.model.BodyMeasurements
import com.calisthenia.core.model.ProgressEntry
import com.calisthenia.core.network.FirebaseSources
import com.calisthenia.domain.repository.ProgressRepository
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.datetime.toEpochMilliseconds

@Singleton
class DefaultProgressRepository @Inject constructor(
    private val progressEntryDao: ProgressEntryDao,
    private val firebaseSources: FirebaseSources,
) : ProgressRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch { runCatching { syncFromRemote() } }
    }

    override fun observeProgress(): Flow<List<ProgressEntry>> =
        progressEntryDao.observeEntries().map { list -> list.map { it.toDomain() } }

    override suspend fun addEntry(entry: ProgressEntry) {
        val entity = entry.toEntity()
        progressEntryDao.insert(entity)
        scope.launch { runCatching { pushRemoteEntry(entity) } }
    }

    override suspend fun deleteEntry(id: String) {
        progressEntryDao.deleteById(id)
        scope.launch { runCatching { deleteRemoteEntry(id) } }
    }

    private suspend fun syncFromRemote() {
        val snapshot = firebaseSources.firestore
            .collection(COLLECTION_PROGRESS)
            .get()
            .await()

        val entities = snapshot.documents.mapNotNull { it.toProgressEntity() }
        if (entities.isNotEmpty()) {
            entities.forEach { progressEntryDao.insert(it) }
        }
    }

    private suspend fun pushRemoteEntry(entity: ProgressEntryEntity) {
        firebaseSources.firestore
            .collection(COLLECTION_PROGRESS)
            .document(entity.id)
            .set(entity.toRemoteMap())
            .await()
    }

    private suspend fun deleteRemoteEntry(id: String) {
        firebaseSources.firestore
            .collection(COLLECTION_PROGRESS)
            .document(id)
            .delete()
            .await()
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

private fun ProgressEntryEntity.toRemoteMap(): Map<String, Any?> = mapOf(
    Field.ID to id,
    Field.USER_ID to userId,
    Field.TIMESTAMP to timestamp.toEpochMilliseconds(),
    Field.WEIGHT to weightKg,
    Field.BODY_FAT to bodyFatPercentage,
    Field.CHEST to chestCm,
    Field.WAIST to waistCm,
    Field.HIP to hipCm,
    Field.ARM to armCm,
    Field.THIGH to thighCm,
    Field.NOTES to workoutNotes,
    Field.IMAGE to imageUrl,
)

private fun DocumentSnapshot.toProgressEntity(): ProgressEntryEntity? {
    if (!exists()) return null
    val id = getString(Field.ID) ?: return null
    val userId = getString(Field.USER_ID) ?: DEFAULT_USER_ID
    val timestamp = getLong(Field.TIMESTAMP)?.let { Instant.fromEpochMilliseconds(it) } ?: return null
    val weight = getDouble(Field.WEIGHT)
    val bodyFat = getDouble(Field.BODY_FAT)
    val chest = getDouble(Field.CHEST)
    val waist = getDouble(Field.WAIST)
    val hip = getDouble(Field.HIP)
    val arm = getDouble(Field.ARM)
    val thigh = getDouble(Field.THIGH)
    val notes = getString(Field.NOTES)
    val image = getString(Field.IMAGE)

    return ProgressEntryEntity(
        id = id,
        userId = userId,
        timestamp = timestamp,
        weightKg = weight,
        bodyFatPercentage = bodyFat,
        chestCm = chest,
        waistCm = waist,
        hipCm = hip,
        armCm = arm,
        thighCm = thigh,
        workoutNotes = notes,
        imageUrl = image,
    )
}

private const val COLLECTION_PROGRESS = "progress"
private const val DEFAULT_USER_ID = "session"

private object Field {
    const val ID = "id"
    const val USER_ID = "userId"
    const val TIMESTAMP = "timestamp"
    const val WEIGHT = "weightKg"
    const val BODY_FAT = "bodyFatPercentage"
    const val CHEST = "chestCm"
    const val WAIST = "waistCm"
    const val HIP = "hipCm"
    const val ARM = "armCm"
    const val THIGH = "thighCm"
    const val NOTES = "workoutNotes"
    const val IMAGE = "imageUrl"
}
