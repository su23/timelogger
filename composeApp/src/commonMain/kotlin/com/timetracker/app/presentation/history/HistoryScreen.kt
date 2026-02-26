package com.timetracker.app.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.TimeEntry
import com.timetracker.app.util.formatDuration
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onEditEntry: (Long) -> Unit,
    onNewEntry: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("History") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewEntry) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.entriesByDate.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No entries yet. Start tracking!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
            ) {
                state.entriesByDate.forEach { (date, entries) ->
                    item(key = "header_$date") {
                        DateHeader(date)
                    }
                    items(entries, key = { (entry, _) -> entry.id }) { (entry, activity) ->
                        EntryRow(
                            entry = entry,
                            activity = activity,
                            onClick = { onEditEntry(entry.id) },
                            onDelete = { viewModel.deleteEntry(entry.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val label = when (date) {
        today -> "Today"
        today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
        else -> "${date.dayOfMonth} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun EntryRow(
    entry: TimeEntry,
    activity: Activity?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete entry?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (activity != null) {
                Box(
                    modifier = Modifier.size(8.dp, 40.dp)
                        .padding(end = 0.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(activity.color),
                        shape = MaterialTheme.shapes.extraSmall,
                        content = {},
                    )
                }
                Spacer(Modifier.width(12.dp))
                if (activity.icon.isNotEmpty()) {
                    Text(activity.icon)
                    Spacer(Modifier.width(8.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity?.name ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                )
                val tz = TimeZone.currentSystemDefault()
                val startLocal = entry.startTime.toLocalDateTime(tz)
                val endLocal = entry.endTime.toLocalDateTime(tz)
                Text(
                    text = "${formatTime(startLocal)} - ${formatTime(endLocal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (entry.comment.isNotEmpty()) {
                    Text(
                        text = entry.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = formatDuration(entry.durationMs),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { showDeleteDialog = true }) {
                Text("\u2715", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatTime(dt: LocalDateTime): String {
    return "%02d:%02d".format(dt.hour, dt.minute)
}
