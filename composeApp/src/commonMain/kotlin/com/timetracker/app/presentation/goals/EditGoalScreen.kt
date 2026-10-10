package com.timetracker.app.presentation.goals

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetracker.app.domain.model.GoalPeriod
import com.timetracker.app.domain.model.GoalType
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalScreen(
    goalId: Long?,
    onBack: () -> Unit,
    viewModel: EditGoalViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(goalId) {
        viewModel.loadGoal(goalId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Goal" else "New Goal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = state.activityId != null && (state.targetHours > 0 || state.targetMinutes > 0),
                    ) {
                        Text("Save")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Activity selector
            Text("Activity", style = MaterialTheme.typography.labelLarge)
            var expanded by remember { mutableStateOf(false) }
            val selectedActivity = state.activities.find { it.id == state.activityId }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = selectedActivity?.let { "${it.icon} ${it.name}".trim() } ?: "Select activity",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.activities.forEach { activity ->
                        DropdownMenuItem(
                            text = { Text("${activity.icon} ${activity.name}".trim()) },
                            onClick = { viewModel.updateActivityId(activity.id); expanded = false },
                            leadingIcon = {
                                Surface(
                                    modifier = Modifier.size(12.dp),
                                    color = Color(activity.color),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    content = {},
                                )
                            },
                        )
                    }
                }
            }

            // Goal type
            Text("Goal Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GoalType.entries.forEach { type ->
                    FilterChip(
                        selected = state.goalType == type,
                        onClick = { viewModel.updateGoalType(type) },
                        label = { Text(type.label) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Period
            Text("Period", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GoalPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = state.period == period,
                        onClick = { viewModel.updatePeriod(period) },
                        label = { Text(period.label) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Target duration
            Text("Target Duration", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.targetHours.toString(),
                    onValueChange = { it.toIntOrNull()?.let(viewModel::updateTargetHours) },
                    label = { Text("Hours") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.targetMinutes.toString(),
                    onValueChange = { it.toIntOrNull()?.let(viewModel::updateTargetMinutes) },
                    label = { Text("Minutes") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
        }
    }
}
