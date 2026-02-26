package com.timetracker.app.presentation.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.ActivityGroupRepository
import com.timetracker.app.domain.model.ActivityGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groups: List<ActivityGroup> = emptyList(),
    val editingGroup: ActivityGroup? = null,
    val newGroupName: String = "",
    val newGroupColor: Int = 0xFF607D8B.toInt(),
    val isLoading: Boolean = true,
)

class GroupsViewModel(
    private val groupRepo: ActivityGroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            groupRepo.observeAll().collect { groups ->
                _uiState.update { it.copy(groups = groups, isLoading = false) }
            }
        }
    }

    fun updateNewGroupName(name: String) { _uiState.update { it.copy(newGroupName = name) } }
    fun updateNewGroupColor(color: Int) { _uiState.update { it.copy(newGroupColor = color) } }

    fun addGroup() {
        val state = _uiState.value
        if (state.newGroupName.isBlank()) return
        viewModelScope.launch {
            groupRepo.insert(ActivityGroup(name = state.newGroupName.trim(), color = state.newGroupColor))
            _uiState.update { it.copy(newGroupName = "", newGroupColor = 0xFF607D8B.toInt()) }
        }
    }

    fun deleteGroup(id: Long) {
        viewModelScope.launch { groupRepo.delete(id) }
    }

    fun startEditing(group: ActivityGroup) {
        _uiState.update { it.copy(editingGroup = group) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editingGroup = null) }
    }

    fun saveEditing(name: String, color: Int) {
        val group = _uiState.value.editingGroup ?: return
        viewModelScope.launch {
            groupRepo.update(group.copy(name = name, color = color))
            _uiState.update { it.copy(editingGroup = null) }
        }
    }
}
