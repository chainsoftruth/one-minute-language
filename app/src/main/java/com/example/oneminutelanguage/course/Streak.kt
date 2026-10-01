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
