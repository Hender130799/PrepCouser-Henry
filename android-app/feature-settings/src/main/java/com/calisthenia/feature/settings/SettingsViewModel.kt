package com.calisthenia.feature.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onNotificationsChanged(enabled: Boolean) = _uiState.update { it.copy(notificationsEnabled = enabled) }

    fun onBiometricsChanged(enabled: Boolean) = _uiState.update { it.copy(biometricsEnabled = enabled) }

    fun onLanguageSelected(language: AppLanguage) = _uiState.update { it.copy(language = language) }
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val biometricsEnabled: Boolean = false,
    val language: AppLanguage = AppLanguage.SPANISH,
)

enum class AppLanguage { SPANISH, ENGLISH }
