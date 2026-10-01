package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Stage 13 audit: everything §6 of the plan promises exists. */
class CurriculumCoverageTest {
    private val root = File("src/main/assets/courses/nl")
    private val units = File(root, "units").listFiles { f -> f.extension == "json" }.orEmpty()
        .sortedBy { it.name }.map { parseUnit(it.readText(), it.path) }.associateBy { it.id }

    private fun ids(prefix: String, kind: Char, count: Int) = (1..count).map { "$prefix.$kind${"%02d".format(it)}" }

    private val planned = ids("a1", 'g', 11) + ids("a1", 't', 4) + "a1.cp" +
        ids("a2", 'g', 12) + ids("a2", 't', 5) + "a2.cp" +
        ids("b1", 'g', 14) + ids("b1", 't', 7) + "b1.exam"

    @Test fun everyPlannedUnitExistsAndNothingElse() {
        assertEquals(planned.sorted(), units.keys.sorted())
        val listed = parseOutline(File(root, "course.json").readText()).levels.flatMap { it.units }
        assertEquals(planned.sorted(), listed.sorted())
    }

    @Test fun everyGrammarUnitHasAtLeastFortyItems() {
        for (unit in units.values.filter { it.kind == UnitKind.GRAMMAR }) {
            val n = unit.lessons.sumOf { it.items.size }
            assertTrue("${unit.id} has only $n items", n >= 40)
        }
    }

    @Test fun everyThemeUnitHasAllFiveLessonKinds() {
        val needed = setOf(LessonKind.VOCAB, LessonKind.READING, LessonKind.LISTENING, LessonKind.SPEAKING, LessonKind.WRITING)
        for (unit in units.values.filter { it.kind == UnitKind.THEME }) {
            val missing = needed - unit.lessons.map { it.kind }.toSet()
            assertTrue("${unit.id} lacks $missing", missing.isEmpty())
        }
    }

    @Test fun lexiconHasAtLeast2800Words() {
        val n = File(root, "lexicon").listFiles { f -> f.extension == "json" }.orEmpty().sumOf { parseLexicon(it.readText(), it.path).size }
        assertTrue("lexicon has only $n entries", n >= 2800)
    }

    @Test fun canDoListHasFourSkillsOfFiveToSixStatementsOverExistingUnits() {
        assertEquals(CAN_DO.size, CAN_DO.map { it.id }.toSet().size)
        for (skill in CAN_DO_SKILLS) assertTrue("$skill needs 5-6 statements", CAN_DO.count { it.skill == skill } in 5..6)
        assertTrue(CAN_DO.all { it.skill in CAN_DO_SKILLS })
        for (item in CAN_DO) {
            assertTrue("${item.id} needs a unit", item.units.isNotEmpty())
            assertTrue("${item.id}: unknown unit in ${item.units}", item.units.all { it in units })
        }
    }
}
