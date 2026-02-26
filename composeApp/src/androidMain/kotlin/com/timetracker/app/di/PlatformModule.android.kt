package com.timetracker.app.di

import com.timetracker.app.data.db.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DriverFactory(get()) }
}
