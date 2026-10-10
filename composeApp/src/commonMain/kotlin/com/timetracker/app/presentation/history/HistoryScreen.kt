package com.timetracker.app.presentation.history

import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.timetracker.app.presentation.components.CalendarView
import com.timetracker.app.util.formatDuration
import kotlinx.datetime.*
import kotlin.time.Clock
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
            TopAppBar(
                title = { Text("History") },
                actions = {
                    TextButton(onClick = { viewModel.toggleCalendar() }) {
                        Text(if (state.showCalendar) "List" else "Calendar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewEntry) {
                Icon(Icons.Filled.Add, contentDescription = "New entry")
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.showCalendar) {
            // Calendar view
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            ) {
                CalendarView(
                    yearMonth = state.calendarYear to state.calendarMonth,
                    daySummaries = state.daySummaries,
                    onDayClick = { date -> viewModel.selectDate(date) },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                )

                // Show entries for selected date
                state.selectedDate?.let { date ->
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = formatDateLabel(date),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))

                    val entries = state.entriesByDate[date]
                    if (entries != null && entries.isNotEmpty()) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(entries, key = { (entry, _) -> entry.id }) { (entry, activity) ->
                                EntryRow(
                                    entry = entry,
                                    activity = activity,
                                    onClick = { onEditEntry(entry.id) },
                                    onDelete = { viewModel.deleteEntry(entry.id) },
                                )
                            }
                        }
                    } else {
                        Text(
                            "No entries for this day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else if (state.entriesByDate.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No entries yet. Start tracking!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                // Bottom inset keeps the last row clear of the floating action button.
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp,
                ),
            ) {
                val entriesToShow = if (state.selectedDate != null) {
                    state.entriesByDate.filterKeys { it == state.selectedDate }
                } else {
                    state.entriesByDate
                }

                entriesToShow.forEach { (date, entries) ->
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
    Text(
        text = formatDateLabel(date),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

private fun formatDateLabel(date: LocalDate): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (date) {
        today -> "Today"
        today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
        else -> "${date.day} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
    }
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
                    modifier = Modifier.size(8.dp, 40.dp),
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
                    Text(activity.icon, modifier = Modifier.clearAndSetSemantics {})
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
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Delete entry",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun formatTime(dt: LocalDateTime): String {
    val h = if (dt.hour < 10) "0${dt.hour}" else "${dt.hour}"
    val m = if (dt.minute < 10) "0${dt.minute}" else "${dt.minute}"
    return "$h:$m"
}
