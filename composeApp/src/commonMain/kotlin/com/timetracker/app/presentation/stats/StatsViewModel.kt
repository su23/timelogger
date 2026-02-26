package com.timetracker.app.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.ActivityStats
import com.timetracker.app.domain.model.DailyTotal
import com.timetracker.app.domain.usecase.GetStatsUseCase
import com.timetracker.app.presentation.components.DateRangeOption
import com.timetracker.app.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

data class StatsUiState(
    val rangeOption: DateRangeOption = DateRangeOption.Week,
    val activityStats: List<ActivityStats> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val totalMs: Long = 0,
    val isLoading: Boolean = true,
)

class StatsViewModel(
    private val getStats: GetStatsUseCase,
    private val activityRepo: ActivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun onRangeChanged(option: DateRangeOption) {
        _uiState.update { it.copy(rangeOption = option) }
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val (start, end) = getRange(_uiState.value.rangeOption)
            val stats = getStats.getActivityStats(start, end)
            val dailyTotals = getStats.getDailyTotals(start, end)
            val totalMs = stats.sumOf { it.totalDurationMs }
            _uiState.update {
                it.copy(
                    activityStats = stats,
                    dailyTotals = dailyTotals,
                    totalMs = totalMs,
                    isLoading = false,
                )
            }
        }
    }

    private fun getRange(option: DateRangeOption): Pair<Instant, Instant> = when (option) {
        DateRangeOption.Day -> todayRange()
        DateRangeOption.Week -> thisWeekRange()
        DateRangeOption.Month -> thisMonthRange()
    }
}
