package com.calisthenia.data.repository

import com.calisthenia.core.model.Recipe
import com.calisthenia.core.model.RecipeCatalog
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

    private val recipes = MutableStateFlow(RecipeCatalog.recipes)

    override fun observeRecipes(filter: RecipeFilter): Flow<List<Recipe>> =
        recipes.asStateFlow().map { list ->
            list.filter { recipe ->
                val matchesMeal = filter.mealType?.let { recipe.mealType == it } ?: true
                val matchesTime = filter.maxPrepTimeMinutes?.let {
                    recipe.prepTimeMinutes + recipe.cookTimeMinutes <= it
                } ?: true
                val matchesDiet = if (filter.dietaryPreferences.isEmpty()) {
                    true
                } else {
                    val text = (recipe.description + recipe.title).lowercase()
                    filter.dietaryPreferences.all { preference ->
                        text.contains(preference.lowercase())
                    }
                }
                matchesMeal && matchesTime && matchesDiet
            }
        }

    override suspend fun refreshRecipes() {
        // TODO sincronizar con Firestore/remote cuando est? disponible
    }
}
