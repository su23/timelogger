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
            val name = activity?.name?.replace(",", ";") ?: "Unknown"
            val comment = entry.comment.replace(",", ";").replace("\n", " ")
            appendLine("$name,$start,$end,${formatDuration(entry.durationMs)},$comment")
        }
    }
}
