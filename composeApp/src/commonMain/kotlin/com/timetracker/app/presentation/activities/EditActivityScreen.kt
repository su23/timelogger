package com.timetracker.app.presentation.activities

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timetracker.app.presentation.components.ColorPicker
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

            OutlinedTextField(
                value = state.icon,
                onValueChange = viewModel::updateIcon,
                label = { Text("Icon (emoji)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
        }
    }
}
