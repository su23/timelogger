package com.timetracker.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.timetracker.app.data.db.AppDatabase
import com.timetracker.app.domain.model.ActiveTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class ActiveTimerRepository(private val db: AppDatabase) {

    private val queries get() = db.activeTimerQueries

    fun observeAll(): Flow<List<ActiveTimer>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    fun observeByActivityId(activityId: Long): Flow<ActiveTimer?> =
        queries.selectByActivityId(activityId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    suspend fun getByActivityId(activityId: Long): ActiveTimer? = withContext(Dispatchers.IO) {
        queries.selectByActivityId(activityId).executeAsOneOrNull()?.toDomain()
    }

    suspend fun insert(timer: ActiveTimer) = withContext(Dispatchers.IO) {
        queries.insert(
            timer.activityId,
            timer.startTime.toEpochMilliseconds(),
            timer.accumulatedMs,
            if (timer.isPaused) 1L else 0L,
            timer.pausedAt?.toEpochMilliseconds(),
        )
    }

    suspend fun update(timer: ActiveTimer) = withContext(Dispatchers.IO) {
        queries.update(
            timer.startTime.toEpochMilliseconds(),
            timer.accumulatedMs,
            if (timer.isPaused) 1L else 0L,
            timer.pausedAt?.toEpochMilliseconds(),
            timer.id,
        )
    }

    suspend fun deleteByActivityId(activityId: Long) = withContext(Dispatchers.IO) {
        queries.deleteByActivityId(activityId)
    }
}

private fun com.timetracker.app.data.db.ActiveTimer.toDomain() = ActiveTimer(
    id = id,
    activityId = activity_id,
    startTime = Instant.fromEpochMilliseconds(start_time),
    accumulatedMs = accumulated_ms,
    isPaused = is_paused != 0L,
    pausedAt = paused_at?.let { Instant.fromEpochMilliseconds(it) },
)
