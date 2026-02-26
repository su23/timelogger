package com.timetracker.app.presentation.export

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timetracker.app.presentation.components.DateRangeSelector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: ExportViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DateRangeSelector(
                selected = state.rangeOption,
                onSelected = viewModel::onRangeChanged,
            )

            // Format toggle
            Text("Format", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportFormat.entries.forEach { format ->
                    FilterChip(
                        selected = state.format == format,
                        onClick = { viewModel.onFormatChanged(format) },
                        label = { Text(format.label) },
                    )
                }
            }

            Button(
                onClick = viewModel::export,
                enabled = !state.isExporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Generate Export")
                }
            }

            // Preview
            state.exportedContent?.let { content ->
                HorizontalDivider()
                Text("Preview", style = MaterialTheme.typography.titleSmall)
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    Text(
                        text = content.take(5000),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState()),
                    )
                }
                Text(
                    "Content copied to clipboard / use platform share",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
