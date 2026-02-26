package com.timetracker.app.presentation.entry

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    entryId: Long?,
    onBack: () -> Unit,
    viewModel: EditEntryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Entry" else "New Entry") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190") }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = state.activityId != null,
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
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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

            // Start time
            Text("Start Time", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = "%02d".format(state.startHour),
                    onValueChange = { it.toIntOrNull()?.coerceIn(0, 23)?.let(viewModel::updateStartHour) },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = "%02d".format(state.startMinute),
                    onValueChange = { it.toIntOrNull()?.coerceIn(0, 59)?.let(viewModel::updateStartMinute) },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            // End time
            Text("End Time", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = "%02d".format(state.endHour),
                    onValueChange = { it.toIntOrNull()?.coerceIn(0, 23)?.let(viewModel::updateEndHour) },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = "%02d".format(state.endMinute),
                    onValueChange = { it.toIntOrNull()?.coerceIn(0, 59)?.let(viewModel::updateEndMinute) },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            // Comment
            OutlinedTextField(
                value = state.comment,
                onValueChange = viewModel::updateComment,
                label = { Text("Comment") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
        }
    }
}
