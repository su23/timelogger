package com.timetracker.app.domain.model

enum class GoalType(val value: String, val label: String) {
    Min("min", "Minimum"),
    Max("max", "Maximum");

    companion object {
        fun fromValue(value: String) = entries.find { it.value == value } ?: Min
    }
}

enum class GoalPeriod(val value: String, val label: String) {
    Daily("daily", "Daily"),
    Weekly("weekly", "Weekly"),
    Monthly("monthly", "Monthly");

    companion object {
        fun fromValue(value: String) = entries.find { it.value == value } ?: Daily
    }
}

data class Goal(
    val id: Long = 0,
    val activityId: Long,
    val goalType: GoalType = GoalType.Min,
    val period: GoalPeriod = GoalPeriod.Daily,
    val targetMs: Long,
    val isActive: Boolean = true,
)

data class GoalProgress(
    val goal: Goal,
    val activity: Activity,
    val currentMs: Long,
    val progressPercent: Float,
    val isReached: Boolean,
)
