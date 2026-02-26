package com.timetracker.app.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityGroupRepository
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.domain.model.Activity
import com.timetracker.app.domain.model.ActivityGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditActivityUiState(
    val name: String = "",
    val icon: String = "",
    val color: Int = 0xFF2196F3.toInt(),
    val groupId: Long? = null,
    val groups: List<ActivityGroup> = emptyList(),
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
)

class EditActivityViewModel(
    private val activityRepo: ActivityRepository,
    private val groupRepo: ActivityGroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditActivityUiState())
    val uiState: StateFlow<EditActivityUiState> = _uiState.asStateFlow()

    private var editingId: Long? = null

    init {
        viewModelScope.launch {
            groupRepo.observeAll().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
    }

    fun loadActivity(id: Long?) {
        if (id == null) return
        editingId = id
        viewModelScope.launch {
            activityRepo.observeById(id).first()?.let { activity ->
                _uiState.update {
                    it.copy(
                        name = activity.name,
                        icon = activity.icon,
                        color = activity.color,
                        groupId = activity.groupId,
                        isEditing = true,
                    )
                }
            }
        }
    }

    fun updateName(name: String) { _uiState.update { it.copy(name = name) } }
    fun updateIcon(icon: String) { _uiState.update { it.copy(icon = icon) } }
    fun updateColor(color: Int) { _uiState.update { it.copy(color = color) } }
    fun updateGroupId(groupId: Long?) { _uiState.update { it.copy(groupId = groupId) } }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return
        viewModelScope.launch {
            val activity = Activity(
                id = editingId ?: 0,
                name = state.name.trim(),
                icon = state.icon.trim(),
                color = state.color,
                groupId = state.groupId,
            )
            if (editingId != null) {
                activityRepo.update(activity)
            } else {
                activityRepo.insert(activity)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
