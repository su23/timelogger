package com.timetracker.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.repository.SettingsRepository
import com.timetracker.app.data.repository.SettingsRepository.Keys
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ThemeOption(val label: String, val value: String) {
    System("System", "system"),
    Light("Light", "light"),
    Dark("Dark", "dark"),
}

data class SettingsUiState(
    val theme: ThemeOption = ThemeOption.System,
    val allowSimultaneous: Boolean = true,
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val pomodoroLongBreakMinutes: Int = 15,
    val pomodoroSessionsBeforeLong: Int = 4,
    val isLoading: Boolean = true,
)

class SettingsViewModel(
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val theme = settingsRepo.getString(Keys.THEME, "system")
            val themeOption = ThemeOption.entries.find { it.value == theme } ?: ThemeOption.System
            val allowSimultaneous = settingsRepo.getBoolean(Keys.ALLOW_SIMULTANEOUS, true)
            val workMin = settingsRepo.getInt(Keys.POMODORO_WORK_MINUTES, 25)
            val breakMin = settingsRepo.getInt(Keys.POMODORO_BREAK_MINUTES, 5)
            val longBreakMin = settingsRepo.getInt(Keys.POMODORO_LONG_BREAK_MINUTES, 15)
            val sessions = settingsRepo.getInt(Keys.POMODORO_SESSIONS_BEFORE_LONG, 4)

            _uiState.update {
                it.copy(
                    theme = themeOption,
                    allowSimultaneous = allowSimultaneous,
                    pomodoroWorkMinutes = workMin,
                    pomodoroBreakMinutes = breakMin,
                    pomodoroLongBreakMinutes = longBreakMin,
                    pomodoroSessionsBeforeLong = sessions,
                    isLoading = false,
                )
            }
        }
    }

    fun setTheme(option: ThemeOption) {
        _uiState.update { it.copy(theme = option) }
        viewModelScope.launch { settingsRepo.setString(Keys.THEME, option.value) }
    }

    fun setAllowSimultaneous(allow: Boolean) {
        _uiState.update { it.copy(allowSimultaneous = allow) }
        viewModelScope.launch { settingsRepo.setBoolean(Keys.ALLOW_SIMULTANEOUS, allow) }
    }

    fun setPomodoroWorkMinutes(minutes: Int) {
        _uiState.update { it.copy(pomodoroWorkMinutes = minutes.coerceIn(1, 120)) }
        viewModelScope.launch { settingsRepo.setInt(Keys.POMODORO_WORK_MINUTES, minutes.coerceIn(1, 120)) }
    }

    fun setPomodoroBreakMinutes(minutes: Int) {
        _uiState.update { it.copy(pomodoroBreakMinutes = minutes.coerceIn(1, 60)) }
        viewModelScope.launch { settingsRepo.setInt(Keys.POMODORO_BREAK_MINUTES, minutes.coerceIn(1, 60)) }
    }

    fun setPomodoroLongBreakMinutes(minutes: Int) {
        _uiState.update { it.copy(pomodoroLongBreakMinutes = minutes.coerceIn(1, 60)) }
        viewModelScope.launch { settingsRepo.setInt(Keys.POMODORO_LONG_BREAK_MINUTES, minutes.coerceIn(1, 60)) }
    }

    fun setPomodoroSessionsBeforeLong(sessions: Int) {
        _uiState.update { it.copy(pomodoroSessionsBeforeLong = sessions.coerceIn(1, 10)) }
        viewModelScope.launch { settingsRepo.setInt(Keys.POMODORO_SESSIONS_BEFORE_LONG, sessions.coerceIn(1, 10)) }
    }
}
