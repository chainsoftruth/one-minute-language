package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
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
