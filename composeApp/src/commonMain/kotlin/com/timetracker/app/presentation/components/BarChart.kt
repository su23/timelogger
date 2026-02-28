package com.timetracker.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.DailyTotal

@Composable
fun BarChart(
    dailyTotals: List<DailyTotal>,
    activities: Map<Long, Activity>,
    modifier: Modifier = Modifier,
) {
    if (dailyTotals.isEmpty()) return

    // Group totals by day bucket
    val byDay = mutableMapOf<Long, MutableList<DailyTotal>>()
    for (total in dailyTotals) {
        byDay.getOrPut(total.dayEpoch) { mutableListOf() }.add(total)
    }
    val sortedDays = byDay.keys.sorted()

    if (sortedDays.isEmpty()) return

    // Calculate max total per day
    var maxMs = 1L
    val dayData = mutableListOf<Triple<String, Long, List<Pair<Int, Long>>>>() // label, totalMs, segments(color, durationMs)

    for (dayBucket in sortedDays) {
        val totals = byDay[dayBucket] ?: continue
        var dayTotal = 0L
        val segments = mutableListOf<Pair<Int, Long>>()
        for (t in totals) {
            val act = activities[t.activityId]
            val color = act?.color ?: 0xFF9E9E9E.toInt()
            segments.add(color to t.totalDurationMs)
            dayTotal += t.totalDurationMs
        }
        segments.sortByDescending { p: Pair<Int, Long> -> p.second }
        if (dayTotal > maxMs) maxMs = dayTotal

        // Simple day label from bucket
        val label = "${(dayBucket % 31 + 1).toInt()}"
        dayData.add(Triple(label, dayTotal, segments.toList()))
    }

    val barWidth = 40.dp
    val barSpacing = 8.dp
    val chartHeight = 180.dp
    val labelHeight = 24.dp

    val scrollState = rememberScrollState()
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight + labelHeight)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(barSpacing),
            verticalAlignment = Alignment.Bottom,
        ) {
            for (i in dayData.indices) {
                val triple = dayData[i]
                val label = triple.first
                val dayTotal = triple.second
                val segments = triple.third

                Column(
                    modifier = Modifier.width(barWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Stacked bar
                    val barHeight = chartHeight * (dayTotal.toFloat() / maxMs)
                    Canvas(
                        modifier = Modifier
                            .width(barWidth)
                            .height(barHeight),
                    ) {
                        var yOffset = 0f
                        for (seg in segments) {
                            val segColor = seg.first
                            val segDuration = seg.second
                            val segmentHeight = size.height * (segDuration.toFloat() / dayTotal)
                            drawRect(
                                color = Color(segColor),
                                topLeft = Offset(0f, yOffset),
                                size = Size(size.width, segmentHeight),
                            )
                            yOffset += segmentHeight
                        }
                    }

                    // Day label
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = textColor,
                        modifier = Modifier.height(labelHeight).padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
