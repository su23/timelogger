package com.timetracker.app.domain.usecase

import com.timetracker.app.data.repository.ActivityRepository
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.model.ActivityStats
import com.timetracker.app.domain.model.DailyTotal
import kotlin.time.Instant

class GetStatsUseCase(
    private val timeEntryRepo: TimeEntryRepository,
    private val activityRepo: ActivityRepository,
) {
    suspend fun getActivityStats(start: Instant, end: Instant): List<ActivityStats> {
        val rawStats = timeEntryRepo.getStatsByDateRange(start, end)
        if (rawStats.isEmpty()) return emptyList()

        val totalMs = rawStats.sumOf { it.second.first }

        return rawStats.mapNotNull { (activityId, pair) ->
            val (durationMs, count) = pair
            val activity = activityRepo.getById(activityId) ?: return@mapNotNull null
            ActivityStats(
                activity = activity,
                totalDurationMs = durationMs,
                entryCount = count,
                percentage = if (totalMs > 0) (durationMs.toFloat() / totalMs) * 100f else 0f,
            )
        }.sortedByDescending { it.totalDurationMs }
    }

    suspend fun getDailyTotals(start: Instant, end: Instant): List<DailyTotal> {
        return timeEntryRepo.getDailyTotalsByDateRange(start, end)
    }
}
