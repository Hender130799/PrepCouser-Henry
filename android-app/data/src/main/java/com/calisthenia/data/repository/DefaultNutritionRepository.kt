package com.calisthenia.data.repository

import com.calisthenia.core.database.dao.RecipeDao
import com.calisthenia.core.database.entity.RecipeEntity
import com.calisthenia.core.model.MealType
import com.calisthenia.core.model.NutritionInfo
import com.calisthenia.core.model.Recipe
import com.calisthenia.core.model.RecipeCatalog
import com.calisthenia.core.model.RecipeIngredient
import com.calisthenia.domain.repository.NutritionRepository
import com.calisthenia.domain.repository.RecipeFilter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
class DefaultNutritionRepository @Inject constructor(
    private val recipeDao: RecipeDao,
) : NutritionRepository {

    private val seedingScope = CoroutineScope(Dispatchers.IO)

    init {
        seedingScope.launch {
            if (recipeDao.count() == 0) {
                recipeDao.insertAll(RecipeCatalog.recipes.map { it.toEntity() })
            }
        }
    }

    override fun observeRecipes(filter: RecipeFilter): Flow<List<Recipe>> =
        recipeDao.observeRecipes().map { list ->
            list.map { it.toDomain() }.filter { recipe ->
                val matchesMeal = filter.mealType?.let { recipe.mealType == it } ?: true
                val matchesTime = filter.maxPrepTimeMinutes?.let { max ->
                    recipe.prepTimeMinutes + recipe.cookTimeMinutes <= max
                } ?: true
                val matchesDiet = if (filter.dietaryPreferences.isEmpty()) {
                    true
                } else {
                    val searchableText = (recipe.description + recipe.title).lowercase()
                    filter.dietaryPreferences.all { preference ->
                        searchableText.contains(preference.lowercase())
                    }
                }
                matchesMeal && matchesTime && matchesDiet
            }
        }

    override suspend fun refreshRecipes() {
        recipeDao.deleteAll()
        recipeDao.insertAll(RecipeCatalog.recipes.map { it.toEntity() })
    }
}

private fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    description = description,
    mealType = mealType,
    prepTimeMinutes = prepTimeMinutes,
    cookTimeMinutes = cookTimeMinutes,
    servings = servings,
    ingredientsSerialized = ingredients.joinToString(INGREDIENT_DELIMITER) { "${it.quantity}$VALUE_DELIMITER${it.name}" },
    stepsSerialized = steps.joinToString(STEP_DELIMITER),
    calories = macros.calories,
    protein = macros.protein,
    carbs = macros.carbs,
    fats = macros.fats,
    imageUrl = imageUrl,
)

private fun RecipeEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    mealType = mealType,
    prepTimeMinutes = prepTimeMinutes,
    cookTimeMinutes = cookTimeMinutes,
    servings = servings,
    ingredients = ingredientsSerialized
        .takeIf { it.isNotBlank() }
        ?.split(INGREDIENT_DELIMITER)
        ?.mapNotNull { token ->
            val pieces = token.split(VALUE_DELIMITER)
            if (pieces.size == 2) RecipeIngredient(name = pieces[1], quantity = pieces[0]) else null
        }
        ?: emptyList(),
    steps = stepsSerialized
        .takeIf { it.isNotBlank() }
        ?.split(STEP_DELIMITER)
        ?.filter { it.isNotBlank() }
        ?: emptyList(),
    macros = NutritionInfo(
        calories = calories,
        protein = protein,
        carbs = carbs,
        fats = fats,
    ),
    imageUrl = imageUrl,
)

private const val INGREDIENT_DELIMITER = "||"
private const val VALUE_DELIMITER = "::"
private const val STEP_DELIMITER = "||"
