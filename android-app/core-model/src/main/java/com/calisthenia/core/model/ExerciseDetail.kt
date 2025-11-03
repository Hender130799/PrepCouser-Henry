package com.calisthenia.core.model

data class ExerciseDetail(
    val id: String,
    val name: String,
    val description: String,
    val muscleGroups: List<MuscleGroup>,
    val difficulty: ExerciseDifficulty,
    val equipment: Equipment,
    val focus: TrainingFocus,
    val tips: List<String>,
    val mediaUrl: String? = null,
)

enum class ExerciseDifficulty { BEGINNER, INTERMEDIATE, ADVANCED }

enum class Equipment { BODYWEIGHT, BARRA, ANILLAS, PARALELAS, RESISTENCIA, ESPALDERA }
