package com.timetracker.app

import androidx.compose.runtime.*
import com.timetracker.app.data.repository.SettingsRepository
import com.timetracker.app.presentation.theme.AppTheme
import com.timetracker.app.presentation.navigation.AppNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        val settingsRepo = koinInject<SettingsRepository>()
        var themeOverride by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            val theme = withContext(Dispatchers.IO) {
                settingsRepo.getString(SettingsRepository.Keys.THEME, "system")
            }
            themeOverride = if (theme == "system") null else theme
        }

        // Listen for changes
        LaunchedEffect(Unit) {
            settingsRepo.observeAll().collect { settings ->
                val theme = settings[SettingsRepository.Keys.THEME] ?: "system"
                themeOverride = if (theme == "system") null else theme
            }
        }

        AppTheme(themeOverride = themeOverride) {
            AppNavigation()
        }
    }
}
