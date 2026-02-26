package com.timetracker.app.domain.model

import kotlinx.datetime.Instant

data class TimeEntry(
    val id: Long = 0,
    val activityId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val durationMs: Long,
    val comment: String = "",
)
