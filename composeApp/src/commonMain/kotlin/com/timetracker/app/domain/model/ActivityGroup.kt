package com.timetracker.app.domain.model

data class ActivityGroup(
    val id: Long = 0,
    val name: String,
    val color: Int = 0xFF607D8B.toInt(),
    val sortOrder: Int = 0,
)
