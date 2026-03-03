package com.timetracker.app.presentation.activities

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timetracker.app.data.repository.CustomFieldRepository
import com.timetracker.app.domain.model.CustomField
import com.timetracker.app.domain.model.CustomFieldType
import com.timetracker.app.presentation.components.ColorPicker
import com.timetracker.app.presentation.components.IconPicker
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityScreen(
    activityId: Long?,
    onBack: () -> Unit,
    viewModel: EditActivityViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(activityId) {
        viewModel.loadActivity(activityId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Activity" else "New Activity") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190") }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = state.name.isNotBlank(),
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
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text("Icon", style = MaterialTheme.typography.labelLarge)
            IconPicker(
                selectedIcon = state.icon,
                onIconSelected = viewModel::updateIcon,
            )

            Text("Color", style = MaterialTheme.typography.labelLarge)
            ColorPicker(
                selectedColor = state.color,
                onColorSelected = viewModel::updateColor,
            )

            // Group selector
            if (state.groups.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                val selectedGroup = state.groups.find { it.id == state.groupId }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedGroup?.name ?: "No group",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("No group") },
                            onClick = { viewModel.updateGroupId(null); expanded = false },
                        )
                        state.groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = { viewModel.updateGroupId(group.id); expanded = false },
                            )
                        }
                    }
                }
            }

            // Custom fields management (only when editing existing activity)
            if (state.isEditing && activityId != null) {
                CustomFieldsSection(activityId = activityId)
            }
        }
    }
}

@Composable
private fun CustomFieldsSection(activityId: Long) {
    val customFieldRepo = koinInject<CustomFieldRepository>()
    val fields by customFieldRepo.observeByActivityId(activityId).collectAsState(initial = emptyList())
    var showAddField by remember { mutableStateOf(false) }
    var newFieldName by remember { mutableStateOf("") }
    var newFieldType by remember { mutableStateOf(CustomFieldType.Text) }

    Text("Custom Fields", style = MaterialTheme.typography.labelLarge)

    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            fields.forEach { field ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text("${field.name} (${field.fieldType.label})", style = MaterialTheme.typography.bodySmall)
                    var deleting by remember { mutableStateOf(false) }
                    if (deleting) {
                        LaunchedEffect(Unit) {
                            customFieldRepo.deleteField(field.id)
                            deleting = false
                        }
                    }
                    TextButton(onClick = { deleting = true }) {
                        Text("\u2715", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (showAddField) {
                OutlinedTextField(
                    value = newFieldName,
                    onValueChange = { newFieldName = it },
                    label = { Text("Field name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CustomFieldType.entries.forEach { type ->
                        FilterChip(
                            selected = newFieldType == type,
                            onClick = { newFieldType = type },
                            label = { Text(type.label) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var saving by remember { mutableStateOf(false) }
                    if (saving && newFieldName.isNotBlank()) {
                        LaunchedEffect(Unit) {
                            customFieldRepo.insertField(
                                CustomField(
                                    activityId = activityId,
                                    name = newFieldName.trim(),
                                    fieldType = newFieldType,
                                    sortOrder = fields.size,
                                )
                            )
                            newFieldName = ""
                            showAddField = false
                            saving = false
                        }
                    }
                    TextButton(onClick = { saving = true }, enabled = newFieldName.isNotBlank()) {
                        Text("Add")
                    }
                    TextButton(onClick = { showAddField = false; newFieldName = "" }) {
                        Text("Cancel")
                    }
                }
            } else {
                TextButton(onClick = { showAddField = true }) {
                    Text("+ Add Custom Field")
                }
            }
        }
    }
}
