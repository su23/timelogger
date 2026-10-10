package com.timetracker.app.data.export

import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.TimeEntry
import com.timetracker.app.util.formatDuration
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HtmlExporter {
    fun export(
        entries: List<TimeEntry>,
        activitiesById: Map<Long, Activity>,
    ): String = buildString {
        appendLine("<!DOCTYPE html><html><head><meta charset='utf-8'>")
        appendLine("<title>Time Tracking Report</title>")
        appendLine("<style>")
        appendLine("body{font-family:system-ui;max-width:800px;margin:0 auto;padding:20px}")
        appendLine("table{width:100%;border-collapse:collapse}")
        appendLine("th,td{border:1px solid #ddd;padding:8px;text-align:left}")
        appendLine("th{background:#f5f5f5}")
        appendLine(".color-dot{width:12px;height:12px;border-radius:50%;display:inline-block;margin-right:6px}")
        appendLine("</style></head><body>")
        appendLine("<h1>Time Tracking Report</h1>")
        appendLine("<table><thead><tr>")
        appendLine("<th>Activity</th><th>Start</th><th>End</th><th>Duration</th><th>Comment</th>")
        appendLine("</tr></thead><tbody>")
        entries.forEach { entry ->
            val activity = activitiesById[entry.activityId]
            val tz = TimeZone.currentSystemDefault()
            val start = entry.startTime.toLocalDateTime(tz)
            val end = entry.endTime.toLocalDateTime(tz)
            val colorHex = activity?.color?.let { colorInt ->
                val hex = (colorInt and 0xFFFFFF).toString(16).padStart(6, '0')
                "#$hex"
            } ?: "#999"
            val name = escape(activity?.name ?: "Unknown")
            appendLine("<tr>")
            appendLine("<td><span class='color-dot' style='background:$colorHex'></span>$name</td>")
            appendLine("<td>$start</td><td>$end</td>")
            appendLine("<td>${formatDuration(entry.durationMs)}</td>")
            appendLine("<td>${escape(entry.comment)}</td>")
            appendLine("</tr>")
        }
        appendLine("</tbody></table></body></html>")
    }

    /**
     * Escapes the characters that would otherwise end the surrounding cell, so an
     * activity named "Research <client>" or a comment containing & renders as typed.
     */
    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { c ->
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(c)
            }
        }
    }
}
