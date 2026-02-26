package com.timetracker.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.timetracker.app.data.db.AppDatabase
import com.timetracker.app.domain.model.ActivityGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class ActivityGroupRepository(private val db: AppDatabase) {

    private val queries get() = db.activityGroupQueries

    fun observeAll(): Flow<List<ActivityGroup>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    fun observeById(id: Long): Flow<ActivityGroup?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    suspend fun insert(group: ActivityGroup): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insert(group.name, group.color.toLong(), group.sortOrder.toLong(), now, now)
        queries.lastInsertId().executeAsOne()
    }

    suspend fun update(group: ActivityGroup) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.update(group.name, group.color.toLong(), group.sortOrder.toLong(), now, group.id)
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteById(id)
    }
}

private fun com.timetracker.app.data.db.ActivityGroup.toDomain() = ActivityGroup(
    id = id,
    name = name,
    color = color.toInt(),
    sortOrder = sort_order.toInt(),
)
