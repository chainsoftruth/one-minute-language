package com.example.oneminutelanguage

import com.example.oneminutelanguage.translation.SupportedLanguages
import com.example.oneminutelanguage.translation.withoutHints
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WordAssetsTest {
    private val json = File("src/main/assets/words.json").readText()
    private val languages = SupportedLanguages.all.map { it.code }

    // org.json is only a stub on the JVM. The file is a flat array of {"code": "word"} objects
    // without escapes (asserted below), so two regexes are enough.
    private val entries: List<Map<String, String>> = Regex("""\{[^{}]*\}""").findAll(json).map { entry ->
        Regex(""""([^"]*)"\s*:\s*"([^"]*)"""").findAll(entry.value)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }.toList()

    @Test
    fun fileHasNoEscapes() {
        assertFalse(json.contains('\\'))
    }

    @Test
    fun everyEntryHasEnglishAndDutch() {
        assertTrue(entries.size > 600)
        val incomplete = entries.filter { "en" !in it || "nl" !in it }
        assertTrue("Entries without en or nl: $incomplete", incomplete.isEmpty())
    }

    @Test
    fun noEmptyValues() {
        val blank = entries.filter { entry -> entry.values.any { it.isBlank() } }
        assertTrue("Entries with empty values: $blank", blank.isEmpty())
    }

    @Test
    fun onlySupportedLanguages() {
        val unknown = entries.flatMap { it.keys }.toSet() - languages.toSet()
        assertTrue("Unknown language keys: $unknown", unknown.isEmpty())
    }

    @Test
    fun noDuplicatesAfterStrippingHints() {
        for (language in languages) {
            val duplicates = entries.mapNotNull { it[language] }
                .groupBy { it.withoutHints().lowercase() }
                .filterValues { it.size > 1 }
                .keys
            assertTrue("$language has duplicates: $duplicates", duplicates.isEmpty())
        }
    }
}
