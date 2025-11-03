package com.calisthenia.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calisthenia.core.model.MealType

@Entity(tableName = "recipe")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val mealType: MealType,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val ingredientsSerialized: String,
    val stepsSerialized: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val imageUrl: String?,
)
