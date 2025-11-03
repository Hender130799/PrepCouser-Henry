package com.calisthenia.core.model

val ExperienceLevel.label: String
    get() = when (this) {
        ExperienceLevel.BEGINNER -> "Principiante"
        ExperienceLevel.INTERMEDIATE -> "Intermedio"
        ExperienceLevel.ADVANCED -> "Avanzado"
    }

val TrainingGoal.label: String
    get() = when (this) {
        TrainingGoal.HYPERTROPHY -> "Hipertrofia"
        TrainingGoal.STRENGTH -> "Fuerza"
        TrainingGoal.FAT_LOSS -> "Definici?n"
        TrainingGoal.MAINTENANCE -> "Mantenimiento"
    }

val TrainingFocus.label: String
    get() = when (this) {
        TrainingFocus.FULL_BODY -> "Cuerpo completo"
        TrainingFocus.UPPER_BODY -> "Tren superior"
        TrainingFocus.LOWER_BODY -> "Tren inferior"
        TrainingFocus.CORE -> "Core y estabilidad"
        TrainingFocus.SKILL -> "Habilidades"
    }

val MealType.label: String
    get() = when (this) {
        MealType.DESAYUNO -> "Desayuno"
        MealType.COMIDA -> "Comida"
        MealType.CENA -> "Cena"
        MealType.SNACK -> "Snack"
        MealType.BATIDO -> "Batido"
    }

val DietaryPreference.label: String
    get() = when (this) {
        DietaryPreference.NONE -> "Sin restricciones"
        DietaryPreference.VEGETARIAN -> "Vegetariana"
        DietaryPreference.VEGAN -> "Vegana"
        DietaryPreference.LACTOSE_FREE -> "Sin l?cteos"
        DietaryPreference.GLUTEN_FREE -> "Sin gluten"
    }
