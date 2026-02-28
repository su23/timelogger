package com.timetracker.app.presentation.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetracker.app.domain.model.GoalProgress
import com.timetracker.app.domain.model.GoalType
import com.timetracker.app.util.formatDurationLong
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNewGoal: () -> Unit,
    onEditGoal: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: GoalsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewGoal) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.goalProgressList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No goals set", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Set time goals for your activities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.goalProgressList, key = { it.goal.id }) { progress ->
                    GoalCard(
                        progress = progress,
                        onClick = { onEditGoal(progress.goal.id) },
                        onDelete = { viewModel.deleteGoal(progress.goal.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    progress: GoalProgress,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete goal?") },
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

    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(8.dp, 36.dp),
                    color = Color(progress.activity.color),
                    shape = MaterialTheme.shapes.extraSmall,
                    content = {},
                )
                Spacer(Modifier.width(12.dp))
                if (progress.activity.icon.isNotEmpty()) {
                    Text(progress.activity.icon)
                    Spacer(Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(progress.activity.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${progress.goal.goalType.label} ${formatDurationLong(progress.goal.targetMs)} / ${progress.goal.period.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Text("\u2715", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (progress.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = when {
                    progress.goal.goalType == GoalType.Max && progress.isReached -> MaterialTheme.colorScheme.error
                    progress.isReached -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    formatDurationLong(progress.currentMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${progress.progressPercent.toLong()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (progress.isReached && progress.goal.goalType == GoalType.Min) {
                        MaterialTheme.colorScheme.tertiary
                    } else if (progress.isReached && progress.goal.goalType == GoalType.Max) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
