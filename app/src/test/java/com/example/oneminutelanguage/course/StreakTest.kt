package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakTest {
    private val today = LocalDate.of(2026, 10, 1)

    @Test fun empty() = assertEquals(0, streak(emptyList(), today))

    @Test fun todayOnly() = assertEquals(1, streak(listOf(today), today))

    @Test fun gapBreaksTheStreak() =
        assertEquals(2, streak(listOf(today, today.minusDays(1), today.minusDays(3)), today))

    @Test fun endingYesterdayStillCounts() =
        assertEquals(2, streak(listOf(today.minusDays(1), today.minusDays(2)), today))

    @Test fun olderThanYesterdayIsZero() = assertEquals(0, streak(listOf(today.minusDays(2)), today))
}

class StreakStatsTest {
    private fun d(day: Int) = LocalDate.of(2026, 10, day)

    @Test fun longestStreakFindsTheBestRun() {
        assertEquals(0, longestStreak(emptyList()))
        assertEquals(3, longestStreak(listOf(d(10), d(1), d(2), d(3), d(5), d(6))))
        assertEquals(1, longestStreak(listOf(d(1), d(3), d(5))))
    }

    @Test fun activityStepHasFourSteps() {
        assertEquals(listOf(0, 1, 1, 2, 3, 4), listOf(0, 1, 4, 5, 10, 20).map(::activityStep))
    }

    @Test fun gridHasTwelveWeeksOfMondayToSundayEndingWithToday() {
        val today = d(1) // a Thursday
        val grid = activityGrid(today)
        assertEquals(12, grid.size)
        assertTrue(grid.all { it.size == 7 })
        assertEquals(java.time.DayOfWeek.MONDAY, grid.first().first()!!.dayOfWeek)
        assertEquals(today, grid.last().last { it != null })
        assertEquals(listOf(null, null, null), grid.last().takeLast(3)) // Fri, Sat, Sun are still to come
        assertEquals(today.minusWeeks(11).minusDays(3), activityStart(today))
    }
}
