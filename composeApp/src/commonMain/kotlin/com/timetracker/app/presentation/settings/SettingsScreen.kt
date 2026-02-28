package com.timetracker.app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190") }
                },
            )
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Theme section
                item {
                    Text("Appearance", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Theme", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ThemeOption.entries.forEach { option ->
                                    FilterChip(
                                        selected = state.theme == option,
                                        onClick = { viewModel.setTheme(option) },
                                        label = { Text(option.label) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }

                // Timer section
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Timer", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allow simultaneous timers", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Run multiple activities at once",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = state.allowSimultaneous,
                                onCheckedChange = viewModel::setAllowSimultaneous,
                            )
                        }
                    }
                }

                // Pomodoro section
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Pomodoro Defaults", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            NumberSetting(
                                label = "Work duration (min)",
                                value = state.pomodoroWorkMinutes,
                                onValueChange = viewModel::setPomodoroWorkMinutes,
                            )
                            NumberSetting(
                                label = "Break duration (min)",
                                value = state.pomodoroBreakMinutes,
                                onValueChange = viewModel::setPomodoroBreakMinutes,
                            )
                            NumberSetting(
                                label = "Long break duration (min)",
                                value = state.pomodoroLongBreakMinutes,
                                onValueChange = viewModel::setPomodoroLongBreakMinutes,
                            )
                            NumberSetting(
                                label = "Sessions before long break",
                                value = state.pomodoroSessionsBeforeLong,
                                onValueChange = viewModel::setPomodoroSessionsBeforeLong,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberSetting(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextButton(
                onClick = { onValueChange(value - 1) },
                enabled = value > 1,
            ) {
                Text("-")
            }
            Text("$value", style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = { onValueChange(value + 1) }) {
                Text("+")
            }
        }
    }
}
