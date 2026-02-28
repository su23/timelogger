package com.timetracker.app.data.notification

expect class NotificationService {
    fun showTimerRunning(activityName: String, elapsed: String)
    fun showTimerCountdownComplete(activityName: String)
    fun showGoalReached(activityName: String, goalDescription: String)
    fun cancelTimerNotification()
    fun requestPermissions()
}
