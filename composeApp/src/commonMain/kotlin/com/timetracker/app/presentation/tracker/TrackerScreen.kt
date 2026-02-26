package com.timetracker.app.presentation.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    viewModel: TrackerViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Time Tracker") },
            actions = {
                TextButton(onClick = onManageActivities) {
                    Text("Activities")
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
                        formatDuration(viewModel.getElapsedMs(it, state.currentTimeMs))
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
                RunningTimerRow(
                    activity = activity,
                    timer = timer,
                    elapsedMs = getElapsedMs(timer, currentTimeMs),
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
                Text(
                    formatDuration(elapsedMs),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(onClick = onPauseResume) {
                Text(if (timer.isPaused) "\u25B6" else "\u23F8")
            }
            TextButton(onClick = onStop) {
                Text("\u23F9", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
