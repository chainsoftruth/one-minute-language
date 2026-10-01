package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerCheckerTest {
    @Test fun caseWhitespaceAndPunctuationAreIgnored() {
        assertEquals(Verdict.CORRECT, check("  Het   HUIS is groot. ", listOf("het huis is groot"), false))
        assertEquals(Verdict.CORRECT, check("Waar woon je?", listOf("Waar woon je"), false))
        assertEquals(Verdict.CORRECT, check("ja , graag", listOf("ja, graag"), false))
    }

    @Test fun commasAreNotGraded() {
        assertEquals(Verdict.CORRECT, check("Als het regent blijf ik thuis", listOf("Als het regent, blijf ik thuis"), false))
        assertEquals(Verdict.CORRECT, check("Als het regent,blijf ik thuis.", listOf("Als het regent, blijf ik thuis"), false))
        assertEquals(Verdict.WRONG, check("Als het regent blijf thuis ik", listOf("Als het regent, blijf ik thuis"), false))
    }

    @Test fun curlyApostrophesMatch() {
        assertEquals(Verdict.CORRECT, check("auto’s", listOf("auto's"), false))
    }

    @Test fun anyAcceptedAnswerCounts() {
        assertEquals(Verdict.CORRECT, check("wou", listOf("wilde", "wou"), false))
    }

    @Test fun missingDiaeresisIsAccent() {
        assertEquals(Verdict.ACCENT, check("tweeenzeventig", listOf("tweeënzeventig"), false))
    }

    @Test fun strictNeverReturnsAlmost() {
        assertEquals(Verdict.WRONG, check("word", listOf("wordt"), false))
        assertEquals(Verdict.WRONG, check("de", listOf("het"), false))
    }

    @Test fun lenientToleratesOneTypoPerEightChars() {
        assertEquals(Verdict.ALMOST, check("ik heb een hnd", listOf("ik heb een hond"), true))
        // Short answer: no tolerance.
        assertEquals(Verdict.WRONG, check("wordt", listOf("word"), true))
    }

    @Test fun emptyInputIsWrong() {
        assertEquals(Verdict.WRONG, check("  ", listOf("het"), false))
    }

    @Test fun missingArticleIsDetected() {
        assertTrue(missingArticle("fiets", listOf("de fiets")))
        assertTrue(!missingArticle("de fiets", listOf("de fiets")))
        assertTrue(!missingArticle("tafel", listOf("de fiets")))
    }

    @Test fun diffWordsFlagsOnlyMissingWords() {
        val diff = diffWords("de kinderen spelen in park", "De kinderen spelen in het park.")
        assertEquals(listOf("De", "kinderen", "spelen", "in", "het", "park."), diff.map { it.first })
        assertEquals(listOf(true, true, true, true, false, true), diff.map { it.second })
    }
}
