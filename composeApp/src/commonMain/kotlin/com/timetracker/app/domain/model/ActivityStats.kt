package com.timetracker.app.domain.model

data class ActivityStats(
    val activity: Activity,
    val totalDurationMs: Long,
    val entryCount: Long,
    val percentage: Float = 0f,
)

data class DailyTotal(
    val activityId: Long,
    val dayEpoch: Long,
    val totalDurationMs: Long,
)
