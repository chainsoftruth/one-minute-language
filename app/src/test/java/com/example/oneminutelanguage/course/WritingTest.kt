package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WritingTest {
    @Test fun parsesARecordedLanguageToolResponse() {
        val json = javaClass.getResource("/lt_sample.json")!!.readText()
        val text = "Hij werken in een winkel en woont in Utrech."
        val matches = parseLanguageTool(json)
        assertEquals(1, matches.size)
        val m = matches[0]
        assertEquals("Utrech", text.substring(m.offset, m.offset + m.length))
        assertEquals("Utrecht", m.replacements.first().value)
        assertEquals("misspelling", m.rule.issueType)
    }

    @Test fun anEmptyAnswerHasNoMatches() {
        assertTrue(parseLanguageTool("""{"software":{"name":"LanguageTool"},"matches":[]}""").isEmpty())
    }

    private fun match(offset: Int, length: Int) = LtMatch("m", offset, length, listOf(LtReplacement("x")))

    @Test fun applyFixReplacesTheRangeAndMovesLaterMatches() {
        val text = "Ik hebben een boek en een pen"
        val first = match(3, 6) // hebben
        val later = match(22, 3) // the second een, before pen
        val (fixed, rest) = applyFix(text, listOf(first, later), first, "heb")
        assertEquals("Ik heb een boek en een pen", fixed)
        assertEquals(1, rest.size)
        assertEquals("een", fixed.substring(rest[0].offset, rest[0].offset + rest[0].length))
    }

    @Test fun applyFixDropsAnOverlappingMatchAndKeepsEarlierOnes() {
        val text = "De huis is groot"
        val earlier = match(0, 2)
        val overlapping = match(1, 4)
        val after = match(8, 2)
        val (fixed, rest) = applyFix(text, listOf(earlier, overlapping, after), earlier, "Het")
        assertEquals("Het huis is groot", fixed)
        assertEquals(listOf(9), rest.map { it.offset })
    }

    @Test fun wordCountIgnoresPunctuationAndExtraSpaces() {
        assertEquals(0, wordCount("   "))
        assertEquals(0, wordCount(" . ! "))
        assertEquals(5, wordCount("Hallo!  Ik ben\nMia, hoi."))
    }

    @Test fun writingScoreIsTheShareOfCoveredPointsAndZeroWhenTooShort() {
        assertEquals(0.8, writingScore(4, 5, 50, 40), 1e-9)
        assertEquals(0.0, writingScore(5, 5, 39, 40), 1e-9)
        assertEquals(1.0, writingScore(0, 0, 50, 40), 1e-9)
        assertEquals(1.0, writingScore(9, 5, 50, 40), 1e-9)
    }
}
