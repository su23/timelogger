package com.timetracker.app

import androidx.compose.runtime.Composable
import com.timetracker.app.presentation.theme.AppTheme
import com.timetracker.app.presentation.navigation.AppNavigation
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        AppTheme {
            AppNavigation()
        }
    }
}
