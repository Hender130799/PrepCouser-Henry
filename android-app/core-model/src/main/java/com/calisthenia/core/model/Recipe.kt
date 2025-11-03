package com.calisthenia.core.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val mealType: MealType,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
    val macros: NutritionInfo,
    val imageUrl: String?,
)

data class RecipeIngredient(
    val name: String,
    val quantity: String,
)

data class NutritionInfo(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
)

enum class MealType { DESAYUNO, COMIDA, CENA, SNACK, BATIDO }
