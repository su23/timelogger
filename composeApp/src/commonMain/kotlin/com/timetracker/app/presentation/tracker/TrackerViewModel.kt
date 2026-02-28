package com.timetracker.app.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActiveTimerRepository
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.SettingsRepository
import com.timetracker.app.domain.model.ActiveTimer
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.usecase.PauseTimerUseCase
import com.timetracker.app.domain.usecase.ResumeTimerUseCase
import com.timetracker.app.domain.usecase.StartTimerUseCase
import com.timetracker.app.domain.usecase.StopTimerUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class TrackerUiState(
    val activities: List<Activity> = emptyList(),
    val activeTimers: List<ActiveTimer> = emptyList(),
    val currentTimeMs: Long = Clock.System.now().toEpochMilliseconds(),
    val isLoading: Boolean = true,
    val allowSimultaneous: Boolean = true,
)

class TrackerViewModel(
    private val activityRepo: ActivityRepository,
    private val activeTimerRepo: ActiveTimerRepository,
    private val startTimer: StartTimerUseCase,
    private val stopTimer: StopTimerUseCase,
    private val pauseTimer: PauseTimerUseCase,
    private val resumeTimer: ResumeTimerUseCase,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            activityRepo.observeAll().collect { activities ->
                _uiState.update { it.copy(activities = activities, isLoading = false) }
            }
        }
        viewModelScope.launch {
            activeTimerRepo.observeAll().collect { timers ->
                _uiState.update { it.copy(activeTimers = timers) }
            }
        }
        // Tick every second for timer display
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(currentTimeMs = Clock.System.now().toEpochMilliseconds()) }
                delay(1000)
            }
        }
        viewModelScope.launch {
            seedDefaultActivities()
        }
        viewModelScope.launch {
            val allow = settingsRepo.getBoolean(SettingsRepository.Keys.ALLOW_SIMULTANEOUS, true)
            _uiState.update { it.copy(allowSimultaneous = allow) }
        }
    }

    fun onActivityClick(activityId: Long) {
        viewModelScope.launch {
            val existing = _uiState.value.activeTimers.find { it.activityId == activityId }
            if (existing != null) {
                stopTimer(activityId)
            } else {
                // If simultaneous not allowed, stop existing timers
                if (!_uiState.value.allowSimultaneous) {
                    _uiState.value.activeTimers.forEach {
                        stopTimer(it.activityId)
                    }
                }
                startTimer(activityId)
            }
        }
    }

    fun startWithCountdown(activityId: Long, targetDurationMs: Long) {
        viewModelScope.launch {
            // If simultaneous not allowed, stop existing timers
            if (!_uiState.value.allowSimultaneous) {
                _uiState.value.activeTimers.forEach {
                    stopTimer(it.activityId)
                }
            }
            val existing = activeTimerRepo.getByActivityId(activityId)
            if (existing != null) return@launch
            val now = Clock.System.now()
            activeTimerRepo.insert(
                ActiveTimer(
                    activityId = activityId,
                    startTime = now,
                    targetDurationMs = targetDurationMs,
                )
            )
        }
    }

    fun onPauseResume(activityId: Long) {
        viewModelScope.launch {
            val timer = _uiState.value.activeTimers.find { it.activityId == activityId } ?: return@launch
            if (timer.isPaused) {
                resumeTimer(activityId)
            } else {
                pauseTimer(activityId)
            }
        }
    }

    fun getElapsedMs(timer: ActiveTimer, currentTimeMs: Long): Long {
        return if (timer.isPaused) {
            timer.accumulatedMs
        } else {
            timer.accumulatedMs + (currentTimeMs - timer.startTime.toEpochMilliseconds())
        }
    }

    fun getCountdownRemainingMs(timer: ActiveTimer, currentTimeMs: Long): Long? {
        val target = timer.targetDurationMs ?: return null
        val elapsed = getElapsedMs(timer, currentTimeMs)
        return target - elapsed
    }

    private suspend fun seedDefaultActivities() {
        val existing = activityRepo.observeAll().first()
        if (existing.isNotEmpty()) return

        val defaults = listOf(
            Activity(name = "Sleep", icon = "\uD83D\uDCA4", color = 0xFF3F51B5.toInt()),
            Activity(name = "Work", icon = "\uD83D\uDCBC", color = 0xFF2196F3.toInt()),
            Activity(name = "Exercise", icon = "\uD83C\uDFCB\uFE0F", color = 0xFF4CAF50.toInt()),
            Activity(name = "Study", icon = "\uD83D\uDCDA", color = 0xFF9C27B0.toInt()),
            Activity(name = "Commute", icon = "\uD83D\uDE97", color = 0xFFFF9800.toInt()),
            Activity(name = "Cooking", icon = "\uD83C\uDF73", color = 0xFFE91E63.toInt()),
            Activity(name = "Reading", icon = "\uD83D\uDCD6", color = 0xFF00BCD4.toInt()),
            Activity(name = "Entertainment", icon = "\uD83C\uDFAC", color = 0xFFF44336.toInt()),
            Activity(name = "Social", icon = "\uD83D\uDC65", color = 0xFFFF5722.toInt()),
            Activity(name = "Chores", icon = "\uD83E\uDDF9", color = 0xFF795548.toInt()),
            Activity(name = "Personal Care", icon = "\uD83D\uDEC1", color = 0xFF009688.toInt()),
            Activity(name = "Other", icon = "\u2699\uFE0F", color = 0xFF607D8B.toInt()),
        )
        defaults.forEachIndexed { index, activity ->
            activityRepo.insert(activity.copy(sortOrder = index))
        }
    }
}
