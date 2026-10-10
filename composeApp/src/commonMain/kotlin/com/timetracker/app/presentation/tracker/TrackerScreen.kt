package com.timetracker.app.presentation.tracker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetracker.app.domain.model.ActiveTimer
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.presentation.components.ActivityCard
import com.timetracker.app.util.formatDuration
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    onManageActivities: () -> Unit,
    onSettings: () -> Unit = {},
    onPomodoro: () -> Unit = {},
    viewModel: TrackerViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Countdown dialog state
    var showCountdownDialog by remember { mutableStateOf(false) }
    var countdownActivityId by remember { mutableStateOf<Long?>(null) }
    var countdownHours by remember { mutableStateOf(0) }
    var countdownMinutes by remember { mutableStateOf(25) }

    if (showCountdownDialog && countdownActivityId != null) {
        AlertDialog(
            onDismissRequest = { showCountdownDialog = false },
            title = { Text("Set Countdown") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Timer will count down to zero", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = countdownHours.toString(),
                            onValueChange = { it.toIntOrNull()?.coerceIn(0, 23)?.let { h -> countdownHours = h } },
                            label = { Text("Hours") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = countdownMinutes.toString(),
                            onValueChange = { it.toIntOrNull()?.coerceIn(0, 59)?.let { m -> countdownMinutes = m } },
                            label = { Text("Minutes") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val targetMs = (countdownHours * 3600000L) + (countdownMinutes * 60000L)
                    if (targetMs > 0) {
                        viewModel.startWithCountdown(countdownActivityId!!, targetMs)
                    }
                    showCountdownDialog = false
                }) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCountdownDialog = false }) { Text("Cancel") }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Time Tracker") },
            actions = {
                IconButton(onClick = onPomodoro) {
                    Icon(Icons.Filled.Timer, contentDescription = "Pomodoro")
                }
                TextButton(onClick = onManageActivities) {
                    Text("Activities")
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            },
        )

        // Active timers section
        if (state.activeTimers.isNotEmpty()) {
            ActiveTimersSection(
                timers = state.activeTimers,
                activities = state.activities,
                currentTimeMs = state.currentTimeMs,
                onStop = viewModel::onActivityClick,
                onPauseResume = viewModel::onPauseResume,
                getElapsedMs = viewModel::getElapsedMs,
                getCountdownRemainingMs = viewModel::getCountdownRemainingMs,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Activity grid
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.activities.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No activities yet", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onManageActivities) {
                        Text("Add Activities")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.activities, key = { it.id }) { activity ->
                    val timer = state.activeTimers.find { it.activityId == activity.id }
                    val isRunning = timer != null
                    val elapsedText = timer?.let {
                        val remaining = viewModel.getCountdownRemainingMs(it, state.currentTimeMs)
                        if (remaining != null) {
                            // Show countdown
                            formatDuration(remaining.coerceAtLeast(0))
                        } else {
                            formatDuration(viewModel.getElapsedMs(it, state.currentTimeMs))
                        }
                    }
                    ActivityCard(
                        activity = activity,
                        isRunning = isRunning,
                        elapsedText = elapsedText,
                        onClick = { viewModel.onActivityClick(activity.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTimersSection(
    timers: List<ActiveTimer>,
    activities: List<Activity>,
    currentTimeMs: Long,
    onStop: (Long) -> Unit,
    onPauseResume: (Long) -> Unit,
    getElapsedMs: (ActiveTimer, Long) -> Long,
    getCountdownRemainingMs: (ActiveTimer, Long) -> Long?,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Running",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        timers.forEach { timer ->
            val activity = activities.find { it.id == timer.activityId }
            if (activity != null) {
                val countdownRemaining = getCountdownRemainingMs(timer, currentTimeMs)
                RunningTimerRow(
                    activity = activity,
                    timer = timer,
                    elapsedMs = getElapsedMs(timer, currentTimeMs),
                    countdownRemainingMs = countdownRemaining,
                    onStop = { onStop(activity.id) },
                    onPauseResume = { onPauseResume(activity.id) },
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun RunningTimerRow(
    activity: Activity,
    timer: ActiveTimer,
    elapsedMs: Long,
    countdownRemainingMs: Long?,
    onStop: () -> Unit,
    onPauseResume: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(activity.color).copy(alpha = 0.15f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (activity.icon.isNotEmpty()) {
                Text(activity.icon, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.name, style = MaterialTheme.typography.bodyMedium)
                if (countdownRemainingMs != null) {
                    // Show countdown
                    Text(
                        formatDuration(countdownRemainingMs.coerceAtLeast(0)),
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (countdownRemainingMs <= 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                    )
                    // Show progress
                    if (timer.targetDurationMs != null && timer.targetDurationMs > 0) {
                        val progress = 1f - (countdownRemainingMs.toFloat() / timer.targetDurationMs)
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        )
                    }
                } else {
                    Text(
                        formatDuration(elapsedMs),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = onPauseResume) {
                Icon(
                    if (timer.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (timer.isPaused) "Resume timer" else "Pause timer",
                )
            }
            IconButton(onClick = onStop) {
                Icon(
                    Icons.Filled.Stop,
                    contentDescription = "Stop timer",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
