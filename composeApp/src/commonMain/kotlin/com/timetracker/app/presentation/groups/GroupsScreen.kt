package com.timetracker.app.presentation.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetracker.app.domain.model.ActivityGroup
import com.timetracker.app.presentation.components.ColorPicker
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onBack: () -> Unit,
    viewModel: GroupsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Add new group
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("New Group", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.newGroupName,
                            onValueChange = viewModel::updateNewGroupName,
                            label = { Text("Group name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Spacer(Modifier.height(8.dp))
                        ColorPicker(
                            selectedColor = state.newGroupColor,
                            onColorSelected = viewModel::updateNewGroupColor,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = viewModel::addGroup,
                            enabled = state.newGroupName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Add Group")
                        }
                    }
                }
            }

            items(state.groups, key = { it.id }) { group ->
                GroupItem(
                    group = group,
                    onDelete = { viewModel.deleteGroup(group.id) },
                    onEdit = { viewModel.startEditing(group) },
                )
            }
        }
    }

    // Edit dialog
    state.editingGroup?.let { group ->
        var editName by remember(group.id) { mutableStateOf(group.name) }
        var editColor by remember(group.id) { mutableStateOf(group.color) }

        AlertDialog(
            onDismissRequest = viewModel::cancelEditing,
            title = { Text("Edit Group") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    ColorPicker(selectedColor = editColor, onColorSelected = { editColor = it })
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.saveEditing(editName, editColor) }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelEditing) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun GroupItem(
    group: ActivityGroup,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${group.name}?") },
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

    Card(onClick = onEdit) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                color = Color(group.color),
                shape = MaterialTheme.shapes.small,
                content = {},
            )
            Spacer(Modifier.width(12.dp))
            Text(group.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = { showDeleteDialog = true }) {
                Text("\u2715", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
