package com.timetracker.app.data.export

import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.TimeEntry
import com.timetracker.app.util.formatDuration
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CsvExporter {
    fun export(
        entries: List<TimeEntry>,
        activitiesById: Map<Long, Activity>,
    ): String = buildString {
        appendLine("Activity,Start,End,Duration,Comment")
        entries.forEach { entry ->
            val activity = activitiesById[entry.activityId]
            val tz = TimeZone.currentSystemDefault()
            val start = entry.startTime.toLocalDateTime(tz)
            val end = entry.endTime.toLocalDateTime(tz)
            appendLine(
                listOf(
                    activity?.name ?: "Unknown",
                    start.toString(),
                    end.toString(),
                    formatDuration(entry.durationMs),
                    entry.comment,
                ).joinToString(",") { field(it) }
            )
        }
    }

    /**
     * Quotes a field when it contains a separator, a quote or a line break, so that
     * names and comments survive the round trip unchanged. Embedded quotes are doubled,
     * as RFC 4180 requires.
     */
    private fun field(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
}
