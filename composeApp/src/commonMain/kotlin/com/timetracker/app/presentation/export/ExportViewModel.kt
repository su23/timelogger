package com.timetracker.app.presentation.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.export.CsvExporter
import com.timetracker.app.data.export.HtmlExporter
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.presentation.components.DateRangeOption
import com.timetracker.app.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Instant

enum class ExportFormat(val label: String, val extension: String) {
    CSV("CSV", "csv"),
    HTML("HTML", "html"),
}

data class ExportUiState(
    val rangeOption: DateRangeOption = DateRangeOption.Month,
    val format: ExportFormat = ExportFormat.CSV,
    val exportedContent: String? = null,
    val isExporting: Boolean = false,
)

class ExportViewModel(
    private val timeEntryRepo: TimeEntryRepository,
    private val activityRepo: ActivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun onRangeChanged(option: DateRangeOption) {
        _uiState.update { it.copy(rangeOption = option) }
    }

    fun onFormatChanged(format: ExportFormat) {
        _uiState.update { it.copy(format = format) }
    }

    fun export() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val (start, end) = getRange(_uiState.value.rangeOption)
            val entries = timeEntryRepo.observeByDateRange(start, end).first()
            val activities = activityRepo.observeAllIncludingArchived().first()
            val activitiesById = activities.associateBy { it.id }

            val content = when (_uiState.value.format) {
                ExportFormat.CSV -> CsvExporter().export(entries, activitiesById)
                ExportFormat.HTML -> HtmlExporter().export(entries, activitiesById)
            }

            _uiState.update { it.copy(exportedContent = content, isExporting = false) }
        }
    }

    fun clearExport() {
        _uiState.update { it.copy(exportedContent = null) }
    }

    private fun getRange(option: DateRangeOption): Pair<Instant, Instant> = when (option) {
        DateRangeOption.Day -> todayRange()
        DateRangeOption.Week -> thisWeekRange()
        DateRangeOption.Month -> thisMonthRange()
    }
}
