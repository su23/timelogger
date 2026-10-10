package com.timetracker.app.domain.usecase

import com.timetracker.app.data.repository.ActiveTimerRepository
import com.timetracker.app.data.repository.TimeEntryRepository
import com.timetracker.app.domain.model.ActiveTimer
import com.timetracker.app.domain.model.TimeEntry
import kotlin.time.Clock
import kotlin.time.Instant

class StartTimerUseCase(
    private val activeTimerRepo: ActiveTimerRepository,
) {
    suspend operator fun invoke(activityId: Long) {
        val existing = activeTimerRepo.getByActivityId(activityId)
        if (existing != null) return
        val now = Clock.System.now()
        activeTimerRepo.insert(
            ActiveTimer(
                activityId = activityId,
                startTime = now,
            )
        )
    }
}

class StopTimerUseCase(
    private val activeTimerRepo: ActiveTimerRepository,
    private val timeEntryRepo: TimeEntryRepository,
) {
    suspend operator fun invoke(activityId: Long) {
        val timer = activeTimerRepo.getByActivityId(activityId) ?: return
        val now = Clock.System.now()
        val elapsed = if (timer.isPaused) {
            timer.accumulatedMs
        } else {
            timer.accumulatedMs + (now.toEpochMilliseconds() - timer.startTime.toEpochMilliseconds())
        }
        if (elapsed > 0) {
            // A paused timer stopped later did no work while it sat paused, so the entry
            // ends when it was paused, not now. The start is derived from the end and the
            // measured duration: ActiveTimer.startTime is reset on every resume, so it is
            // the start of the last running stretch rather than of the entry as a whole.
            val endTime = if (timer.isPaused) timer.pausedAt ?: now else now
            val startTime = Instant.fromEpochMilliseconds(endTime.toEpochMilliseconds() - elapsed)
            timeEntryRepo.insert(
                TimeEntry(
                    activityId = activityId,
                    startTime = startTime,
                    endTime = endTime,
                    durationMs = elapsed,
                )
            )
        }
        activeTimerRepo.deleteByActivityId(activityId)
    }
}

class PauseTimerUseCase(
    private val activeTimerRepo: ActiveTimerRepository,
) {
    suspend operator fun invoke(activityId: Long) {
        val timer = activeTimerRepo.getByActivityId(activityId) ?: return
        if (timer.isPaused) return
        val now = Clock.System.now()
        val elapsed = now.toEpochMilliseconds() - timer.startTime.toEpochMilliseconds()
        activeTimerRepo.update(
            timer.copy(
                accumulatedMs = timer.accumulatedMs + elapsed,
                isPaused = true,
                pausedAt = now,
            )
        )
    }
}

class ResumeTimerUseCase(
    private val activeTimerRepo: ActiveTimerRepository,
) {
    suspend operator fun invoke(activityId: Long) {
        val timer = activeTimerRepo.getByActivityId(activityId) ?: return
        if (!timer.isPaused) return
        val now = Clock.System.now()
        activeTimerRepo.update(
            timer.copy(
                startTime = now,
                isPaused = false,
                pausedAt = null,
            )
        )
    }
}
