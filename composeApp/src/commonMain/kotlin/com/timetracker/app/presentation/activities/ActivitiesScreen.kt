package com.timetracker.app.presentation.activities

import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    onEditActivity: (Long) -> Unit,
    onNewActivity: () -> Unit,
    onManageGroups: () -> Unit,
    onBack: () -> Unit,
    viewModel: ActivitiesViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activities") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onManageGroups) { Text("Groups") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewActivity) {
                Icon(Icons.Filled.Add, contentDescription = "New activity")
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
            ) {
                items(state.activities, key = { it.id }) { activity ->
                    ActivityListItem(
                        activity = activity,
                        onEdit = { onEditActivity(activity.id) },
                        onArchiveToggle = {
                            if (activity.isArchived) viewModel.unarchiveActivity(activity.id)
                            else viewModel.archiveActivity(activity.id)
                        },
                        onDelete = { viewModel.deleteActivity(activity.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityListItem(
    activity: Activity,
    onEdit: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${activity.name}?") },
            text = { Text("This will also delete all time entries for this activity.") },
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
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (activity.isArchived)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = Color(activity.color),
                shape = MaterialTheme.shapes.small,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        if (activity.icon.isNotEmpty()) activity.icon else activity.name.take(1),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.name, style = MaterialTheme.typography.bodyLarge)
                if (activity.isArchived) {
                    Text("Archived", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(onClick = onArchiveToggle) {
                Text(if (activity.isArchived) "Restore" else "Archive")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Delete ${activity.name}",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
