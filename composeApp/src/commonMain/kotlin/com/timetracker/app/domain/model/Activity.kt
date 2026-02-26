package com.timetracker.app.domain.model

data class Activity(
    val id: Long = 0,
    val name: String,
    val icon: String = "",
    val color: Int = 0xFF2196F3.toInt(),
    val groupId: Long? = null,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
)
