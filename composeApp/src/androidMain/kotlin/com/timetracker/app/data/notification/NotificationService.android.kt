package com.timetracker.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

actual class NotificationService(private val context: Context) {

    companion object {
        private const val CHANNEL_TIMER = "timer_channel"
        private const val CHANNEL_GOALS = "goals_channel"
        private const val NOTIFICATION_TIMER = 1001
        private const val NOTIFICATION_COUNTDOWN = 1002
        private const val NOTIFICATION_GOAL = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val timerChannel = NotificationChannel(
                CHANNEL_TIMER,
                "Timer",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Running timer notifications"
            }

            val goalsChannel = NotificationChannel(
                CHANNEL_GOALS,
                "Goals",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Goal reached notifications"
            }

            manager.createNotificationChannels(listOf(timerChannel, goalsChannel))
        }
    }

    actual fun showTimerRunning(activityName: String, elapsed: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("Tracking: $activityName")
            .setContentText("Elapsed: $elapsed")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_TIMER, notification)
    }

    actual fun showTimerCountdownComplete(activityName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Countdown Complete")
            .setContentText("$activityName timer has finished")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_COUNTDOWN, notification)
    }

    actual fun showGoalReached(activityName: String, goalDescription: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_GOALS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Goal Reached!")
            .setContentText("$activityName: $goalDescription")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_GOAL, notification)
    }

    actual fun cancelTimerNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_TIMER)
    }

    actual fun requestPermissions() {
        // On Android 13+, POST_NOTIFICATIONS permission is needed
        // This would typically be handled by the Activity
    }
}
