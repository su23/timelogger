package com.timetracker.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.TimeEntry
import com.timetracker.app.presentation.components.DaySummary
import com.timetracker.app.util.toLocalDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Clock

data class HistoryUiState(
    val entriesByDate: Map<LocalDate, List<Pair<TimeEntry, Activity?>>> = emptyMap(),
    val activities: Map<Long, Activity> = emptyMap(),
    val isLoading: Boolean = true,
    val showCalendar: Boolean = false,
    val calendarYear: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year,
    val calendarMonth: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month.number,
    val daySummaries: Map<LocalDate, DaySummary> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val recentlyDeleted: TimeEntry? = null,
)

class HistoryViewModel(
    private val timeEntryRepo: TimeEntryRepository,
    private val activityRepo: ActivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Load last 30 days of entries
            val tz = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            val thirtyDaysAgo = now.toLocalDate(tz).minus(30, DateTimeUnit.DAY).atStartOfDayIn(tz)

            combine(
                timeEntryRepo.observeByDateRange(thirtyDaysAgo, now),
                activityRepo.observeAllIncludingArchived(),
            ) { entries, activities ->
                val activityMap = activities.associateBy { it.id }
                val entryPairsList: List<Pair<TimeEntry, Activity?>> = entries.map { e: TimeEntry ->
                    Pair(e, activityMap[e.activityId])
                }
                val groupedMap: Map<LocalDate, List<Pair<TimeEntry, Activity?>>> = entryPairsList
                    .groupBy { pair: Pair<TimeEntry, Activity?> -> pair.first.startTime.toLocalDate(tz) }
                val sortedKeys = groupedMap.keys.sortedDescending()
                val grouped: Map<LocalDate, List<Pair<TimeEntry, Activity?>>> = linkedMapOf<LocalDate, List<Pair<TimeEntry, Activity?>>>().also { map ->
                    for (key in sortedKeys) {
                        map[key] = groupedMap[key] ?: emptyList()
                    }
                }

                // Build day summaries for calendar
                val summaries = mutableMapOf<LocalDate, DaySummary>()
                for (mapEntry: Map.Entry<LocalDate, List<Pair<TimeEntry, Activity?>>> in grouped) {
                    val date: LocalDate = mapEntry.key
                    val pairs: List<Pair<TimeEntry, Activity?>> = mapEntry.value
                    var totalMs = 0L
                    val activityColorMap = mutableMapOf<Long, Int>() // activityId -> color
                    val activityDurationMap = mutableMapOf<Long, Long>() // activityId -> totalDuration
                    for (i in pairs.indices) {
                        val timeEntry: TimeEntry = pairs[i].first
                        val activity: Activity? = pairs[i].second
                        totalMs += timeEntry.durationMs
                        if (activity != null) {
                            activityColorMap[activity.id] = activity.color
                            activityDurationMap[activity.id] = (activityDurationMap[activity.id] ?: 0L) + timeEntry.durationMs
                        }
                    }
                    val topColors: List<Int> = activityDurationMap.entries
                        .sortedByDescending { e -> e.value }
                        .mapNotNull { e -> activityColorMap[e.key] }
                    summaries[date] = DaySummary(
                        date = date,
                        totalMs = totalMs,
                        entryCount = pairs.size,
                        topActivityColors = topColors,
                    )
                }

                Triple(grouped, activityMap, summaries)
            }.collect { (grouped, activityMap, summaries) ->
                // Copy onto the current state rather than rebuilding it. A fresh
                // HistoryUiState(...) silently dropped every field not listed
                // here, so anything set between emissions was lost.
                _uiState.update {
                    it.copy(
                        entriesByDate = grouped,
                        activities = activityMap,
                        daySummaries = summaries,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun toggleCalendar() {
        _uiState.update { it.copy(showCalendar = !it.showCalendar) }
    }

    fun previousMonth() {
        _uiState.update {
            val newMonth = if (it.calendarMonth == 1) 12 else it.calendarMonth - 1
            val newYear = if (it.calendarMonth == 1) it.calendarYear - 1 else it.calendarYear
            it.copy(calendarYear = newYear, calendarMonth = newMonth)
        }
    }

    fun nextMonth() {
        _uiState.update {
            val newMonth = if (it.calendarMonth == 12) 1 else it.calendarMonth + 1
            val newYear = if (it.calendarMonth == 12) it.calendarYear + 1 else it.calendarYear
            it.copy(calendarYear = newYear, calendarMonth = newMonth)
        }
    }

    fun selectDate(date: LocalDate) {
        // Stay on the calendar and show the day underneath it. Closing the
        // calendar here meant the day detail below the grid never rendered.
        _uiState.update {
            it.copy(selectedDate = if (it.selectedDate == date) null else date)
        }
    }

    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            // Keep the row so the snackbar can offer it back. A delete is one
            // tap on a row of identical buttons, and it is otherwise final.
            val deleted = timeEntryRepo.observeById(entryId).first()
            timeEntryRepo.delete(entryId)
            _uiState.update { it.copy(recentlyDeleted = deleted) }
        }
    }

    fun undoDelete() {
        val entry = _uiState.value.recentlyDeleted ?: return
        viewModelScope.launch {
            timeEntryRepo.insert(entry)
            _uiState.update { it.copy(recentlyDeleted = null) }
        }
    }

    fun clearRecentlyDeleted() {
        _uiState.update { it.copy(recentlyDeleted = null) }
    }
}
