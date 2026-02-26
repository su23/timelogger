package com.timetracker.app

import com.timetracker.app.di.appModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule())
    }
}
