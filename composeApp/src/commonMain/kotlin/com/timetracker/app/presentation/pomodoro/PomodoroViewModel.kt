package com.timetracker.app.presentation.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.SettingsRepository
import com.timetracker.app.data.repository.SettingsRepository.Keys
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.PomodoroConfig
import com.timetracker.app.domain.model.PomodoroPhase
import com.timetracker.app.domain.model.TimeEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class PomodoroUiState(
    val activities: List<Activity> = emptyList(),
    val selectedActivityId: Long? = null,
    val phase: PomodoroPhase = PomodoroPhase.Idle,
    val remainingMs: Long = 0,
    val totalPhaseMs: Long = 0,
    val completedSessions: Int = 0,
    val config: PomodoroConfig = PomodoroConfig(),
    val isRunning: Boolean = false,
)

class PomodoroViewModel(
    private val activityRepo: ActivityRepository,
    private val timeEntryRepo: TimeEntryRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var phaseStartTime: Instant? = null

    init {
        viewModelScope.launch {
            activityRepo.observeAll().collect { activities ->
                _uiState.update { it.copy(activities = activities) }
            }
        }
        viewModelScope.launch {
            loadConfig()
        }
    }

    private suspend fun loadConfig() {
        val workMin = settingsRepo.getInt(Keys.POMODORO_WORK_MINUTES, 25)
        val breakMin = settingsRepo.getInt(Keys.POMODORO_BREAK_MINUTES, 5)
        val longBreakMin = settingsRepo.getInt(Keys.POMODORO_LONG_BREAK_MINUTES, 15)
        val sessions = settingsRepo.getInt(Keys.POMODORO_SESSIONS_BEFORE_LONG, 4)

        val config = PomodoroConfig(
            workDurationMs = workMin * 60 * 1000L,
            breakDurationMs = breakMin * 60 * 1000L,
            longBreakDurationMs = longBreakMin * 60 * 1000L,
            sessionsBeforeLongBreak = sessions,
        )
        _uiState.update {
            it.copy(
                config = config,
                remainingMs = config.workDurationMs,
                totalPhaseMs = config.workDurationMs,
            )
        }
    }

    fun selectActivity(activityId: Long) {
        _uiState.update { it.copy(selectedActivityId = activityId) }
    }

    fun start() {
        if (_uiState.value.selectedActivityId == null) return
        phaseStartTime = Clock.System.now()
        _uiState.update {
            it.copy(
                phase = PomodoroPhase.Work,
                remainingMs = it.config.workDurationMs,
                totalPhaseMs = it.config.workDurationMs,
                isRunning = true,
            )
        }
        startCountdown()
    }

    fun pause() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resume() {
        _uiState.update { it.copy(isRunning = true) }
        startCountdown()
    }

    fun stop() {
        timerJob?.cancel()
        // Record work time if in work phase
        if (_uiState.value.phase == PomodoroPhase.Work) {
            recordWorkSession()
        }
        _uiState.update {
            it.copy(
                phase = PomodoroPhase.Idle,
                remainingMs = it.config.workDurationMs,
                totalPhaseMs = it.config.workDurationMs,
                completedSessions = 0,
                isRunning = false,
            )
        }
        phaseStartTime = null
    }

    fun skipPhase() {
        onPhaseComplete()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingMs > 0 && _uiState.value.isRunning) {
                delay(1000)
                _uiState.update { it.copy(remainingMs = (it.remainingMs - 1000).coerceAtLeast(0)) }
                if (_uiState.value.remainingMs <= 0) {
                    onPhaseComplete()
                }
            }
        }
    }

    private fun onPhaseComplete() {
        timerJob?.cancel()
        val state = _uiState.value
        val config = state.config

        when (state.phase) {
            PomodoroPhase.Work -> {
                recordWorkSession()
                val newCount = state.completedSessions + 1
                val isLongBreak = newCount % config.sessionsBeforeLongBreak == 0
                val nextPhase = if (isLongBreak) PomodoroPhase.LongBreak else PomodoroPhase.Break
                val nextMs = if (isLongBreak) config.longBreakDurationMs else config.breakDurationMs

                phaseStartTime = Clock.System.now()
                _uiState.update {
                    it.copy(
                        phase = nextPhase,
                        remainingMs = nextMs,
                        totalPhaseMs = nextMs,
                        completedSessions = newCount,
                        isRunning = true,
                    )
                }
                startCountdown()
            }
            PomodoroPhase.Break, PomodoroPhase.LongBreak -> {
                phaseStartTime = Clock.System.now()
                _uiState.update {
                    it.copy(
                        phase = PomodoroPhase.Work,
                        remainingMs = config.workDurationMs,
                        totalPhaseMs = config.workDurationMs,
                        isRunning = true,
                    )
                }
                startCountdown()
            }
            PomodoroPhase.Idle -> {}
        }
    }

    private fun recordWorkSession() {
        val activityId = _uiState.value.selectedActivityId ?: return
        val startTime = phaseStartTime ?: return
        val now = Clock.System.now()
        val durationMs = now.toEpochMilliseconds() - startTime.toEpochMilliseconds()

        viewModelScope.launch {
            timeEntryRepo.insert(
                TimeEntry(
                    activityId = activityId,
                    startTime = startTime,
                    endTime = now,
                    durationMs = durationMs,
                    comment = "Pomodoro session",
                )
            )
        }
    }
}
