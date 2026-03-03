package com.timetracker.app.presentation.entry

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetracker.app.data.repository.CustomFieldRepository
import com.timetracker.app.domain.model.CustomField
import com.timetracker.app.domain.model.CustomFieldType
import com.timetracker.app.util.padTwoInt
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    entryId: Long?,
    onBack: () -> Unit,
    viewModel: EditEntryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val customFieldRepo = koinInject<CustomFieldRepository>()

    // Load custom fields for selected activity
    val customFields by remember(state.activityId) {
        if (state.activityId != null) {
            customFieldRepo.observeByActivityId(state.activityId!!)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    // Track custom field values locally
    val customFieldValues = remember { mutableStateMapOf<Long, String>() }

    // Load existing custom field values when editing
    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
        if (entryId != null) {
            customFieldRepo.observeValuesByEntryId(entryId).collect { values ->
                values.forEach { cfv ->
                    customFieldValues[cfv.fieldId] = cfv.value
                }
            }
        }
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

            // Start time
            Text("Start Time", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = padTwoInt(state.startHour),
                    onValueChange = { it.toIntOrNull()?.coerceIn(0, 23)?.let(viewModel::updateStartHour) },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = padTwoInt(state.startMinute),
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
                    value = padTwoInt(state.endHour),
                    onValueChange = { it.toIntOrNull()?.coerceIn(0, 23)?.let(viewModel::updateEndHour) },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = padTwoInt(state.endMinute),
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

            // Custom fields
            if (customFields.isNotEmpty()) {
                Text("Custom Fields", style = MaterialTheme.typography.labelLarge)
                customFields.forEach { field ->
                    CustomFieldInput(
                        field = field,
                        value = customFieldValues[field.id] ?: "",
                        onValueChange = { customFieldValues[field.id] = it },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomFieldInput(
    field: CustomField,
    value: String,
    onValueChange: (String) -> Unit,
) {
    when (field.fieldType) {
        CustomFieldType.Text -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(field.name) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        CustomFieldType.Number -> {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                        onValueChange(newValue)
                    }
                },
                label = { Text(field.name) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        CustomFieldType.Dropdown -> {
            val options = field.options.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = value.ifEmpty { "Select ${field.name}" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(field.name) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { onValueChange(option); expanded = false },
                        )
                    }
                }
            }
        }
    }
}
