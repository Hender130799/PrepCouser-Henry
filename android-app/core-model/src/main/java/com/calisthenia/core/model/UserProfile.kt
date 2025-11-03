package com.calisthenia.core.model

import kotlinx.datetime.LocalDate

data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String?,
    val weightKg: Double,
    val heightCm: Double,
    val birthDate: LocalDate?,
    val experienceLevel: ExperienceLevel,
    val primaryGoal: TrainingGoal,
    val dietaryPreferences: Set<DietaryPreference>,
    val remindersEnabled: Boolean,
)

enum class ExperienceLevel { BEGINNER, INTERMEDIATE, ADVANCED }

enum class TrainingGoal { HYPERTROPHY, STRENGTH, FAT_LOSS, MAINTENANCE }

enum class DietaryPreference { NONE, VEGETARIAN, VEGAN, LACTOSE_FREE, GLUTEN_FREE }
