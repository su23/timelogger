package com.timetracker.app.presentation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.TimeEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class EditEntryUiState(
    val activityId: Long? = null,
    val activities: List<Activity> = emptyList(),
    val startDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val startHour: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour,
    val startMinute: Int = 0,
    val endDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val endHour: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour,
    val endMinute: Int = 0,
    val comment: String = "",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
)

class EditEntryViewModel(
    private val timeEntryRepo: TimeEntryRepository,
    private val activityRepo: ActivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditEntryUiState())
    val uiState: StateFlow<EditEntryUiState> = _uiState.asStateFlow()

    private var editingId: Long? = null

    init {
        viewModelScope.launch {
            activityRepo.observeAll().collect { activities ->
                _uiState.update { it.copy(activities = activities) }
            }
        }
    }

    fun loadEntry(id: Long?) {
        if (id == null) return
        editingId = id
        viewModelScope.launch {
            timeEntryRepo.observeById(id).first()?.let { entry ->
                val tz = TimeZone.currentSystemDefault()
                val startLocal = entry.startTime.toLocalDateTime(tz)
                val endLocal = entry.endTime.toLocalDateTime(tz)
                _uiState.update {
                    it.copy(
                        activityId = entry.activityId,
                        startDate = startLocal.date,
                        startHour = startLocal.hour,
                        startMinute = startLocal.minute,
                        endDate = endLocal.date,
                        endHour = endLocal.hour,
                        endMinute = endLocal.minute,
                        comment = entry.comment,
                        isEditing = true,
                    )
                }
            }
        }
    }

    fun updateActivityId(id: Long) { _uiState.update { it.copy(activityId = id) } }
    fun updateStartHour(h: Int) { _uiState.update { it.copy(startHour = h) } }
    fun updateStartMinute(m: Int) { _uiState.update { it.copy(startMinute = m) } }
    fun updateEndHour(h: Int) { _uiState.update { it.copy(endHour = h) } }
    fun updateEndMinute(m: Int) { _uiState.update { it.copy(endMinute = m) } }
    fun updateComment(c: String) { _uiState.update { it.copy(comment = c) } }

    fun save() {
        val state = _uiState.value
        val activityId = state.activityId ?: return
        val tz = TimeZone.currentSystemDefault()

        val startInstant = LocalDateTime(state.startDate, LocalTime(state.startHour, state.startMinute))
            .toInstant(tz)
        val endInstant = LocalDateTime(state.endDate, LocalTime(state.endHour, state.endMinute))
            .toInstant(tz)

        if (endInstant <= startInstant) return

        val durationMs = endInstant.toEpochMilliseconds() - startInstant.toEpochMilliseconds()

        viewModelScope.launch {
            val entry = TimeEntry(
                id = editingId ?: 0,
                activityId = activityId,
                startTime = startInstant,
                endTime = endInstant,
                durationMs = durationMs,
                comment = state.comment,
            )
            if (editingId != null) {
                timeEntryRepo.update(entry)
            } else {
                timeEntryRepo.insert(entry)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
