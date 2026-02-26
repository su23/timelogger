package com.timetracker.app.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityGroupRepository
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.ActivityGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActivitiesUiState(
    val activities: List<Activity> = emptyList(),
    val groups: List<ActivityGroup> = emptyList(),
    val isLoading: Boolean = true,
)

class ActivitiesViewModel(
    private val activityRepo: ActivityRepository,
    private val groupRepo: ActivityGroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                activityRepo.observeAllIncludingArchived(),
                groupRepo.observeAll(),
            ) { activities, groups ->
                ActivitiesUiState(activities = activities, groups = groups, isLoading = false)
            }.collect { _uiState.value = it }
        }
    }

    fun archiveActivity(id: Long) {
        viewModelScope.launch { activityRepo.archive(id) }
    }

    fun unarchiveActivity(id: Long) {
        viewModelScope.launch { activityRepo.unarchive(id) }
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch { activityRepo.delete(id) }
    }
}
