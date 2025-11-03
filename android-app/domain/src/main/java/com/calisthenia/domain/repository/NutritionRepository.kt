package com.calisthenia.domain.repository

import com.calisthenia.core.model.MealType
import com.calisthenia.core.model.Recipe
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun observeRecipes(filter: RecipeFilter = RecipeFilter()): Flow<List<Recipe>>
    suspend fun refreshRecipes()
}

data class RecipeFilter(
    val mealType: MealType? = null,
    val maxPrepTimeMinutes: Int? = null,
    val dietaryPreferences: Set<String> = emptySet(),
)
