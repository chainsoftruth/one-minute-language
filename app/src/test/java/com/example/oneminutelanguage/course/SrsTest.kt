package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsTest {
    @Test fun goodThreeTimesGivesOneThreeAndSevenAndAHalfDays() {
        val first = schedule(NEW_CARD, Grade.GOOD)
        val second = schedule(first, Grade.GOOD)
        val third = schedule(second, Grade.GOOD)
        assertEquals(1.0, first.intervalDays, 0.0001)
        assertEquals(3.0, second.intervalDays, 0.0001)
        assertEquals(7.5, third.intervalDays, 0.0001)
        assertEquals(3, third.reps)
    }

    @Test fun againResetsRepsAndLowersEaseWithAFloor() {
        var card = schedule(schedule(NEW_CARD, Grade.GOOD), Grade.GOOD)
        card = schedule(card, Grade.AGAIN)
        assertEquals(0, card.reps)
        assertEquals(0.0, card.intervalDays, 0.0001)
        assertEquals(2.3, card.ease, 0.0001)
        assertEquals(1, card.lapses)
        repeat(20) { card = schedule(card, Grade.AGAIN) }
        assertEquals(1.3, card.ease, 0.0001)
    }

    @Test fun hardKeepsAtLeastADayAndEasyGrows() {
        val hard = schedule(NEW_CARD, Grade.HARD)
        assertEquals(1.0, hard.intervalDays, 0.0001)
        assertEquals(2.35, hard.ease, 0.0001)
        assertEquals(3.0, schedule(NEW_CARD, Grade.EASY).intervalDays, 0.0001)
        val easy = schedule(schedule(NEW_CARD, Grade.GOOD), Grade.EASY)
        assertEquals(1.0 * 2.5 * 1.3, easy.intervalDays, 0.0001)
        assertEquals(2.65, easy.ease, 0.0001)
    }

    @Test fun intervalIsCappedAt180Days() {
        val big = CardState(intervalDays = 170.0, ease = 2.5, reps = 5, lapses = 0)
        assertEquals(180.0, schedule(big, Grade.GOOD).intervalDays, 0.0001)
        assertEquals(180.0, schedule(big, Grade.EASY).intervalDays, 0.0001)
    }

    @Test fun dueAtIsTenMinutesForIntervalZeroAndDaysOtherwise() {
        assertEquals(1_000L + 600_000L, dueAt(1_000L, NEW_CARD))
        assertEquals(1_000L + 3 * 86_400_000L, dueAt(1_000L, CardState(3.0, 2.5, 2, 0)))
    }

    @Test fun masteredFromTwentyOneDays() {
        assertFalse(CardState(20.9, 2.5, 4, 0).isMastered())
        assertTrue(CardState(21.0, 2.5, 4, 0).isMastered())
    }

    @Test fun etaTextPicksTheNaturalUnit() {
        assertEquals("now", etaText(1_000L, 1_000L))
        assertEquals("now", etaText(5_000L, 1_000L))
        assertEquals("10 min", etaText(0L, 10 * 60_000L))
        assertEquals("6 h", etaText(0L, 6 * 3_600_000L))
        assertEquals("3 days", etaText(0L, 3 * 86_400_000L))
    }

    @Test fun finishingAVocabLessonSchedulesEveryWordOnceWrongOnesSoonerThanTomorrow() {
        val now = 10_000_000L
        val cards = vocabCardSchedule(listOf("fiets", "huis", "fiets", "kat"), setOf("huis"), now)
        assertEquals(listOf("lex:fiets", "lex:huis", "lex:kat"), cards.map { it.first })
        assertEquals(now + 86_400_000L, cards[0].second)
        assertEquals(now + 600_000L, cards[1].second)
        assertEquals(now + 86_400_000L, cards[2].second)
    }
}
