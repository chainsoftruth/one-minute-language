package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class PlacementTest {
    private val units = File("src/main/assets/courses/nl/units").listFiles { f -> f.extension == "json" }.orEmpty()
        .map { parseUnit(it.readText(), it.path) }

    @Test fun thirtyItemsTenPerLevelEasiestFirstAndOnlyChoiceGapOrder() {
        val test = placementTest(units, Random(1))
        assertEquals(30, test.size)
        assertEquals(listOf("A1", "A2", "B1"), test.map { it.level }.distinct())
        assertTrue(test.groupBy { it.level }.values.all { it.size == PLACEMENT_PER_LEVEL })
        assertTrue(test.all { it.item is Item.Choice || it.item is Item.Gap || it.item is Item.Order })
    }

    @Test fun scoresCountTheRightAnswersPerLevel() {
        val test = placementTest(units, Random(2))
        val wrong = test.filter { it.level == "A2" }.take(4).map { it.item } + test.first { it.level == "A1" }.item
        assertEquals(mapOf("A1" to 9, "A2" to 6, "B1" to 10), placementScores(test, wrong))
    }

    @Test fun startLevelFollowsTheSevenOutOfTenRule() {
        assertEquals("A1", placementStart(mapOf("A1" to 6, "A2" to 10, "B1" to 10)))
        assertEquals("A2", placementStart(mapOf("A1" to 7, "A2" to 6, "B1" to 10)))
        assertEquals("B1", placementStart(mapOf("A1" to 7, "A2" to 7, "B1" to 0)))
    }

    @Test fun lessonsBeforeALevelAreEveryLessonOfTheEarlierLevels() {
        fun unit(id: String, vararg lessons: String) =
            PathUnit(id, CourseUnit(id, "A1", UnitKind.GRAMMAR, id, lessons = lessons.map { Lesson(it, it, LessonKind.GRAMMAR) }))
        val path = listOf(
            PathLevel(LevelOutline("A1", "", listOf("a1.g01")), listOf(unit("a1.g01", "a1.g01.l1", "a1.g01.l2"), PathUnit("a1.g02", null))),
            PathLevel(LevelOutline("A2", "", listOf("a2.g01")), listOf(unit("a2.g01", "a2.g01.l1"))),
            PathLevel(LevelOutline("B1", "", listOf("b1.g01")), listOf(unit("b1.g01", "b1.g01.l1")))
        )
        assertEquals(emptyList<String>(), lessonsBefore(path, "A1"))
        assertEquals(listOf("a1.g01.l1", "a1.g01.l2"), lessonsBefore(path, "A2"))
        assertEquals(listOf("a1.g01.l1", "a1.g01.l2", "a2.g01.l1"), lessonsBefore(path, "B1"))
    }
}

class ProgressStatsTest {
    private fun lesson(id: String, kind: LessonKind) = Lesson(id, id, kind)
    private val unit = CourseUnit(
        "a1.t01", "A1", UnitKind.THEME, "t",
        lessons = listOf(lesson("a1.t01.l1", LessonKind.READING), lesson("a1.t01.l2", LessonKind.READING), lesson("a1.t01.l3", LessonKind.LISTENING), lesson("a1.t01.l4", LessonKind.TEST))
    )
    private val path = listOf(PathLevel(LevelOutline("A1", "", listOf("a1.t01", "a1.t02")), listOf(PathUnit("a1.t01", unit), PathUnit("a1.t02", null))))

    @Test fun skillMeterAveragesTheDoneLessonsOfThatKind() {
        val meters = skillMeters(path, mapOf("a1.t01.l1" to 80, "a1.t01.l2" to 91, "a1.t01.l4" to 100))
        assertEquals(86, meters[LessonKind.READING]) // 85.5 rounds up
        assertEquals(null, meters[LessonKind.LISTENING])
        assertEquals(SKILL_KINDS.toSet(), meters.keys) // checkpoint tests have no meter
    }

    @Test fun levelCountsIgnoreUnitsWithoutContent() {
        assertEquals(2 to 4, levelCounts(path.first(), setOf("a1.t01.l1", "a1.t01.l3", "other")))
    }
}
