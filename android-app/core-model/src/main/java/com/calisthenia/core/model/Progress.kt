package com.calisthenia.core.model

import kotlinx.datetime.Instant

data class ProgressEntry(
    val id: String,
    val userId: String,
    val timestamp: Instant,
    val weightKg: Double?,
    val bodyFatPercentage: Double?,
    val measurements: BodyMeasurements?,
    val workoutNotes: String?,
    val imageUrl: String?,
)

data class BodyMeasurements(
    val chestCm: Double?,
    val waistCm: Double?,
    val hipCm: Double?,
    val armCm: Double?,
    val thighCm: Double?,
)
