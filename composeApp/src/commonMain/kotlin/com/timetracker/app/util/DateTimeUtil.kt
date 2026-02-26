package com.timetracker.app.util

import kotlinx.datetime.*

fun Instant.toLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    toLocalDateTime(timeZone).date

fun LocalDate.atStartOfDayInstant(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant =
    atStartOfDayIn(timeZone)

fun LocalDate.atEndOfDayInstant(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant =
    plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

fun todayRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
    val today = Clock.System.now().toLocalDate(timeZone)
    return today.atStartOfDayInstant(timeZone) to today.atEndOfDayInstant(timeZone)
}

fun thisWeekRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
    val today = Clock.System.now().toLocalDate(timeZone)
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
    val nextMonday = monday.plus(7, DateTimeUnit.DAY)
    return monday.atStartOfDayInstant(timeZone) to nextMonday.atStartOfDayInstant(timeZone)
}

fun thisMonthRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
    val today = Clock.System.now().toLocalDate(timeZone)
    val firstOfMonth = LocalDate(today.year, today.month, 1)
    val firstOfNext = firstOfMonth.plus(1, DateTimeUnit.MONTH)
    return firstOfMonth.atStartOfDayInstant(timeZone) to firstOfNext.atStartOfDayInstant(timeZone)
}
