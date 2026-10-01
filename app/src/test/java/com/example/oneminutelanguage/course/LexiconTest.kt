package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.io.File

/** Reads every lexicon file from disk with the real (strict) parser and checks the authoring rules (plan §7, 3.2). */
class LexiconTest {
    private val root = File("src/main/assets/courses")
    private val validPos = setOf("noun", "verb", "adj", "adv", "prep", "conj", "pron", "num", "det", "phrase", "intj")
    private val levels = listOf("A1", "A2", "B1")

    private fun lexiconFiles(courseId: String) =
        File(root, "$courseId/lexicon").listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }

    private fun entries(courseId: String): List<LexEntry> =
        lexiconFiles(courseId).flatMap { parseLexicon(it.readText(), it.path) }

    private fun courseIds() = root.listFiles { f -> f.isDirectory }.orEmpty().map { it.name }

    @Test fun everyFileParsesAndIsNamedAfterItsLevelAndTopic() {
        for (course in courseIds()) for (file in lexiconFiles(course)) {
            val list = parseLexicon(file.readText(), file.path)
            assertTrue("${file.name} is empty", list.isNotEmpty())
            list.forEach { assertEquals("${it.id}: lives in the wrong file", "${it.lvl.lowercase()}_${it.topic}.json", file.name) }
        }
    }

    @Test fun idsAreUniqueAndTidy() {
        for (course in courseIds()) {
            val all = entries(course)
            assertEquals("Duplicate lexicon ids", all.size, all.map { it.id }.toSet().size)
            all.forEach { assertTrue("${it.id}: id must be lower case with _ for spaces", Regex("^[a-z0-9_à-ÿ]+$").matches(it.id)) }
        }
    }

    @Test fun posLevelAndTopicAreValid() {
        for (course in courseIds()) {
            val topics = parseOutline(File(root, "$course/course.json").readText()).topics.map { it.id }.toSet()
            entries(course).forEach {
                assertTrue("${it.id}: pos '${it.pos}'", it.pos in validPos)
                assertTrue("${it.id}: lvl '${it.lvl}'", it.lvl in levels)
                assertTrue("${it.id}: topic '${it.topic}' is not declared in course.json", it.topic in topics)
            }
        }
    }

    @Test fun nounsHaveArticleAndPlural() {
        for (course in courseIds()) entries(course).filter { it.pos == "noun" }.forEach {
            assertTrue("${it.id}: art must be de, het, de/het or - (proper noun)", it.art in setOf("de", "het", "de/het", "-"))
            assertTrue("${it.id}: needs a plural (or - for uncountables)", !it.pl.isNullOrBlank())
        }
    }

    @Test fun verbsHaveAllFormsAndFlagsAgree() {
        for (course in courseIds()) entries(course).forEach {
            if (it.pos == "verb") {
                assertTrue("${it.id}: verbs need pres, past, pp and aux", listOf(it.pres, it.past, it.pp, it.aux).all { f -> !f.isNullOrBlank() })
                assertTrue("${it.id}: needs an example", !it.ex.isNullOrBlank())
            }
            if (it.sep) assertTrue("${it.id}: a separable verb's pres needs a space (belt op)", it.pres?.contains(' ') == true)
            if (it.refl) assertTrue("${it.id}: a reflexive verb starts with 'zich '", it.nl.startsWith("zich "))
        }
    }

    // Deviation from the plan (one pair per nl + pos): homographs such as bank (sofa) and bank (money) share both,
    // so the English meaning is part of the key. The ids still differ (bank, bank_2).
    @Test fun noDuplicateWords() {
        for (course in courseIds()) {
            val all = entries(course)
            assertEquals("Duplicate (nl, pos, en)", all.size, all.map { Triple(it.nl, it.pos, it.en) }.toSet().size)
        }
    }

    @Test fun examplesComeInPairsAndMostWordsHaveOne() {
        for (course in courseIds()) {
            val all = entries(course)
            all.forEach { assertEquals("${it.id}: ex and exEn go together", it.ex == null, it.exEn == null) }
            assertTrue("At least half of the words need an example", all.count { it.ex != null } * 2 >= all.size)
        }
    }

    @Test fun lessonsOnlyReferenceExistingWords() {
        for (course in courseIds()) {
            val ids = entries(course).map { it.id }.toSet()
            File(root, "$course/units").listFiles().orEmpty().map { parseUnit(it.readText(), it.path) }.forEach { unit ->
                unit.lessons.forEach { l ->
                    l.vocab.forEach { assertTrue("${l.id}: unknown lexicon id '$it'", it in ids) }
                    if (l.kind == LessonKind.VOCAB) assertTrue("${l.id}: a vocab lesson needs 15-30 words", l.vocab.size in 15..30)
                }
            }
        }
    }

    private fun cumulative(level: String): Int {
        val upTo = levels.take(levels.indexOf(level) + 1)
        return courseIds().sumOf { c -> entries(c).count { it.lvl in upTo } }
    }

    @Test fun a1HasAtLeast750Words() = assertTrue("A1 has ${cumulative("A1")}", cumulative("A1") >= 750)

    @Test fun a2HasAtLeast1650WordsCumulative() = assertTrue("A2 has ${cumulative("A2")}", cumulative("A2") >= 1650)

    @Test fun a2LexiconCoversTheStageSevenTargets() {
        val a2 = courseIds().flatMap { entries(it) }.filter { it.lvl == "A2" }
        val verbs = a2.filter { it.pos == "verb" }
        assertTrue("A2 entries: ${a2.size}", a2.size >= 900)
        assertTrue("A2 verbs: ${verbs.size}", verbs.size >= 200)
        assertTrue("A2 separable verbs: ${verbs.count { it.sep }}", verbs.count { it.sep } >= 60)
        assertTrue("A2 reflexive verbs: ${verbs.count { it.refl }}", verbs.count { it.refl } >= 10)
        assertTrue("A2 verbs with a fixed preposition: ${verbs.count { it.prep != null }}", verbs.count { it.prep != null } >= 20)
        assertTrue("Every A2 verb needs an example", verbs.all { !it.ex.isNullOrBlank() })
        assertTrue("A2 examples: ${a2.count { it.ex != null }}", a2.count { it.ex != null } * 10 >= a2.size * 6)
    }

    @Ignore("Enabled in Stage 9") @Test fun b1HasAtLeast2800WordsCumulative() = assertTrue(cumulative("B1") >= 2800)
}
