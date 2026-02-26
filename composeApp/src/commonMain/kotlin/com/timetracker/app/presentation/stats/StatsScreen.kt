package com.timetracker.app.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.timetracker.app.domain.model.ActivityStats
import com.timetracker.app.presentation.components.DateRangeSelector
import com.timetracker.app.util.formatDurationLong
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onExport: () -> Unit,
    viewModel: StatsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                actions = {
                    TextButton(onClick = onExport) { Text("Export") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                DateRangeSelector(
                    selected = state.rangeOption,
                    onSelected = viewModel::onRangeChanged,
                )
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.activityStats.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No data for this period", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                // Pie chart
                item {
                    PieChart(
                        stats = state.activityStats,
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                    )
                }

                // Total
                item {
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium)
                            Text(
                                formatDurationLong(state.totalMs),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // Activity breakdown
                items(state.activityStats) { stat ->
                    StatRow(stat)
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    stats: List<ActivityStats>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height) - 32f
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f,
        )
        var startAngle = -90f
        stats.forEach { stat ->
            val sweep = stat.percentage * 3.6f
            drawArc(
                color = Color(stat.activity.color),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = Size(diameter, diameter),
            )
            startAngle += sweep
        }
        // White center circle for donut effect
        val innerDiameter = diameter * 0.55f
        val innerTopLeft = Offset(
            (size.width - innerDiameter) / 2f,
            (size.height - innerDiameter) / 2f,
        )
        drawCircle(
            color = Color.White,
            radius = innerDiameter / 2f,
            center = Offset(size.width / 2f, size.height / 2f),
        )
    }
}

@Composable
private fun StatRow(stat: ActivityStats) {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(8.dp, 36.dp),
                color = Color(stat.activity.color),
                shape = MaterialTheme.shapes.extraSmall,
                content = {},
            )
            Spacer(Modifier.width(12.dp))
            if (stat.activity.icon.isNotEmpty()) {
                Text(stat.activity.icon)
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stat.activity.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${stat.entryCount} entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatDurationLong(stat.totalDurationMs),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    "${"%.1f".format(stat.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
