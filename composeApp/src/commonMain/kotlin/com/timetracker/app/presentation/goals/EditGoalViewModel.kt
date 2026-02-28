package com.timetracker.app.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.GoalRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.Goal
import com.timetracker.app.domain.model.GoalPeriod
import com.timetracker.app.domain.model.GoalType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditGoalUiState(
    val activityId: Long? = null,
    val activities: List<Activity> = emptyList(),
    val goalType: GoalType = GoalType.Min,
    val period: GoalPeriod = GoalPeriod.Daily,
    val targetHours: Int = 1,
    val targetMinutes: Int = 0,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
)

class EditGoalViewModel(
    private val goalRepo: GoalRepository,
    private val activityRepo: ActivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditGoalUiState())
    val uiState: StateFlow<EditGoalUiState> = _uiState.asStateFlow()

    private var editingId: Long? = null

    init {
        viewModelScope.launch {
            activityRepo.observeAll().collect { activities ->
                _uiState.update { it.copy(activities = activities) }
            }
        }
    }

    fun loadGoal(id: Long?) {
        if (id == null) return
        editingId = id
        viewModelScope.launch {
            goalRepo.observeById(id).first()?.let { goal ->
                val totalMinutes = (goal.targetMs / 60000).toInt()
                _uiState.update {
                    it.copy(
                        activityId = goal.activityId,
                        goalType = goal.goalType,
                        period = goal.period,
                        targetHours = totalMinutes / 60,
                        targetMinutes = totalMinutes % 60,
                        isEditing = true,
                    )
                }
            }
        }
    }

    fun updateActivityId(id: Long) { _uiState.update { it.copy(activityId = id) } }
    fun updateGoalType(type: GoalType) { _uiState.update { it.copy(goalType = type) } }
    fun updatePeriod(period: GoalPeriod) { _uiState.update { it.copy(period = period) } }
    fun updateTargetHours(h: Int) { _uiState.update { it.copy(targetHours = h.coerceIn(0, 23)) } }
    fun updateTargetMinutes(m: Int) { _uiState.update { it.copy(targetMinutes = m.coerceIn(0, 59)) } }

    fun save() {
        val state = _uiState.value
        val activityId = state.activityId ?: return
        val targetMs = (state.targetHours * 3600000L) + (state.targetMinutes * 60000L)
        if (targetMs <= 0) return

        viewModelScope.launch {
            val goal = Goal(
                id = editingId ?: 0,
                activityId = activityId,
                goalType = state.goalType,
                period = state.period,
                targetMs = targetMs,
            )
            if (editingId != null) {
                goalRepo.update(goal)
            } else {
                goalRepo.insert(goal)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
