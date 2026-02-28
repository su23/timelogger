package com.timetracker.app.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.GoalRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.GoalProgress
import com.timetracker.app.domain.usecase.CheckGoalsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goalProgressList: List<GoalProgress> = emptyList(),
    val isLoading: Boolean = true,
)

class GoalsViewModel(
    private val goalRepo: GoalRepository,
    private val activityRepo: ActivityRepository,
    private val checkGoals: CheckGoalsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                goalRepo.observeAll(),
                activityRepo.observeAllIncludingArchived(),
            ) { _, activities ->
                activities
            }.collect { activities ->
                refreshGoals(activities)
            }
        }
    }

    private suspend fun refreshGoals(activities: List<Activity>) {
        val activityMap = activities.associateBy { it.id }
        val progressList = checkGoals.checkAllGoals(activityMap)
        _uiState.update {
            it.copy(goalProgressList = progressList, isLoading = false)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            goalRepo.delete(goalId)
        }
    }
}
