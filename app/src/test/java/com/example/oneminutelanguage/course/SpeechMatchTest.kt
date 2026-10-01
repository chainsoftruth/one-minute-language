package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SpeechMatchTest {
    private val target = "Ik woon in Utrecht"

    @Test fun exactIsOne() {
        val m = score("ik woon in utrecht", target)
        assertEquals(1.0, m.score, 0.0)
        assertTrue(m.words.all { it.second })
    }

    @Test fun oneWordMissingFlagsThatWord() {
        val m = score("ik woon Utrecht", target)
        assertEquals(0.75, m.score, 0.0)
        assertEquals(listOf("Ik" to true, "woon" to true, "in" to false, "Utrecht" to true), m.words)
    }

    @Test fun digitsBecomeWords() {
        assertEquals(1.0, score("ik heb 3 katten", "Ik heb drie katten.").score, 0.0)
        assertEquals(1.0, score("ik ben 20", "Ik ben twintig").score, 0.0)
    }

    @Test fun swappedWordsArePenalised() {
        val m = score("woon ik in utrecht", target)
        assertEquals(0.75, m.score, 0.0)
    }

    @Test fun punctuationAndAccentsDoNotMatter() {
        assertEquals(1.0, score("ik heb tweeenzeventig euro", "Ik heb tweeënzeventig euro,").score, 0.0)
        assertEquals(1.0, score("Hallo, ik ben Jan!", "hallo ik ben jan").score, 0.0)
    }

    @Test fun bestPicksTheClosestGuess() {
        assertEquals(1.0, best(listOf("ik woon", "ik woon in utrecht", "x"), target).score, 0.0)
        assertEquals(0.0, best(emptyList(), target).score, 0.0)
    }

    @Test fun gradesFollowThePlanThresholds() {
        assertEquals(Verdict.CORRECT, gradeSpeech(score("ik woon in utrecht", target), target).verdict)
        val almost = gradeSpeech(score("ik woon utrecht", target), target)
        assertEquals(Verdict.ALMOST, almost.verdict)
        assertTrue(almost.correct)
        val wrong = gradeSpeech(score("goedemorgen", target), target)
        assertEquals(Verdict.WRONG, wrong.verdict)
        assertFalse(wrong.correct)
    }

    @Test fun selfGrade() {
        assertTrue(gradeSelf(true, target).correct)
        assertEquals(Grade.AGAIN, gradeFor(gradeSelf(false, target)))
    }

    @Test fun listenChoiceAndDictation() {
        val choice = Item.Listen("Ik woon in Utrecht", options = listOf("I live in Utrecht", "I work in Utrecht"), answer = 0)
        assertTrue(gradeListen(choice, "0").correct)
        assertEquals("I live in Utrecht", gradeListen(choice, "1").expected)
        val dictation = Item.Listen("Ik woon in Utrecht.")
        assertTrue(gradeListen(dictation, "ik woon in utrecht").correct)
        assertFalse(gradeListen(dictation, "ik woon in Utrech").correct)
        // Strict: a one-letter slip is wrong, a missing accent is only a note.
        assertEquals(Verdict.ACCENT, gradeListen(Item.Listen("tweeënzeventig"), "tweeenzeventig").verdict)
        assertEquals("Ik woon in Utrecht.", expectedAnswer(dictation))
    }

    @Test fun dictationOfANounNeedsItsArticleButIsForgiving() {
        val bare = gradeListen(Item.Listen("de fiets"), "fiets")
        assertTrue(bare.correct)
        assertEquals(Verdict.ALMOST, bare.verdict)
    }

    private val lexicon = (1..20).map {
        LexEntry("w$it", "woord$it", "word$it", "noun", "A1", "basics", art = "de", ex = "Zin $it is dit.", exEn = "Sentence $it.")
    }

    @Test fun drillsTopUpFromExampleSentencesAndStayAtTen() {
        val items = listOf<Item>(Item.Listen("Goedemorgen"), Item.Speak("Hallo", "Hello"), Item.Choice("q", listOf("a", "b"), 0))
        val listening = listeningDrill(items, lexicon, Random(1))
        assertEquals(DRILL_SIZE, listening.size)
        assertTrue(listening.all { it is Item.Listen })
        assertTrue(Item.Listen("Goedemorgen") in listening)
        val speaking = speakingDrill(items, lexicon, Random(1))
        assertEquals(DRILL_SIZE, speaking.size)
        assertTrue(speaking.all { it is Item.Speak })
        assertEquals(listening, listeningDrill(items, lexicon, Random(1)))
    }

    @Test fun drillsWithNothingToUseAreEmpty() {
        assertTrue(listeningDrill(emptyList(), emptyList(), Random(1)).isEmpty())
        assertTrue(speakingDrill(listOf(Item.Choice("q", listOf("a", "b"), 0)), emptyList(), Random(1)).isEmpty())
    }
}
