package com.timetracker.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.timetracker.app.data.db.AppDatabase
import com.timetracker.app.domain.model.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class ActivityRepository(private val db: AppDatabase) {

    private val queries get() = db.activityQueries

    fun observeAll(): Flow<List<Activity>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    fun observeAllIncludingArchived(): Flow<List<Activity>> =
        queries.selectAllIncludingArchived()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    fun observeById(id: Long): Flow<Activity?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    suspend fun getById(id: Long): Activity? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    fun observeByGroupId(groupId: Long): Flow<List<Activity>> =
        queries.selectByGroupId(groupId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    suspend fun insert(activity: Activity): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insert(
            activity.name,
            activity.icon,
            activity.color.toLong(),
            activity.groupId,
            if (activity.isArchived) 1L else 0L,
            activity.sortOrder.toLong(),
            now,
            now,
        )
        queries.lastInsertId().executeAsOne()
    }

    suspend fun update(activity: Activity) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.update(
            activity.name,
            activity.icon,
            activity.color.toLong(),
            activity.groupId,
            if (activity.isArchived) 1L else 0L,
            activity.sortOrder.toLong(),
            now,
            activity.id,
        )
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteById(id)
    }

    suspend fun archive(id: Long) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.archive(now, id)
    }

    suspend fun unarchive(id: Long) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.unarchive(now, id)
    }
}

private fun com.timetracker.app.data.db.Activity.toDomain() = Activity(
    id = id,
    name = name,
    icon = icon,
    color = color.toInt(),
    groupId = group_id,
    isArchived = is_archived != 0L,
    sortOrder = sort_order.toInt(),
)
