package com.timetracker.app.data.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Clears rows orphaned by deletes made before foreign keys were enabled on the
 * connection. With the constraints now enforced the database cannot produce new
 * orphans, so after the first run this finds nothing.
 */
class DatabaseMaintenance(private val db: AppDatabase) {

    suspend fun deleteOrphanedRows() = withContext(Dispatchers.IO) {
        db.transaction {
            with(db.maintenanceQueries) {
                // Parents first: removing an orphaned entry or field orphans its
                // values in turn, so the value sweep has to run last to catch them.
                deleteOrphanedTimeEntries()
                deleteOrphanedCustomFields()
                deleteOrphanedActiveTimers()
                deleteOrphanedGoals()
                deleteOrphanedCustomFieldValues()
            }
        }
    }
}
