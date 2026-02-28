package com.timetracker.app.domain.usecase

import com.timetracker.app.data.repository.GoalRepository
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.model.*
import com.timetracker.app.util.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

class CheckGoalsUseCase(
    private val goalRepo: GoalRepository,
    private val timeEntryRepo: TimeEntryRepository,
) {
    suspend fun checkAllGoals(activities: Map<Long, Activity>): List<GoalProgress> {
        val goals = goalRepo.observeAll().first()
        return goals.mapNotNull { goal ->
            val activity = activities[goal.activityId] ?: return@mapNotNull null
            val (start, end) = getRangeForPeriod(goal.period)
            val stats = timeEntryRepo.getStatsByDateRange(start, end)
            val activityStats = stats.find { it.first == goal.activityId }
            val currentMs = activityStats?.second?.first ?: 0L
            val progressPercent = if (goal.targetMs > 0) {
                (currentMs.toFloat() / goal.targetMs * 100f).coerceAtMost(100f)
            } else {
                0f
            }
            val isReached = when (goal.goalType) {
                GoalType.Min -> currentMs >= goal.targetMs
                GoalType.Max -> currentMs >= goal.targetMs
            }
            GoalProgress(
                goal = goal,
                activity = activity,
                currentMs = currentMs,
                progressPercent = progressPercent,
                isReached = isReached,
            )
        }
    }

    private fun getRangeForPeriod(period: GoalPeriod): Pair<Instant, Instant> = when (period) {
        GoalPeriod.Daily -> todayRange()
        GoalPeriod.Weekly -> thisWeekRange()
        GoalPeriod.Monthly -> thisMonthRange()
    }
}
