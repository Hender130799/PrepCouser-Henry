package com.calisthenia.core.database

import androidx.room.TypeConverter
import com.calisthenia.core.model.DietaryPreference
import com.calisthenia.core.model.ExperienceLevel
import com.calisthenia.core.model.MealType
import com.calisthenia.core.model.MuscleGroup
import com.calisthenia.core.model.TrainingFocus
import com.calisthenia.core.model.TrainingGoal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class Converters {

    @TypeConverter
    fun fromInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun fromDietarySet(value: String?): Set<DietaryPreference> =
        value?.takeIf { it.isNotBlank() }
            ?.split(DELIMITER)
            ?.mapNotNull { runCatching { DietaryPreference.valueOf(it) }.getOrNull() }
            ?.toSet()
            ?: emptySet()

    @TypeConverter
    fun dietarySetToString(value: Set<DietaryPreference>?): String =
        value?.joinToString(DELIMITER) { it.name } ?: ""

    @TypeConverter
    fun fromMuscleGroups(value: String?): List<MuscleGroup> =
        value?.takeIf { it.isNotBlank() }
            ?.split(DELIMITER)
            ?.mapNotNull { runCatching { MuscleGroup.valueOf(it) }.getOrNull() }
            ?: emptyList()

    @TypeConverter
    fun muscleGroupsToString(value: List<MuscleGroup>?): String =
        value?.joinToString(DELIMITER) { it.name } ?: ""

    @TypeConverter
    fun fromExperienceLevel(value: String?): ExperienceLevel? =
        value?.let { runCatching { ExperienceLevel.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun experienceLevelToString(value: ExperienceLevel?): String? = value?.name

    @TypeConverter
    fun fromTrainingGoal(value: String?): TrainingGoal? =
        value?.let { runCatching { TrainingGoal.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun trainingGoalToString(value: TrainingGoal?): String? = value?.name

    @TypeConverter
    fun fromTrainingFocus(value: String?): TrainingFocus? =
        value?.let { runCatching { TrainingFocus.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun trainingFocusToString(value: TrainingFocus?): String? = value?.name

    @TypeConverter
    fun fromMealType(value: String?): MealType? =
        value?.let { runCatching { MealType.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun mealTypeToString(value: MealType?): String? = value?.name

    private companion object {
        const val DELIMITER = "|"
    }
}
