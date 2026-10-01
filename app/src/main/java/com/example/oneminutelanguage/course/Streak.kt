package com.example.oneminutelanguage.course

import java.time.LocalDate

/** "Active" day = at least this many exercises. */
const val STREAK_MIN_EXERCISES = 5

/** Consecutive active days ending today, or ending yesterday while today has no activity yet. */
fun streak(activeDates: List<LocalDate>, today: LocalDate): Int {
    val days = activeDates.toSet()
    var day = if (today in days) today else today.minusDays(1)
    var count = 0
    while (day in days) {
        count++
        day = day.minusDays(1)
    }
    return count
}

/** The longest run of consecutive active days ever. */
fun longestStreak(activeDates: List<LocalDate>): Int {
    var best = 0
    var run = 0
    var previous: LocalDate? = null
    for (day in activeDates.toSortedSet()) {
        run = if (previous != null && previous.plusDays(1) == day) run + 1 else 1
        best = maxOf(best, run)
        previous = day
    }
    return best
}

/** Colour step of an activity-grid square: 0 = nothing, 1..4 = darker the more exercises were done. */
fun activityStep(exercises: Int): Int = when {
    exercises <= 0 -> 0
    exercises < STREAK_MIN_EXERCISES -> 1
    exercises < 10 -> 2
    exercises < 20 -> 3
    else -> 4
}

/** [weeks] columns of 7 days (Monday first), the last column is the current week; days after [today] are null. */
fun activityGrid(today: LocalDate, weeks: Int = 12): List<List<LocalDate?>> {
    val first = activityStart(today, weeks)
    return List(weeks) { w -> List(7) { d -> first.plusDays(w * 7L + d).takeIf { !it.isAfter(today) } } }
}

/** The Monday of the grid's first column. */
fun activityStart(today: LocalDate, weeks: Int = 12): LocalDate = today.minusDays(today.dayOfWeek.value - 1L).minusWeeks(weeks - 1L)
