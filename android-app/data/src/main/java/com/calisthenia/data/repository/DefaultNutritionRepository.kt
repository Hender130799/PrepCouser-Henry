package com.calisthenia.data.repository

import com.calisthenia.core.database.dao.RecipeDao
import com.calisthenia.core.database.entity.RecipeEntity
import com.calisthenia.core.model.MealType
import com.calisthenia.core.model.NutritionInfo
import com.calisthenia.core.model.Recipe
import com.calisthenia.core.model.RecipeCatalog
import com.calisthenia.core.model.RecipeIngredient
import com.calisthenia.core.network.FirebaseSources
import com.calisthenia.domain.repository.NutritionRepository
import com.calisthenia.domain.repository.RecipeFilter
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Singleton
class DefaultNutritionRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    private val firebaseSources: FirebaseSources,
) : NutritionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            runCatching { refreshFromRemote() }
                .onFailure {
                    // seed local if remote fails
                    if (recipeDao.count() == 0) {
                        recipeDao.insertAll(RecipeCatalog.recipes.map { it.toEntity() })
                        runCatching { pushAllToRemote() }
                    }
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
        refreshFromRemote()
    }

    private suspend fun refreshFromRemote() {
        val snapshot = firebaseSources.firestore
            .collection(COLLECTION_RECIPES)
            .get()
            .await()

        val remoteEntities = snapshot.documents.mapNotNull { it.toRecipeEntity() }
        if (remoteEntities.isNotEmpty()) {
            recipeDao.deleteAll()
            recipeDao.insertAll(remoteEntities)
        } else if (recipeDao.count() == 0) {
            recipeDao.insertAll(RecipeCatalog.recipes.map { it.toEntity() })
            pushAllToRemote()
        }
    }

    private suspend fun pushAllToRemote() {
        val batch = firebaseSources.firestore.batch()
        val collection = firebaseSources.firestore.collection(COLLECTION_RECIPES)
        val localEntities = recipeDao.observeRecipes().awaitFirst()
        localEntities.forEach { entity ->
            val doc = collection.document(entity.id)
            batch.set(doc, entity.toRemoteMap())
        }
        batch.commit().await()
    }
}

private suspend fun <T> Flow<T>.awaitFirst(): T = first()

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

private fun RecipeEntity.toRemoteMap(): Map<String, Any?> = mapOf(
    Field.ID to id,
    Field.TITLE to title,
    Field.DESCRIPTION to description,
    Field.MEAL_TYPE to mealType.name,
    Field.PREP to prepTimeMinutes,
    Field.COOK to cookTimeMinutes,
    Field.SERVINGS to servings,
    Field.INGREDIENTS to ingredientsSerialized,
    Field.STEPS to stepsSerialized,
    Field.CALORIES to calories,
    Field.PROTEIN to protein,
    Field.CARBS to carbs,
    Field.FATS to fats,
    Field.IMAGE to imageUrl,
)

private fun DocumentSnapshot.toRecipeEntity(): RecipeEntity? {
    if (!exists()) return null
    val id = getString(Field.ID) ?: return null
    val title = getString(Field.TITLE) ?: return null
    val description = getString(Field.DESCRIPTION) ?: ""
    val mealTypeString = getString(Field.MEAL_TYPE) ?: MealType.DESAYUNO.name
    val mealType = runCatching { MealType.valueOf(mealTypeString) }.getOrDefault(MealType.DESAYUNO)
    val prep = getLong(Field.PREP)?.toInt() ?: 0
    val cook = getLong(Field.COOK)?.toInt() ?: 0
    val servings = getLong(Field.SERVINGS)?.toInt() ?: 1
    val ingredients = getString(Field.INGREDIENTS) ?: ""
    val steps = getString(Field.STEPS) ?: ""
    val calories = getLong(Field.CALORIES)?.toInt() ?: 0
    val protein = getDouble(Field.PROTEIN) ?: 0.0
    val carbs = getDouble(Field.CARBS) ?: 0.0
    val fats = getDouble(Field.FATS) ?: 0.0
    val image = getString(Field.IMAGE)

    return RecipeEntity(
        id = id,
        title = title,
        description = description,
        mealType = mealType,
        prepTimeMinutes = prep,
        cookTimeMinutes = cook,
        servings = servings,
        ingredientsSerialized = ingredients,
        stepsSerialized = steps,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fats = fats,
        imageUrl = image,
    )
}

private const val INGREDIENT_DELIMITER = "||"
private const val VALUE_DELIMITER = "::"
private const val STEP_DELIMITER = "||"
private const val COLLECTION_RECIPES = "recipes"

private object Field {
    const val ID = "id"
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val MEAL_TYPE = "mealType"
    const val PREP = "prepTimeMinutes"
    const val COOK = "cookTimeMinutes"
    const val SERVINGS = "servings"
    const val INGREDIENTS = "ingredientsSerialized"
    const val STEPS = "stepsSerialized"
    const val CALORIES = "calories"
    const val PROTEIN = "protein"
    const val CARBS = "carbs"
    const val FATS = "fats"
    const val IMAGE = "imageUrl"
}
