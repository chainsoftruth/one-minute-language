package com.example.oneminutelanguage.course

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every reading text and dialogue must be mostly made of words the course dictionary can explain (tap-to-translate
 * would otherwise say "Not in the course dictionary" all the time). Names and numbers don't count. The report per
 * lesson goes to the test output and to `build/reports/lexicon-coverage.txt`.
 */
class LexiconCoverageTest {
    private val root = File("src/main/assets/courses")
    private val minimum = 0.90

    private class Coverage(val lessonId: String, val total: Int, val unknown: List<String>) {
        val ratio get() = if (total == 0) 1.0 else (total - unknown.size).toDouble() / total
    }

    /** A capitalised word inside a sentence is a name; at the start of a sentence it is checked like any other word. */
    private fun coverage(lessonId: String, text: String, lookup: WordLookup): Coverage {
        val sentenceStarts = sentenceRanges(text).map { it.first }.toSet()
        var total = 0
        val unknown = mutableListOf<String>()
        for (range in wordRanges(text)) {
            val word = text.substring(range.first, range.last + 1)
            if (word.first().isDigit()) continue
            val startsSentence = sentenceStarts.any { s -> s <= range.first && text.substring(s, range.first).isBlank() }
            if (word.first().isUpperCase() && !startsSentence) continue
            total++
            if (lookup.lookup(word).isEmpty()) unknown += word
        }
        return Coverage(lessonId, total, unknown)
    }

    @Test fun readingTextsAndDialoguesAreCoveredByTheLexicon() {
        val report = StringBuilder()
        val failures = mutableListOf<String>()
        for (course in root.listFiles { f -> f.isDirectory }.orEmpty()) {
            val lexicon = File(course, "lexicon").listFiles { f -> f.extension == "json" }.orEmpty()
                .flatMap { parseLexicon(it.readText(), it.path) }
            val lookup = WordLookup(lexicon)
            val units = File(course, "units").listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }
                .map { parseUnit(it.readText(), it.path) }
            for (lesson in units.flatMap { it.lessons }) {
                val texts = buildList {
                    lesson.text?.let { add(lesson.id to it) }
                    if (lesson.dialogue.isNotEmpty()) add("${lesson.id} (dialogue)" to lesson.dialogue.joinToString("\n") { it.nl })
                }
                for ((id, text) in texts) {
                    val c = coverage(id, text, lookup)
                    report.appendLine("%-26s %3d words  %5.1f%%  unknown: %s".format(id, c.total, c.ratio * 100, c.unknown.joinToString(", ")))
                    if (c.ratio < minimum) failures += "$id: ${"%.0f".format(c.ratio * 100)}% covered, unknown: ${c.unknown}"
                }
            }
        }
        File("build/reports").apply { mkdirs() }.resolve("lexicon-coverage.txt").writeText(report.toString())
        println(report)
        assertTrue("Texts below ${(minimum * 100).toInt()}% coverage:\n" + failures.joinToString("\n"), failures.isEmpty())
    }
}
