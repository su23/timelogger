package com.timetracker.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.timetracker.app.data.db.AppDatabase
import com.timetracker.app.domain.model.CustomField
import com.timetracker.app.domain.model.CustomFieldType
import com.timetracker.app.domain.model.CustomFieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class CustomFieldRepository(private val db: AppDatabase) {

    private val fieldQueries get() = db.customFieldQueries
    private val valueQueries get() = db.customFieldValueQueries

    // Custom Field definitions
    fun observeByActivityId(activityId: Long): Flow<List<CustomField>> =
        fieldQueries.selectByActivityId(activityId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map {
                    CustomField(
                        id = it.id,
                        activityId = it.activity_id,
                        name = it.name,
                        fieldType = CustomFieldType.fromValue(it.field_type),
                        options = it.options,
                        sortOrder = it.sort_order.toInt(),
                    )
                }
            }

    suspend fun insertField(field: CustomField): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        fieldQueries.insert(
            field.activityId,
            field.name,
            field.fieldType.value,
            field.options,
            field.sortOrder.toLong(),
            now,
            now,
        )
        fieldQueries.lastInsertId().executeAsOne()
    }

    suspend fun updateField(field: CustomField) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        fieldQueries.update(
            field.name,
            field.fieldType.value,
            field.options,
            field.sortOrder.toLong(),
            now,
            field.id,
        )
    }

    suspend fun deleteField(id: Long) = withContext(Dispatchers.IO) {
        fieldQueries.deleteById(id)
    }

    // Custom Field values
    fun observeValuesByEntryId(entryId: Long): Flow<List<CustomFieldValue>> =
        valueQueries.selectByEntryId(entryId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map {
                    CustomFieldValue(
                        id = it.id,
                        entryId = it.entry_id,
                        fieldId = it.field_id,
                        value = it.value_,
                    )
                }
            }

    suspend fun setFieldValue(entryId: Long, fieldId: Long, value: String) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        valueQueries.insert(entryId, fieldId, value, now, now)
    }

    suspend fun deleteValuesByEntryId(entryId: Long) = withContext(Dispatchers.IO) {
        valueQueries.deleteByEntryId(entryId)
    }
}
