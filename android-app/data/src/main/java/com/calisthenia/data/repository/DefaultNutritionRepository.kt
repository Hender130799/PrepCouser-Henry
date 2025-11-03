package com.calisthenia.data.repository

import com.calisthenia.core.model.MealType
import com.calisthenia.core.model.NutritionInfo
import com.calisthenia.core.model.Recipe
import com.calisthenia.core.model.RecipeIngredient
import com.calisthenia.domain.repository.NutritionRepository
import com.calisthenia.domain.repository.RecipeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNutritionRepository @Inject constructor() : NutritionRepository {

    private val recipes = MutableStateFlow(sampleRecipes())

    override fun observeRecipes(filter: RecipeFilter): Flow<List<Recipe>> =
        recipes.asStateFlow().map { list ->
            list.filter { recipe ->
                val matchesMeal = filter.mealType?.let { recipe.mealType == it } ?: true
                val matchesTime = filter.maxPrepTimeMinutes?.let {
                    recipe.prepTimeMinutes + recipe.cookTimeMinutes <= it
                } ?: true
                val matchesDiet = if (filter.dietaryPreferences.isEmpty()) true else {
                    // TODO map dietary flags once data lo soporte
                    true
                }
                matchesMeal && matchesTime && matchesDiet
            }
        }

    override suspend fun refreshRecipes() {
        // TODO sincronizar con Firestore/Remote source
    }

    private fun sampleRecipes(): List<Recipe> = listOf(
        Recipe(
            id = "receta-batido-verde",
            title = "Batido verde para ganar masa",
            description = "Batido cremoso con espinaca, pl?tano y avena.",
            mealType = MealType.BATIDO,
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 1,
            ingredients = listOf(
                RecipeIngredient("Leche entera", "250 ml"),
                RecipeIngredient("Avena", "40 g"),
                RecipeIngredient("Pl?tano maduro", "1 pieza"),
                RecipeIngredient("Mantequilla de man?", "1 cucharada"),
                RecipeIngredient("Espinaca fresca", "1 taza"),
            ),
            steps = listOf(
                "Lic?a todos los ingredientes hasta obtener una mezcla homog?nea.",
                "Sirve de inmediato y disfruta fr?o.",
            ),
            macros = NutritionInfo(calories = 520, protein = 24.0, carbs = 58.0, fats = 22.0),
            imageUrl = null,
        ),
    )
}
