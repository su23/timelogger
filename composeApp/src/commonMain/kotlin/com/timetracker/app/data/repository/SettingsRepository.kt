package com.timetracker.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.timetracker.app.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepository(private val db: AppDatabase) {

    private val queries get() = db.appSettingQueries

    object Keys {
        const val THEME = "theme" // "system", "light", "dark"
        const val ALLOW_SIMULTANEOUS = "allow_simultaneous" // "true", "false"
        const val POMODORO_WORK_MINUTES = "pomodoro_work_minutes" // "25"
        const val POMODORO_BREAK_MINUTES = "pomodoro_break_minutes" // "5"
        const val POMODORO_LONG_BREAK_MINUTES = "pomodoro_long_break_minutes" // "15"
        const val POMODORO_SESSIONS_BEFORE_LONG = "pomodoro_sessions_before_long" // "4"
        const val DEFAULT_VIEW = "default_view" // "tracker", "history", "stats"
    }

    suspend fun getString(key: String, default: String = ""): String = withContext(Dispatchers.IO) {
        queries.selectByKey(key).executeAsOneOrNull() ?: default
    }

    suspend fun setString(key: String, value: String) = withContext(Dispatchers.IO) {
        queries.upsert(key, value)
    }

    suspend fun getBoolean(key: String, default: Boolean = false): Boolean =
        getString(key, default.toString()).toBooleanStrictOrNull() ?: default

    suspend fun setBoolean(key: String, value: Boolean) =
        setString(key, value.toString())

    suspend fun getInt(key: String, default: Int = 0): Int =
        getString(key, default.toString()).toIntOrNull() ?: default

    suspend fun setInt(key: String, value: Int) =
        setString(key, value.toString())

    fun observeAll(): Flow<Map<String, String>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.associate { it.key to it.value_ } }
}
