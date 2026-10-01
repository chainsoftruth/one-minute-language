package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Reads the shipped course files from disk with the real (strict) parser. */
class CourseContentTest {
    private val root = File("src/main/assets/courses")
    private val unitIdFormat = Regex("^(a1|a2|b1)[.](g[0-9]{2}|t[0-9]{2}|cp|exam)$")

    private fun courseDirs() = root.listFiles { f -> f.isDirectory }.orEmpty().toList()

    private fun units(courseId: String): List<CourseUnit> =
        File(root, "$courseId/units").listFiles { f -> f.extension == "json" }.orEmpty()
            .sortedBy { it.name }
            .map { parseUnit(it.readText(), it.path) }

    private fun words(s: String) = s.lowercase().split(' ').map { w -> w.filter { it.isLetterOrDigit() || it == '\'' } }.filter { it.isNotEmpty() }

    @Test fun indexListsEveryCourseFolder() {
        val courses = parseCourses(File(root, "index.json").readText())
        assertEquals(courses.map { it.id }.sorted(), courseDirs().map { it.name }.sorted())
        assertTrue(courses.all { it.levels.isNotEmpty() })
    }

    @Test fun everyUnitFileIsListedInTheOutlineAndNamedLikeItsId() {
        for (course in courseDirs()) {
            val outline = parseOutline(File(course, "course.json").readText())
            val listed = outline.levels.flatMap { it.units }
            assertEquals("Duplicate unit ids in ${course.name}/course.json", listed.size, listed.toSet().size)
            assertTrue("Unit ids must match the format", listed.all { unitIdFormat.matches(it) })
            assertEquals(outline.topics.size, outline.topics.map { it.id }.toSet().size)
            for (file in File(course, "units").listFiles().orEmpty()) {
                val unit = parseUnit(file.readText(), file.path)
                assertEquals("${file.name}: id must match the file name", file.nameWithoutExtension, unit.id)
                assertTrue("${unit.id} is not listed in course.json", unit.id in listed)
            }
        }
    }

    @Test fun idsAreUniqueAndLessonsStartWithTheirUnitId() {
        for (course in courseDirs()) {
            val lessonIds = mutableSetOf<String>()
            for (unit in units(course.name)) {
                for (lesson in unit.lessons) {
                    assertTrue("${lesson.id} must start with ${unit.id}.", lesson.id.startsWith(unit.id + ".l"))
                    assertEquals(unit.id, unitIdOf(lesson.id))
                    assertTrue("Duplicate lesson id ${lesson.id}", lessonIds.add(lesson.id))
                }
            }
        }
    }

    @Test fun itemsAreWellFormed() {
        for (course in courseDirs()) for (unit in units(course.name)) for (lesson in unit.lessons) {
            lesson.items.forEachIndexed { i, item ->
                val where = "${lesson.id} item ${i + 1}"
                when (item) {
                    is Item.Choice -> {
                        assertTrue("$where: 2-5 options", item.options.size in 2..5)
                        assertEquals("$where: distinct options", item.options.size, item.options.toSet().size)
                        assertTrue("$where: answer in range", item.answer in item.options.indices)
                    }
                    is Item.Gap -> {
                        assertEquals("$where: exactly one ___", 1, Regex("___").findAll(item.text).count())
                        assertTrue("$where: needs an answer", item.answers.isNotEmpty())
                    }
                    is Item.Order -> {
                        assertTrue("$where: at least 3 tiles", words(item.answer).size >= 3)
                        val expected = words(item.answer).sorted()
                        item.alt.forEach { assertEquals("$where: alt '$it' uses other words", expected, words(it).sorted()) }
                    }
                    is Item.Transform -> assertTrue("$where: needs an answer", item.answers.isNotEmpty())
                    is Item.Translate -> assertTrue("$where: needs an answer", item.answers.isNotEmpty())
                    is Item.Match -> {
                        assertTrue("$where: 3-6 pairs", item.pairs.size in 3..6)
                        assertTrue("$where: every pair has 2 strings", item.pairs.all { it.size == 2 })
                        assertEquals("$where: duplicate left side", item.pairs.size, item.pairs.map { it[0] }.toSet().size)
                        assertEquals("$where: duplicate right side", item.pairs.size, item.pairs.map { it[1] }.toSet().size)
                    }
                    is Item.Listen ->
                        assertTrue("$where: dictation or a valid answer", item.options.isEmpty() || item.answer in item.options.indices)
                    is Item.Write -> {
                        assertTrue("$where: minWords < maxWords", item.minWords < item.maxWords)
                        assertTrue("$where: model answer length", words(item.model).size in item.minWords..item.maxWords)
                    }
                    is Item.Speak, is Item.OpenPrompt -> Unit
                }
            }
        }
    }

    @Test fun dialoguesAndVocabListsAreWellFormed() {
        for (course in courseDirs()) {
            val lexIds = File(course, "lexicon").listFiles { f -> f.extension == "json" }.orEmpty()
                .flatMap { parseLexicon(it.readText(), it.path) }.map { it.id }.toSet()
            for (unit in units(course.name)) for (lesson in unit.lessons) {
                if (lesson.dialogue.isNotEmpty()) {
                    assertTrue("${lesson.id}: 6-14 dialogue lines", lesson.dialogue.size in 6..14)
                    assertTrue("${lesson.id}: speakers are A or B", lesson.dialogue.all { it.who == "A" || it.who == "B" })
                    assertTrue("${lesson.id}: both speakers talk", lesson.dialogue.map { it.who }.toSet().size == 2)
                    assertTrue("${lesson.id}: only listening and speaking lessons have a dialogue", lesson.kind == LessonKind.LISTENING || lesson.kind == LessonKind.SPEAKING)
                }
                if (lesson.kind == LessonKind.VOCAB) {
                    assertTrue("${lesson.id}: 15-30 words", lesson.vocab.size in 15..30)
                    assertTrue("${lesson.id}: unknown lexicon ids ${lesson.vocab.filter { it !in lexIds }}", lesson.vocab.all { it in lexIds })
                }
            }
        }
    }

    @Test fun readingTextsBelongToReadingLessons() {
        for (course in courseDirs()) for (unit in units(course.name)) for (lesson in unit.lessons) {
            // The exam's reading parts are test lessons with a text (no feedback until the end).
            assertTrue("${lesson.id}: only reading lessons and tests have a text, and every reading lesson has it",
                if (lesson.kind == LessonKind.READING) lesson.text != null else lesson.kind == LessonKind.TEST || lesson.text == null)
            lesson.text?.let { assertTrue("${lesson.id}: reading text of 40+ words", words(it).size >= 40) }
        }
    }

    @Test fun resourcesAreHttpsWithoutDuplicatesAndKnownSkills() {
        for (course in courseDirs()) {
            val resources = parseResources(File(course, "resources.json").readText(), "${course.name}/resources.json")
            val skills = RESOURCE_SKILLS.map { it.first }.toSet()
            assertTrue("${course.name}: resource URLs use https", resources.all { it.url.startsWith("https://") })
            assertEquals("${course.name}: duplicate resource URLs", resources.size, resources.map { it.url }.toSet().size)
            assertTrue("${course.name}: unknown skill", resources.all { it.skill in skills })
            assertTrue("${course.name}: lang is nl or en", resources.all { it.lang == "nl" || it.lang == "en" })
            assertTrue("${course.name}: verified is curl or browser", resources.all { it.verified == "curl" || it.verified == "browser" })
            assertTrue("${course.name}: levels are A1, A2 or B1", resources.all { r -> r.levels.all { it == "A1" || it == "A2" || it == "B1" } })
        }
    }

    @Test fun b1GrammarUnitsAreDenseEnough() {
        for (unit in units("nl").filter { it.level == "B1" && it.kind == UnitKind.GRAMMAR }) {
            val items = unit.lessons.flatMap { it.items }
            val production = items.count { it is Item.Gap || it is Item.Order || it is Item.Transform || it is Item.Translate }
            assertTrue("${unit.id}: at least 3 lessons plus practice", unit.lessons.size >= 4)
            assertTrue("${unit.id}: at least 60 items, has ${items.size}", items.size >= 60)
            assertTrue("${unit.id}: at least 40% production items, has $production of ${items.size}", production * 10 >= items.size * 4)
        }
    }

    /** Stage 12: every B1 theme unit has the full skill set, and the exam follows the plan's format. */
    @Test fun b1ThemeUnitsAndTheExamHaveTheirParts() {
        val b1 = units("nl").filter { it.level == "B1" }
        val themes = b1.filter { it.kind == UnitKind.THEME }
        assertEquals(listOf("b1.t01", "b1.t02", "b1.t03", "b1.t04", "b1.t05", "b1.t06", "b1.t07"), themes.map { it.id })
        for (unit in themes) {
            val kinds = unit.lessons.map { it.kind }
            assertTrue("${unit.id}: at least one vocab lesson", LessonKind.VOCAB in kinds)
            assertEquals("${unit.id}: two readings", 2, kinds.count { it == LessonKind.READING })
            assertEquals("${unit.id}: two dialogues (listening + roleplay)", 2, unit.lessons.count { it.dialogue.isNotEmpty() })
            assertTrue("${unit.id}: a speaking lesson with 2-3 open prompts", unit.lessons.first { it.kind == LessonKind.SPEAKING }.items.count { it is Item.OpenPrompt } in 2..3)
            assertTrue("${unit.id}: writing", unit.lessons.any { it.kind == LessonKind.WRITING && it.items.count { i -> i is Item.Write } in 1..2 })
            unit.lessons.filter { it.kind == LessonKind.READING }.forEach {
                assertTrue("${it.id}: B1 readings have 200-350 words", words(it.text!!).size in 200..350)
            }
            assertTrue("${unit.id}: links a resource", unit.lessons.any { it.sources.isNotEmpty() })
        }
        val exam = b1.first { it.id == "b1.exam" }
        val tests = exam.lessons.filter { it.kind == LessonKind.TEST }
        assertEquals("five reading tests with a text and 3-4 questions", 5, tests.count { it.text != null && it.items.size in 3..4 })
        assertTrue("6 listening fragments with 2-3 questions each", tests.flatMap { it.items }.filterIsInstance<Item.Listen>().groupBy { it.nl }.let { it.size == 6 && it.values.all { q -> q.size in 2..3 } })
        assertEquals("three writing tasks", 3, tests.flatMap { it.items }.count { it is Item.Write })
        assertEquals("six speaking tasks", 6, tests.flatMap { it.items }.count { it is Item.OpenPrompt })
        val last = exam.lessons.last()
        assertTrue("the last lesson only links the official practice exams", last.items.isEmpty() && last.sources.size >= 3)
    }

    /** In b1.g04 a participle + auxiliary pair at the end of a clause can be written in two orders, so the item must list both. */
    @Test fun clusterOrderItemsListBothOrders() {
        val aux = setOf("heeft", "heb", "hebt", "hebben", "had", "hadden", "is", "ben", "bent", "zijn", "was", "waren")
        val participle = Regex("^(ge\\w{3,}|\\w+ge\\w+(d|t|en)|(be|ver|ont|her|er)\\w+(d|t|en))$")
        val unit = units("nl").firstOrNull { it.id == "b1.g04" } ?: return
        for (lesson in unit.lessons) lesson.items.filterIsInstance<Item.Order>().forEach { item ->
            for (clause in item.answer.lowercase().split(',')) {
                val w = words(clause)
                if (w.size < 3) continue
                val (a, b) = w[w.size - 2] to w.last()
                val auxFirst = a in aux && participle.matches(b)
                val auxLast = b in aux && participle.matches(a)
                val auxIndex = if (auxFirst) w.size - 2 else w.size - 1
                if ((auxFirst || auxLast) && auxIndex >= 2) {
                    assertTrue("${lesson.id}: '${item.answer}' needs the other cluster order in alt", item.alt.isNotEmpty())
                }
            }
        }
    }

    @Test fun grammarLessonsFollowTheAuthoringStandard() {
        for (course in courseDirs()) for (unit in units(course.name)) {
            for (lesson in unit.lessons) {
                lesson.sources.forEach { assertTrue("${lesson.id}: source URLs use https", it.url.startsWith("https://")) }
                if (unit.kind != UnitKind.GRAMMAR || lesson.kind != LessonKind.GRAMMAR) continue
                assertTrue("${lesson.id}: grammar lessons need a source", lesson.sources.isNotEmpty())
                if (lesson.explain.isEmpty()) continue
                assertTrue("${lesson.id}: at least 2 example blocks", lesson.explain.count { it is Block.Example } >= 2)
                assertTrue("${lesson.id}: at least 3 item types", lesson.items.map { it::class }.toSet().size >= 3)
                val production = lesson.items.count { it is Item.Gap || it is Item.Order || it is Item.Transform || it is Item.Translate }
                assertTrue("${lesson.id}: at least 30% production items", production * 10 >= lesson.items.size * 3)
                lesson.explain.filterIsInstance<Block.Paragraph>().forEach {
                    assertTrue("${lesson.id}: paragraph over 120 words", it.text.split(' ').size <= 120)
                }
            }
        }
    }
}
