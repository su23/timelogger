package com.timetracker.app.presentation.pomodoro

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetracker.app.domain.model.PomodoroPhase
import com.timetracker.app.util.formatDuration
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onBack: () -> Unit,
    viewModel: PomodoroViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Activity selector (only when idle)
            if (state.phase == PomodoroPhase.Idle) {
                var expanded by remember { mutableStateOf(false) }
                val selectedActivity = state.activities.find { it.id == state.selectedActivityId }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedActivity?.let { "${it.icon} ${it.name}".trim() } ?: "Select activity",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Activity") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        state.activities.forEach { activity ->
                            DropdownMenuItem(
                                text = { Text("${activity.icon} ${activity.name}".trim()) },
                                onClick = { viewModel.selectActivity(activity.id); expanded = false },
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
            } else {
                // Show current phase
                val phaseLabel = when (state.phase) {
                    PomodoroPhase.Work -> "Work"
                    PomodoroPhase.Break -> "Break"
                    PomodoroPhase.LongBreak -> "Long Break"
                    PomodoroPhase.Idle -> ""
                }
                val phaseColor = when (state.phase) {
                    PomodoroPhase.Work -> MaterialTheme.colorScheme.primary
                    PomodoroPhase.Break -> MaterialTheme.colorScheme.tertiary
                    PomodoroPhase.LongBreak -> MaterialTheme.colorScheme.secondary
                    PomodoroPhase.Idle -> MaterialTheme.colorScheme.onSurface
                }
                Text(
                    text = phaseLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = phaseColor,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Timer circle
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                val progress = if (state.totalPhaseMs > 0) {
                    1f - (state.remainingMs.toFloat() / state.totalPhaseMs)
                } else {
                    0f
                }
                val progressColor = when (state.phase) {
                    PomodoroPhase.Work -> MaterialTheme.colorScheme.primary
                    PomodoroPhase.Break -> MaterialTheme.colorScheme.tertiary
                    PomodoroPhase.LongBreak -> MaterialTheme.colorScheme.secondary
                    PomodoroPhase.Idle -> MaterialTheme.colorScheme.outlineVariant
                }
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    // Progress
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                // Time text
                Text(
                    text = formatDuration(state.remainingMs),
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Session counter
            Text(
                text = "Sessions: ${state.completedSessions}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when {
                    state.phase == PomodoroPhase.Idle -> {
                        Button(
                            onClick = { viewModel.start() },
                            enabled = state.selectedActivityId != null,
                        ) {
                            Text("Start")
                        }
                    }
                    state.isRunning -> {
                        OutlinedButton(onClick = { viewModel.pause() }) {
                            Text("Pause")
                        }
                        OutlinedButton(onClick = { viewModel.skipPhase() }) {
                            Text("Skip")
                        }
                        Button(
                            onClick = { viewModel.stop() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("Stop")
                        }
                    }
                    else -> {
                        Button(onClick = { viewModel.resume() }) {
                            Text("Resume")
                        }
                        Button(
                            onClick = { viewModel.stop() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("Stop")
                        }
                    }
                }
            }
        }
    }
}
