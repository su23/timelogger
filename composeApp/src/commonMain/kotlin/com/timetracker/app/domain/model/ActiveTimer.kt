package com.timetracker.app.domain.model

import kotlin.time.Instant

data class ActiveTimer(
    val id: Long = 0,
    val activityId: Long,
    val startTime: Instant,
    val accumulatedMs: Long = 0,
    val isPaused: Boolean = false,
    val pausedAt: Instant? = null,
    val targetDurationMs: Long? = null,
)
