package com.timetracker.app.util

private fun padTwo(n: Long): String = if (n < 10) "0$n" else "$n"

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "$hours:${padTwo(minutes)}:${padTwo(seconds)}"
    } else {
        "$minutes:${padTwo(seconds)}"
    }
}

fun formatDurationLong(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours == 0L) append("${minutes}m")
    }.trim()
}

fun padTwoInt(n: Int): String = if (n < 10) "0$n" else "$n"
