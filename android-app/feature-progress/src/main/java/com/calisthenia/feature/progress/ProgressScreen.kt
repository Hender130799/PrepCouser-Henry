package com.calisthenia.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calisthenia.core.designsystem.CalisteniaTheme
import com.calisthenia.core.model.ProgressEntry
import com.calisthenia.core.ui.LocalSpacing
import com.calisthenia.core.ui.components.CalisteniaScaffold
import com.calisthenia.core.ui.components.MetricChip
import com.calisthenia.core.ui.components.SectionCard
import com.calisthenia.feature.progress.R
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ProgressRoute(onBack: () -> Unit, viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ProgressScreen(
        state = state,
        onBack = onBack,
        onAddQuickEntry = viewModel::addQuickEntry,
        onDeleteEntry = viewModel::deleteEntry,
    )
}

@Composable
fun ProgressScreen(
    state: ProgressUiState,
    onBack: () -> Unit,
    onAddQuickEntry: () -> Unit,
    onDeleteEntry: (String) -> Unit,
) {
    CalisteniaTheme {
        val spacing = LocalSpacing.current
        CalisteniaScaffold(title = stringResource(id = R.string.progress_title), onNavigateBack = onBack) { paddingValues ->
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
                            SummaryCard(state = state, onAddQuickEntry = onAddQuickEntry)
                        }

                        items(state.entries) { entry ->
                            EntryCard(entry = entry, onDelete = { onDeleteEntry(entry.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(state: ProgressUiState, onAddQuickEntry: () -> Unit) {
    val spacing = LocalSpacing.current
    SectionCard(title = stringResource(id = R.string.progress_summary_title)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
            MetricChip(
                label = stringResource(id = R.string.progress_metric_latest),
                value = state.lastWeight?.let { "${String.format("%.1f", it)} kg" } ?: "--",
            )
            MetricChip(
                label = stringResource(id = R.string.progress_metric_entries),
                value = state.entries.size.toString(),
            )
        }
        Button(
            onClick = onAddQuickEntry,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.medium),
        ) {
            Text(text = stringResource(id = R.string.progress_add_quick))
        }
    }
}

@Composable
private fun EntryCard(entry: ProgressEntry, onDelete: () -> Unit) {
    SectionCard(title = formatDate(entry.timestamp), subtitle = entry.workoutNotes, onRefresh = null) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = entry.weightKg?.let { stringResource(id = R.string.progress_entry_weight, String.format("%.1f", it)) }
                    ?: stringResource(id = R.string.progress_entry_no_weight),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(id = R.string.progress_entry_delete))
                }
            }
        }
    }
}

private fun formatDate(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val year = dateTime.year
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$day/$month/$year ? $hour:$minute"
}
