package com.timetracker.app.di

import com.timetracker.app.data.db.AppDatabase
import com.timetracker.app.data.db.DriverFactory
import com.timetracker.app.data.repository.ActiveTimerRepository
import com.timetracker.app.data.repository.ActivityGroupRepository
import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.CustomFieldRepository
import com.timetracker.app.data.repository.GoalRepository
import com.timetracker.app.data.repository.SettingsRepository
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.usecase.*
import com.timetracker.app.presentation.tracker.TrackerViewModel
import com.timetracker.app.presentation.history.HistoryViewModel
import com.timetracker.app.presentation.stats.StatsViewModel
import com.timetracker.app.presentation.activities.ActivitiesViewModel
import com.timetracker.app.presentation.activities.EditActivityViewModel
import com.timetracker.app.presentation.groups.GroupsViewModel
import com.timetracker.app.presentation.entry.EditEntryViewModel
import com.timetracker.app.presentation.export.ExportViewModel
import com.timetracker.app.presentation.settings.SettingsViewModel
import com.timetracker.app.presentation.goals.GoalsViewModel
import com.timetracker.app.presentation.goals.EditGoalViewModel
import com.timetracker.app.presentation.pomodoro.PomodoroViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect fun platformModule(): Module

fun appModule(): Module = module {
    includes(platformModule())

    single {
        val driver = get<DriverFactory>().createDriver()
        AppDatabase(driver)
    }

    single { ActivityGroupRepository(get()) }
    single { ActivityRepository(get()) }
    single { TimeEntryRepository(get()) }
    single { ActiveTimerRepository(get()) }
    single { SettingsRepository(get()) }
    single { GoalRepository(get()) }
    single { CustomFieldRepository(get()) }

    factory { StartTimerUseCase(get()) }
    factory { StopTimerUseCase(get(), get()) }
    factory { PauseTimerUseCase(get()) }
    factory { ResumeTimerUseCase(get()) }
    factory { GetStatsUseCase(get(), get()) }
    factory { CheckGoalsUseCase(get(), get()) }

    viewModel { TrackerViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get()) }
    viewModel { StatsViewModel(get(), get()) }
    viewModel { ActivitiesViewModel(get(), get()) }
    viewModel { EditActivityViewModel(get(), get()) }
    viewModel { GroupsViewModel(get()) }
    viewModel { EditEntryViewModel(get(), get()) }
    viewModel { ExportViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { GoalsViewModel(get(), get(), get()) }
    viewModel { EditGoalViewModel(get(), get()) }
    viewModel { PomodoroViewModel(get(), get(), get()) }
}
