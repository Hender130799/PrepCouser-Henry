package com.calisthenia.feature.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calisthenia.core.model.MealType
import com.calisthenia.core.model.Recipe
import com.calisthenia.domain.repository.RecipeFilter
import com.calisthenia.domain.usecase.ObserveRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val observeRecipesUseCase: ObserveRecipesUseCase,
) : ViewModel() {

    private val filterState = MutableStateFlow(RecipeFilter())
    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            filterState
                .flatMapLatest { filter -> observeRecipesUseCase(filter) }
                .collect { recipes ->
                    _uiState.update { it.copy(recipes = recipes, isLoading = false) }
                }
        }
    }

    fun onMealTypeSelected(type: MealType?) {
        filterState.update { it.copy(mealType = type) }
        _uiState.update { it.copy(selectedMealType = type) }
    }

    fun onPrepTimeSelected(minutes: Int?) {
        filterState.update { it.copy(maxPrepTimeMinutes = minutes) }
        _uiState.update { it.copy(selectedPrepTime = minutes) }
    }
}

data class NutritionUiState(
    val recipes: List<Recipe> = emptyList(),
    val selectedMealType: MealType? = null,
    val selectedPrepTime: Int? = null,
    val isLoading: Boolean = true,
)
