package com.timetracker.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.timetracker.app.data.db.AppDatabase
import com.timetracker.app.domain.model.ActivityStats
import com.timetracker.app.domain.model.DailyTotal
import com.timetracker.app.domain.model.TimeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

class TimeEntryRepository(private val db: AppDatabase) {

    private val queries get() = db.timeEntryQueries

    fun observeByDateRange(start: Instant, end: Instant): Flow<List<TimeEntry>> =
        queries.selectByDateRange(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    fun observeById(id: Long): Flow<TimeEntry?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    suspend fun getStatsByDateRange(start: Instant, end: Instant): List<Pair<Long, Pair<Long, Long>>> =
        withContext(Dispatchers.IO) {
            queries.statsByDateRange(start.toEpochMilliseconds(), end.toEpochMilliseconds())
                .executeAsList()
                .map { row ->
                    row.activity_id to (row.total_duration!! to row.entry_count)
                }
        }

    suspend fun getDailyTotalsByDateRange(start: Instant, end: Instant): List<DailyTotal> =
        withContext(Dispatchers.IO) {
            queries.dailyTotalsByDateRange(start.toEpochMilliseconds(), end.toEpochMilliseconds())
                .executeAsList()
                .map { row ->
                    DailyTotal(
                        activityId = row.activity_id,
                        dayEpoch = row.day_bucket,
                        totalDurationMs = row.total_duration!!,
                    )
                }
        }

    suspend fun insert(entry: TimeEntry): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insert(
            entry.activityId,
            entry.startTime.toEpochMilliseconds(),
            entry.endTime.toEpochMilliseconds(),
            entry.durationMs,
            entry.comment,
            now,
            now,
        )
        queries.lastInsertId().executeAsOne()
    }

    suspend fun update(entry: TimeEntry) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.update(
            entry.activityId,
            entry.startTime.toEpochMilliseconds(),
            entry.endTime.toEpochMilliseconds(),
            entry.durationMs,
            entry.comment,
            now,
            entry.id,
        )
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteById(id)
    }
}

private fun com.timetracker.app.data.db.TimeEntry.toDomain() = TimeEntry(
    id = id,
    activityId = activity_id,
    startTime = Instant.fromEpochMilliseconds(start_time),
    endTime = Instant.fromEpochMilliseconds(end_time),
    durationMs = duration_ms,
    comment = comment,
)
