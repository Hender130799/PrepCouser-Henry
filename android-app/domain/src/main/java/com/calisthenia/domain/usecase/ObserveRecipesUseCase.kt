package com.calisthenia.domain.usecase

import com.calisthenia.core.model.Recipe
import com.calisthenia.domain.repository.NutritionRepository
import com.calisthenia.domain.repository.RecipeFilter
import kotlinx.coroutines.flow.Flow

class ObserveRecipesUseCase(
    private val nutritionRepository: NutritionRepository,
) {
    operator fun invoke(filter: RecipeFilter = RecipeFilter()): Flow<List<Recipe>> =
        nutritionRepository.observeRecipes(filter)
}
