package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionarySearchTest {
    private val lopen = LexEntry("lopen", "lopen", "to walk", "verb", "A1", "daily_life", pres = "loopt", past = "liep/liepen", pp = "gelopen", aux = "heeft/is")
    private val fiets = LexEntry("fiets", "fiets", "bicycle", "noun", "A1", "travel", art = "de", pl = "fietsen")
    private val loon = LexEntry("loon", "loon", "wage", "noun", "A2", "work", art = "het", pl = "lonen")
    private val tweeenzeventig = LexEntry("tweeenzeventig", "tweeënzeventig", "seventy-two", "num", "A1", "time_numbers")
    private val all = listOf(lopen, fiets, loon, tweeenzeventig)

    @Test fun anInflectedFormFindsTheDictionaryWord() {
        assertEquals(listOf("lopen"), searchLexicon(all, "liep").map { it.id })
        assertEquals(listOf("lopen"), searchLexicon(all, "gelopen").map { it.id })
        assertEquals(listOf("fiets"), searchLexicon(all, "fietsen").map { it.id })
    }

    @Test fun englishWordsAndAccentsMatch() {
        assertEquals(listOf("lopen"), searchLexicon(all, "walk").map { it.id })
        assertEquals(listOf("fiets"), searchLexicon(all, "BICYCLE").map { it.id })
        assertEquals(listOf("tweeenzeventig"), searchLexicon(all, "tweeenzeventig").map { it.id })
    }

    @Test fun exactMatchesComeBeforePrefixMatches() {
        val result = searchLexicon(all, "lo").map { it.id }
        assertEquals(listOf("loon", "lopen"), result)
        assertEquals("loon", searchLexicon(all, "loon").first().id)
    }

    @Test fun levelAndTopicFilterAndAnEmptyQueryListsEverythingAlphabetically() {
        assertEquals(listOf("loon"), searchLexicon(all, "", level = "A2").map { it.id })
        assertEquals(listOf("fiets"), searchLexicon(all, "", topic = "travel").map { it.id })
        assertEquals(all.map { it.nl }.sortedBy { stripDiacritics(it) }, searchLexicon(all, "").map { it.nl })
        assertTrue(searchLexicon(all, "xyz").isEmpty())
    }
}
