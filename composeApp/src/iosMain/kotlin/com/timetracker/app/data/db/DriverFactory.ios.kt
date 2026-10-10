package com.timetracker.app.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = AppDatabase.Schema,
            name = "timetracker.db",
            // Matches the Android driver: without this the schema's ON DELETE
            // CASCADE clauses never fire.
            onConfiguration = { config: DatabaseConfiguration ->
                config.copy(
                    extendedConfig = config.extendedConfig.copy(foreignKeyConstraints = true),
                )
            },
        )
    }
}
