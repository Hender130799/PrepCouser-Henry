package com.calisthenia.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calisthenia.core.designsystem.CalisteniaTheme
import com.calisthenia.feature.onboarding.R

@Composable
fun OnboardingRoute(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    OnboardingScreen(
        state = uiState,
        onContinue = {
            viewModel.onContinueClicked()
            onFinish()
        },
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onContinue: () -> Unit,
) {
    CalisteniaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(id = R.string.onboarding_title))
            Text(text = state.message)
            Button(onClick = onContinue) {
                Text(text = stringResource(id = R.string.onboarding_continue))
            }
        }
    }
}
