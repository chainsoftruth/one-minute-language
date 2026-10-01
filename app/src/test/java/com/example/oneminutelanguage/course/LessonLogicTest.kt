package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LessonLogicTest {
    private val choice = Item.Choice("___ fiets", listOf("de", "het"), 0)
    private val gap = Item.Gap("Ik zie ___ meisje.", listOf("het"))
    private val order = Item.Order("De kinderen spelen in het park.", alt = listOf("In het park spelen de kinderen."), extra = listOf("een"))
    private val translate = Item.Translate("I have a dog", listOf("Ik heb een hond"))

    @Test fun choiceIsGradedByIndex() {
        assertTrue(gradeChoice(choice, 0).correct)
        val wrong = gradeChoice(choice, 1)
        assertFalse(wrong.correct)
        assertEquals("de", wrong.expected)
    }

    @Test fun gapIsStrictAndShowsTheFilledSentence() {
        assertTrue(gradeText(gap, " Het ").correct)
        val wrong = gradeText(gap, "de")
        assertFalse(wrong.correct)
        assertEquals("Ik zie het meisje.", wrong.expected)
    }

    @Test fun orderAcceptsTheAlternativeOrder() {
        assertTrue(gradeText(order, "in het park spelen de kinderen").correct)
        assertFalse(gradeText(order, "de kinderen in het park spelen").correct)
    }

    @Test fun translateForgivesATypoAndExplainsIt() {
        val result = gradeText(translate, "ik heb een hnd")
        assertTrue(result.correct)
        assertEquals(Verdict.ALMOST, result.verdict)
        assertEquals("Almost: Ik heb een hond", result.note)
    }

    @Test fun matchAllowsOneMistake() {
        val match = Item.Match(listOf(listOf("a", "b"), listOf("c", "d"), listOf("e", "f")))
        assertTrue(gradeMatch(match, 1).correct)
        assertFalse(gradeMatch(match, 2).correct)
    }

    @Test fun orderTilesHaveTheWordsPlusExtrasAndNoFinalPunctuation() {
        val tiles = orderTiles(order, Random(1))
        assertEquals(listOf("de", "een", "het", "in", "kinderen", "park", "spelen"), tiles.sorted())
    }

    @Test fun wrongItemIsRequeuedOnceAndCountsOnce() {
        val q = LessonQueue(listOf(choice, gap))
        assertEquals(0, q.currentIndex)
        q.answer(false)                     // item 0 wrong -> goes to the end
        assertEquals(1, q.currentIndex)
        q.answer(true)
        assertEquals(0, q.currentIndex)
        q.answer(false)                     // wrong again: not queued a second time
        assertTrue(q.isDone)
        assertEquals(1, q.mistakes)
        assertEquals(50, q.score)
        assertEquals(3, q.progressDone)
    }

    @Test fun checkpointsGiveNoSecondChance() {
        val q = LessonQueue(listOf(choice, gap), requeue = false)
        q.answer(false)
        q.answer(true)
        assertTrue(q.isDone)
    }

    @Test fun wrongItemsOfATestAreListedWithTheirRightAnswer() {
        val q = LessonQueue(listOf(choice, gap), requeue = false)
        q.answer(true)
        q.answer(false)
        assertEquals(listOf(itemPrompt(gap) to expectedAnswer(gap)), q.wrongItems.map { itemPrompt(it) to expectedAnswer(it) })
        assertEquals("Wat is dit?", itemPrompt(Item.Choice("Wat is dit?", listOf("a", "b"), 0)))
    }

    @Test fun skippedItemsDoNotCount() {
        val q = LessonQueue(listOf(choice, gap))
        q.skip()
        q.answer(true)
        assertEquals(100, q.score)
    }

    @Test fun pathHelpersFollowCourseOrder() {
        fun lesson(id: String) = Lesson(id, id, LessonKind.GRAMMAR)
        fun unit(id: String, vararg lessons: String) = CourseUnit(id, "A1", UnitKind.GRAMMAR, id, lessons = lessons.map(::lesson))
        val path = listOf(
            PathLevel(
                LevelOutline("A1", "Foundations", listOf("u1", "u2", "u3")),
                listOf(PathUnit("u1", unit("u1", "u1.l1", "u1.l2")), PathUnit("u2", null), PathUnit("u3", unit("u3", "u3.l1")))
            )
        )
        assertEquals("u1.l2", firstUnfinished(path, setOf("u1.l1"))?.second?.id)
        assertEquals("u3.l1", firstUnfinished(path, setOf("u1.l1", "u1.l2"))?.second?.id)
        assertNull(firstUnfinished(path, setOf("u1.l1", "u1.l2", "u3.l1")))
        assertEquals("u3.l1", lessonAfter(path, "u1.l2")?.id)
        assertNull(lessonAfter(path, "u3.l1"))
        assertEquals("u1.g04", unitIdOf("u1.g04.l2"))
    }
}
