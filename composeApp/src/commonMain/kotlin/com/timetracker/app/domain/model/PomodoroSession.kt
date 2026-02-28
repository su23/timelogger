package com.timetracker.app.domain.model

enum class PomodoroPhase {
    Work,
    Break,
    LongBreak,
    Idle,
}

data class PomodoroConfig(
    val workDurationMs: Long = 25 * 60 * 1000L,
    val breakDurationMs: Long = 5 * 60 * 1000L,
    val longBreakDurationMs: Long = 15 * 60 * 1000L,
    val sessionsBeforeLongBreak: Int = 4,
)
