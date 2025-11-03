package com.calisthenia.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calisthenia.core.model.UserProfile
import com.calisthenia.domain.usecase.GetUserProfileUseCase
import com.calisthenia.domain.usecase.UpsertUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getUserProfile: GetUserProfileUseCase,
    private val upsertUserProfile: UpsertUserProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUserProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    message = profile?.let { "?Hola ${it.displayName}!" }
                        ?: "Completa tus datos para personalizar tus rutinas",
                )
            }
        }
    }

    fun onContinueClicked() {
        viewModelScope.launch {
            if (_uiState.value.newUserProfile != null) {
                upsertUserProfile(_uiState.value.newUserProfile)
            }
        }
    }
}

data class OnboardingUiState(
    val message: String = "Configura tu perfil para comenzar",
    val newUserProfile: UserProfile? = null,
)
