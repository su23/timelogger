package com.timetracker.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.savedstate.read
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.timetracker.app.presentation.tracker.TrackerScreen
import com.timetracker.app.presentation.history.HistoryScreen
import com.timetracker.app.presentation.stats.StatsScreen
import com.timetracker.app.presentation.activities.ActivitiesScreen
import com.timetracker.app.presentation.activities.EditActivityScreen
import com.timetracker.app.presentation.groups.GroupsScreen
import com.timetracker.app.presentation.entry.EditEntryScreen
import com.timetracker.app.presentation.export.ExportScreen
import com.timetracker.app.presentation.settings.SettingsScreen
import com.timetracker.app.presentation.goals.GoalsScreen
import com.timetracker.app.presentation.goals.EditGoalScreen
import com.timetracker.app.presentation.pomodoro.PomodoroScreen

object Routes {
    const val TRACKER = "tracker"
    const val HISTORY = "history"
    const val STATS = "stats"
    const val ACTIVITIES = "activities"
    const val EDIT_ACTIVITY = "edit_activity/{activityId}"
    const val NEW_ACTIVITY = "edit_activity/new"
    const val GROUPS = "groups"
    const val EDIT_ENTRY = "edit_entry/{entryId}"
    const val NEW_ENTRY = "edit_entry/new"
    const val EXPORT = "export"
    const val SETTINGS = "settings"
    const val GOALS = "goals"
    const val EDIT_GOAL = "edit_goal/{goalId}"
    const val NEW_GOAL = "edit_goal/new"
    const val POMODORO = "pomodoro"

    fun editActivity(id: Long) = "edit_activity/$id"
    fun editEntry(id: Long) = "edit_entry/$id"
    fun editGoal(id: Long) = "edit_goal/$id"
}

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    Tracker(Routes.TRACKER, "Tracker", Icons.Filled.Timer),
    History(Routes.HISTORY, "History", Icons.AutoMirrored.Filled.List),
    Stats(Routes.STATS, "Stats", Icons.Filled.BarChart),
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val bottomTabs = BottomTab.entries
    val isBottomBarVisible = bottomTabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(Routes.TRACKER) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            label = { Text(tab.label) },
                            icon = {
                                // The adjacent label already names the tab, so the icon
                                // stays decorative rather than being announced twice.
                                Icon(tab.icon, contentDescription = null)
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TRACKER,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.TRACKER) {
                TrackerScreen(
                    onManageActivities = { navController.navigate(Routes.ACTIVITIES) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onPomodoro = { navController.navigate(Routes.POMODORO) },
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    onEditEntry = { navController.navigate(Routes.editEntry(it)) },
                    onNewEntry = { navController.navigate(Routes.NEW_ENTRY) },
                )
            }
            composable(Routes.STATS) {
                StatsScreen(
                    onExport = { navController.navigate(Routes.EXPORT) },
                    onGoals = { navController.navigate(Routes.GOALS) },
                )
            }
            composable(Routes.ACTIVITIES) {
                ActivitiesScreen(
                    onEditActivity = { navController.navigate(Routes.editActivity(it)) },
                    onNewActivity = { navController.navigate(Routes.NEW_ACTIVITY) },
                    onManageGroups = { navController.navigate(Routes.GROUPS) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.EDIT_ACTIVITY,
                arguments = listOf(navArgument("activityId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val activityIdStr = backStackEntry.arguments?.read { getStringOrNull("activityId") }
                val activityId = activityIdStr?.toLongOrNull()
                EditActivityScreen(
                    activityId = activityId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.GROUPS) {
                GroupsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                Routes.EDIT_ENTRY,
                arguments = listOf(navArgument("entryId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val entryIdStr = backStackEntry.arguments?.read { getStringOrNull("entryId") }
                val entryId = entryIdStr?.toLongOrNull()
                EditEntryScreen(
                    entryId = entryId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.EXPORT) {
                ExportScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.GOALS) {
                GoalsScreen(
                    onNewGoal = { navController.navigate(Routes.NEW_GOAL) },
                    onEditGoal = { navController.navigate(Routes.editGoal(it)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.EDIT_GOAL,
                arguments = listOf(navArgument("goalId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val goalIdStr = backStackEntry.arguments?.read { getStringOrNull("goalId") }
                val goalId = goalIdStr?.toLongOrNull()
                EditGoalScreen(
                    goalId = goalId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.POMODORO) {
                PomodoroScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
