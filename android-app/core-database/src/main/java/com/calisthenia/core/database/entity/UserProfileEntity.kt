package com.calisthenia.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calisthenia.core.model.DietaryPreference
import com.calisthenia.core.model.ExperienceLevel
import com.calisthenia.core.model.TrainingGoal
import kotlinx.datetime.LocalDate

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
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
