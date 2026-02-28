package com.timetracker.app.data.notification

import platform.UserNotifications.*

actual class NotificationService {

    actual fun showTimerRunning(activityName: String, elapsed: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Tracking: $activityName")
            setBody("Elapsed: $elapsed")
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "timer_running",
            content = content,
            trigger = null,
        )
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ -> }
    }

    actual fun showTimerCountdownComplete(activityName: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Countdown Complete")
            setBody("$activityName timer has finished")
            setSound(UNNotificationSound.defaultSound())
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "timer_countdown",
            content = content,
            trigger = null,
        )
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ -> }
    }

    actual fun showGoalReached(activityName: String, goalDescription: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Goal Reached!")
            setBody("$activityName: $goalDescription")
            setSound(UNNotificationSound.defaultSound())
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "goal_reached",
            content = content,
            trigger = null,
        )
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ -> }
    }

    actual fun cancelTimerNotification() {
        UNUserNotificationCenter.currentNotificationCenter()
            .removeDeliveredNotificationsWithIdentifiers(listOf("timer_running"))
    }

    actual fun requestPermissions() {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { _, _ -> }
    }
}
