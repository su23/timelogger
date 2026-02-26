package com.timetracker.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.TimeEntry
import com.timetracker.app.util.toLocalDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class HistoryUiState(
    val entriesByDate: Map<LocalDate, List<Pair<TimeEntry, Activity?>>> = emptyMap(),
    val activities: Map<Long, Activity> = emptyMap(),
    val isLoading: Boolean = true,
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
                val grouped = entries
                    .map { entry -> entry to activityMap[entry.activityId] }
                    .groupBy { (entry, _) -> entry.startTime.toLocalDate(tz) }
                    .toSortedMap(compareByDescending { it })
                HistoryUiState(
                    entriesByDate = grouped,
                    activities = activityMap,
                    isLoading = false,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            timeEntryRepo.delete(entryId)
        }
    }
}
