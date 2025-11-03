package com.calisthenia.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "progress_entry")
data class ProgressEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val timestamp: Instant,
    val weightKg: Double?,
    val bodyFatPercentage: Double?,
    val chestCm: Double?,
    val waistCm: Double?,
    val hipCm: Double?,
    val armCm: Double?,
    val thighCm: Double?,
    val workoutNotes: String?,
    val imageUrl: String?,
)
