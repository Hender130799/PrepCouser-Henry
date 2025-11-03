package com.calisthenia.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calisthenia.core.model.ProgressEntry
import com.calisthenia.domain.usecase.AddProgressEntryUseCase
import com.calisthenia.domain.usecase.DeleteProgressEntryUseCase
import com.calisthenia.domain.usecase.ObserveProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val observeProgressUseCase: ObserveProgressUseCase,
    private val addProgressEntryUseCase: AddProgressEntryUseCase,
    private val deleteProgressEntryUseCase: DeleteProgressEntryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeProgressUseCase().collect { entries ->
                val ordered = entries.sortedByDescending { it.timestamp }
                _uiState.update { state ->
                    state.copy(
                        entries = ordered,
                        isLoading = false,
                        lastWeight = ordered.firstOrNull { it.weightKg != null }?.weightKg,
                    )
                }
            }
        }
    }

    fun addQuickEntry() {
        viewModelScope.launch {
            val now = Clock.System.now()
            val lastWeight = _uiState.value.lastWeight ?: 70.0
            val newWeight = (lastWeight + QUICK_ENTRY_DELTA).coerceAtLeast(40.0)
            val entry = ProgressEntry(
                id = UUID.randomUUID().toString(),
                userId = "session",
                timestamp = now,
                weightKg = newWeight,
                bodyFatPercentage = null,
                measurements = null,
                workoutNotes = "Registro r?pido",
                imageUrl = null,
            )
            addProgressEntryUseCase(entry)
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            deleteProgressEntryUseCase(id)
        }
    }

    companion object {
        private const val QUICK_ENTRY_DELTA = 0.2
    }
}

data class ProgressUiState(
    val entries: List<ProgressEntry> = emptyList(),
    val isLoading: Boolean = true,
    val lastWeight: Double? = null,
)
