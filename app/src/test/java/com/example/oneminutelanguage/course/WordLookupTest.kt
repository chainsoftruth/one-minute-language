package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordLookupTest {
    private fun e(id: String, nl: String, pos: String, vararg f: Pair<String, Any>): LexEntry {
        val m = f.toMap()
        return LexEntry(
            id = id, nl = nl, en = id, pos = pos, lvl = "A1", topic = "basics",
            art = m["art"] as String?, pl = m["pl"] as String?, dim = m["dim"] as String?,
            pres = m["pres"] as String?, past = m["past"] as String?, pp = m["pp"] as String?,
            sep = m["sep"] as Boolean? ?: false, refl = m["refl"] as Boolean? ?: false,
            cmp = m["cmp"] as String?, sup = m["sup"] as String?
        )
    }

    private val lookup = WordLookup(
        listOf(
            e("lopen", "lopen", "verb", "pres" to "loopt", "past" to "liep/liepen", "pp" to "gelopen"),
            e("huis", "huis", "noun", "art" to "het", "pl" to "huizen", "dim" to "huisje"),
            e("groot", "groot", "adj", "cmp" to "groter", "sup" to "grootst"),
            e("bellen", "bellen", "verb", "pres" to "belt", "past" to "belde/belden", "pp" to "gebeld"),
            e("opbellen", "opbellen", "verb", "pres" to "belt op", "past" to "belde op/belden op", "pp" to "opgebeld", "sep" to true),
            e("zijn", "zijn", "verb", "pres" to "is", "past" to "was/waren", "pp" to "geweest"),
            e("wassen", "zich wassen", "verb", "pres" to "wast", "past" to "waste", "pp" to "gewassen", "refl" to true),
            e("man", "man", "noun", "art" to "de", "pl" to "mannen"),
            e("maan", "maan", "noun", "art" to "de", "pl" to "manen"),
            e("wit", "wit", "adj", "cmp" to "witter", "sup" to "witst"),
            e("lief", "lief", "adj"),
            e("auto", "auto", "noun", "art" to "de", "pl" to "auto's"),
            e("ziens", "tot ziens", "phrase"),
            e("tot", "tot", "prep")
        )
    )

    private fun ids(token: String) = lookup.lookup(token).map { it.id }

    @Test fun irregularAndRegularFormsFindTheirVerb() {
        assertEquals(listOf("lopen"), ids("liep"))
        assertEquals(listOf("lopen"), ids("gelopen"))
        assertEquals(listOf("lopen"), ids("loop")) // ik loop: the stem comes from loopt
        assertEquals(listOf("zijn"), ids("ben"))
    }

    @Test fun pluralsAndDiminutives() {
        assertEquals(listOf("huis"), ids("huizen"))
        assertEquals(listOf("huis"), ids("huisje"))
        assertEquals(listOf("auto"), ids("auto's"))
        assertEquals(listOf("man"), ids("mannen")) // pl listed
    }

    @Test fun adjectiveEndingsFollowSpellingRules() {
        assertEquals(listOf("groot"), ids("grote"))
        assertEquals(listOf("wit"), ids("witte"))
        assertEquals(listOf("lief"), ids("lieve"))
        assertEquals(listOf("groot"), ids("groter"))
    }

    @Test fun separableVerbFirstWordFindsBothVerbs() {
        assertEquals(setOf("bellen", "opbellen"), ids("belt").toSet())
        assertEquals(setOf("bellen", "opbellen"), ids("belde").toSet())
    }

    @Test fun reflexiveVerbIsFoundWithoutZich() {
        assertEquals(listOf("wassen"), ids("wassen"))
        assertTrue(ids("zich").isEmpty())
    }

    @Test fun capitalsPunctuationAndCurlyApostrophes() {
        assertEquals(listOf("huis"), ids("Huizen"))
        assertEquals(listOf("huis"), ids("huizen,"))
        assertEquals(listOf("auto"), ids("auto’s"))
    }

    @Test fun phraseIsFoundByEachWordAndListedAfterRealWords() {
        assertEquals(listOf("tot", "ziens"), ids("tot"))
        assertEquals(listOf("ziens"), ids("ziens"))
    }

    @Test fun unknownAndEmptyGiveNothing() {
        assertTrue(ids("zonnebloem").isEmpty())
        assertTrue(ids("").isEmpty())
        assertTrue(ids("...").isEmpty())
    }

    @Test fun wordRangesKeepApostrophesAndHyphens() {
        val text = "Het is 10.30 uur; auto's en zo-even!"
        val words = wordRanges(text).map { text.substring(it.first, it.last + 1) }
        assertEquals(listOf("Het", "is", "10", "30", "uur", "auto's", "en", "zo-even"), words)
    }

    @Test fun sentenceRangesSplitOnEndMarksAndLineBreaksButNotInsideNumbers() {
        val text = "Hallo! Het kost 2.50 euro. Dank u.\nBeste Jan,\nTot ziens"
        val sentences = sentenceRanges(text).map { text.substring(it.first, it.last + 1) }
        assertEquals(listOf("Hallo!", "Het kost 2.50 euro.", "Dank u.", "Beste Jan,", "Tot ziens"), sentences)
    }
}
