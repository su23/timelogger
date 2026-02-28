package com.timetracker.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.timetracker.app.data.db.AppDatabase
import com.timetracker.app.domain.model.Goal
import com.timetracker.app.domain.model.GoalPeriod
import com.timetracker.app.domain.model.GoalType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class GoalRepository(private val db: AppDatabase) {

    private val queries get() = db.goalQueries

    fun observeAll(): Flow<List<Goal>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    fun observeById(id: Long): Flow<Goal?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    fun observeByActivityId(activityId: Long): Flow<List<Goal>> =
        queries.selectByActivityId(activityId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    suspend fun insert(goal: Goal): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insert(
            goal.activityId,
            goal.goalType.value,
            goal.period.value,
            goal.targetMs,
            if (goal.isActive) 1L else 0L,
            now,
            now,
        )
        queries.lastInsertId().executeAsOne()
    }

    suspend fun update(goal: Goal) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.update(
            goal.activityId,
            goal.goalType.value,
            goal.period.value,
            goal.targetMs,
            if (goal.isActive) 1L else 0L,
            now,
            goal.id,
        )
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteById(id)
    }
}

private fun com.timetracker.app.data.db.Goal.toDomain() = Goal(
    id = id,
    activityId = activity_id,
    goalType = GoalType.fromValue(goal_type),
    period = GoalPeriod.fromValue(period),
    targetMs = target_ms,
    isActive = is_active != 0L,
)
