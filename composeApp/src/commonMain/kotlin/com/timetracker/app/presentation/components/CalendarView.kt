package com.timetracker.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

data class DaySummary(
    val date: LocalDate,
    val totalMs: Long,
    val entryCount: Int,
    val topActivityColors: List<Int>,
)

@Composable
fun CalendarView(
    yearMonth: Pair<Int, Int>, // year, month (1-12)
    daySummaries: Map<LocalDate, DaySummary>,
    onDayClick: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (year, month) = yearMonth
    val monthObj = Month(month)
    val firstDay = LocalDate(year, month, 1)
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30
    }
    val startDayOfWeek = firstDay.dayOfWeek.ordinal // Monday = 0

    Column(modifier = modifier) {
        // Month header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPreviousMonth) { Text("\u25C0") }
            Text(
                text = "${monthObj.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} $year",
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = onNextMonth) { Text("\u25B6") }
        }

        Spacer(Modifier.height(8.dp))

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Calendar grid
        var dayCounter = 1
        val totalCells = startDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    if (cellIndex >= startDayOfWeek && dayCounter <= daysInMonth) {
                        val date = LocalDate(year, month, dayCounter)
                        val summary = daySummaries[date]
                        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                        CalendarDayCell(
                            day = dayCounter,
                            isToday = date == today,
                            hasEntries = summary != null && summary.entryCount > 0,
                            topColors = summary?.topActivityColors ?: emptyList(),
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f),
                        )
                        dayCounter++
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    hasEntries: Boolean,
    topColors: List<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (isToday) {
                            Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$day",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                )
            }

            // Activity dots
            if (hasEntries && topColors.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    topColors.take(3).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(color)),
                        )
                    }
                }
            }
        }
    }
}
