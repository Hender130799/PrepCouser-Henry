package com.calisthenia.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calisthenia.core.designsystem.CalisteniaTheme
import com.calisthenia.core.ui.LocalSpacing
import com.calisthenia.core.ui.components.CalisteniaScaffold
import com.calisthenia.core.ui.components.FilterChipRow
import com.calisthenia.core.ui.components.SectionCard
import com.calisthenia.feature.settings.R

@Composable
fun SettingsRoute(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onBack = onBack,
        onNotificationsChanged = viewModel::onNotificationsChanged,
        onBiometricsChanged = viewModel::onBiometricsChanged,
        onLanguageSelected = viewModel::onLanguageSelected,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onBiometricsChanged: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    CalisteniaTheme {
        val spacing = LocalSpacing.current
        CalisteniaScaffold(title = stringResource(id = R.string.settings_title), onNavigateBack = onBack) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = spacing.large, vertical = spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.large),
            ) {
                SectionCard(title = stringResource(id = R.string.settings_notifications_title)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(id = R.string.settings_notifications_body))
                        }
                        Switch(checked = state.notificationsEnabled, onCheckedChange = onNotificationsChanged)
                    }
                }

                SectionCard(title = stringResource(id = R.string.settings_security_title)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(id = R.string.settings_security_body))
                        }
                        Switch(checked = state.biometricsEnabled, onCheckedChange = onBiometricsChanged)
                    }
                }

                SectionCard(title = stringResource(id = R.string.settings_language_title)) {
                    val items = listOf(AppLanguage.SPANISH, AppLanguage.ENGLISH)
                    FilterChipRow(
                        items = items.map { languageLabel(it) },
                        selected = setOf(languageLabel(state.language)),
                        onSelectionChanged = { label ->
                            items.firstOrNull { languageLabel(it) == label }?.let(onLanguageSelected)
                        },
                    )
                }
            }
        }
    }
}

private fun languageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.SPANISH -> "Espa?ol"
    AppLanguage.ENGLISH -> "Ingl?s"
}
