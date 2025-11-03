package com.calisthenia.feature.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.calisthenia.core.designsystem.CalisteniaTheme
import com.calisthenia.core.model.MealType
import com.calisthenia.core.model.Recipe
import com.calisthenia.core.model.label
import com.calisthenia.core.ui.LocalSpacing
import com.calisthenia.core.ui.components.CalisteniaScaffold
import com.calisthenia.core.ui.components.FilterChipRow
import com.calisthenia.core.ui.components.MetricChip
import com.calisthenia.core.ui.components.SectionCard
import com.calisthenia.core.ui.components.Tag
import com.calisthenia.feature.nutrition.R

@Composable
fun NutritionRoute(
    onBack: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NutritionScreen(
        state = state,
        onBack = onBack,
        onMealTypeSelected = viewModel::onMealTypeSelected,
        onPrepTimeSelected = viewModel::onPrepTimeSelected,
    )
}

@Composable
fun NutritionScreen(
    state: NutritionUiState,
    onBack: () -> Unit,
    onMealTypeSelected: (MealType?) -> Unit,
    onPrepTimeSelected: (Int?) -> Unit,
) {
    CalisteniaTheme {
        val spacing = LocalSpacing.current
        CalisteniaScaffold(
            title = stringResource(id = R.string.nutrition_title),
            onNavigateBack = onBack,
        ) { paddingValues ->
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(id = R.string.nutrition_loading),
                            modifier = Modifier.padding(top = spacing.small),
                        )
                    }
                }

                state.recipes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(spacing.large),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(id = R.string.nutrition_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(id = R.string.nutrition_empty_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = spacing.small),
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = spacing.large,
                            end = spacing.large,
                            top = spacing.large,
                            bottom = spacing.xLarge,
                        ),
                        verticalArrangement = Arrangement.spacedBy(spacing.large),
                    ) {
                        item {
                            FilterSection(
                                selectedMealType = state.selectedMealType,
                                selectedPrepTime = state.selectedPrepTime,
                                onMealTypeSelected = onMealTypeSelected,
                                onPrepTimeSelected = onPrepTimeSelected,
                            )
                        }

                        items(state.recipes) { recipe ->
                            RecipeCard(recipe = recipe)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedMealType: MealType?,
    selectedPrepTime: Int?,
    onMealTypeSelected: (MealType?) -> Unit,
    onPrepTimeSelected: (Int?) -> Unit,
) {
    val spacing = LocalSpacing.current
    val mealTypes = MealType.entries.toList()
    val mealItems = listOf(stringResource(id = R.string.nutrition_filter_all)) + mealTypes.map { it.label }
    SectionCard(title = stringResource(id = R.string.nutrition_filters_title)) {
        Text(
            text = stringResource(id = R.string.nutrition_filter_mealtype),
            style = MaterialTheme.typography.labelLarge,
        )
        FilterChipRow(
            items = mealItems,
            selected = setOf(selectedMealType?.label ?: mealItems.first()),
            onSelectionChanged = { label ->
                val type = mealTypes.firstOrNull { it.label == label }
                onMealTypeSelected(type)
            },
        )

        Text(
            text = stringResource(id = R.string.nutrition_filter_time),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = spacing.medium),
        )
        val prepOptions = listOf(null, 15, 30, 45)
        val prepLabels = prepOptions.map { option ->
            option?.let { stringResource(id = R.string.nutrition_filter_time_item, it) }
                ?: stringResource(id = R.string.nutrition_filter_time_all)
        }
        FilterChipRow(
            items = prepLabels,
            selected = setOf(
                selectedPrepTime?.let { stringResource(id = R.string.nutrition_filter_time_item, it) }
                    ?: stringResource(id = R.string.nutrition_filter_time_all),
            ),
            onSelectionChanged = { label ->
                val index = prepLabels.indexOf(label)
                val minutes = prepOptions.getOrNull(index)
                onPrepTimeSelected(minutes)
            },
        )
    }
}

@Composable
private fun RecipeCard(recipe: Recipe) {
    val spacing = LocalSpacing.current
    SectionCard(
        title = recipe.title,
        subtitle = recipe.description,
    ) {
        AsyncImage(
            model = recipe.imageUrl,
            contentDescription = recipe.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.medium),
            placeholder = painterResource(id = R.drawable.ic_recipe_placeholder),
            error = painterResource(id = R.drawable.ic_recipe_placeholder),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Tag(text = recipe.mealType.label)
            Tag(text = stringResource(id = R.string.nutrition_tag_time, recipe.prepTimeMinutes + recipe.cookTimeMinutes))
            Tag(text = stringResource(id = R.string.nutrition_tag_servings, recipe.servings))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            MetricChip(label = stringResource(id = R.string.nutrition_macro_protein), value = "${recipe.macros.protein.toInt()} g")
            MetricChip(label = stringResource(id = R.string.nutrition_macro_carbs), value = "${recipe.macros.carbs.toInt()} g")
            MetricChip(label = stringResource(id = R.string.nutrition_macro_fats), value = "${recipe.macros.fats.toInt()} g")
        }

        Column(
            modifier = Modifier.padding(top = spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
        ) {
            Text(
                text = stringResource(id = R.string.nutrition_ingredients_title),
                style = MaterialTheme.typography.titleSmall,
            )
            recipe.ingredients.take(4).forEach { ingredient ->
                Text(
                    text = "? ${ingredient.quantity} ${ingredient.name}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (recipe.ingredients.size > 4) {
                Text(
                    text = stringResource(id = R.string.nutrition_ingredients_more, recipe.ingredients.size - 4),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(
            modifier = Modifier.padding(top = spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
        ) {
            Text(
                text = stringResource(id = R.string.nutrition_steps_title),
                style = MaterialTheme.typography.titleSmall,
            )
            recipe.steps.take(3).forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (recipe.steps.size > 3) {
                Text(
                    text = stringResource(id = R.string.nutrition_steps_more, recipe.steps.size - 3),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
