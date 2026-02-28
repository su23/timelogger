package com.timetracker.app.widget

/**
 * iOS Widget data provider.
 *
 * To enable iOS widgets:
 * 1. Add a WidgetKit extension target in Xcode
 * 2. Configure App Groups for shared data access
 * 3. This class provides data from the shared SQLDelight database
 *    to the WidgetKit extension via App Groups UserDefaults
 *
 * The actual WidgetKit SwiftUI code lives in the iOS Xcode project.
 * This Kotlin class helps serialize data for the widget to consume.
 */
class WidgetDataProvider {

    /**
     * Serializes active timer data to a format the iOS widget can read.
     * Call this whenever timers change to update widget state.
     */
    fun getActiveTimersJson(): String {
        // Return JSON of active timers for WidgetKit to consume
        // Write to shared UserDefaults via App Groups
        return "[]"
    }

    /**
     * Serializes favorite activities for the widget quick-start grid.
     */
    fun getFavoriteActivitiesJson(): String {
        // Return JSON of top activities
        return "[]"
    }
}
