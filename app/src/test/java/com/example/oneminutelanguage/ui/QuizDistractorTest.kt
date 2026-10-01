package com.example.oneminutelanguage.ui

import com.example.oneminutelanguage.data.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class QuizDistractorTest {
    private fun word(id: Long, native: String, learning: String) =
        WordEntity(id = id, language1Word = native, language2Word = learning, dateAdded = 0)

    private val big = word(1, "Big", "Groot")
    private val pool = listOf(
        big,
        word(2, "Large", "Groot"),
        word(3, "Small", "Klein"),
        word(4, "Red", "Rood"),
        word(5, "Blue", "Blauw"),
        word(6, "The tyre", "De band (wiel)")
    )

    @Test
    fun synonymIsNeverADistractor() {
        repeat(100) {
            val question = buildQuestion(big, pool)
            assertFalse(question.options.toString(), "Large" in question.options)
            assertEquals("Big", question.options[question.correctIndex])
        }
    }

    @Test
    fun promptHasNoHints() {
        val tyre = pool.last()
        assertEquals("De band", buildQuestion(tyre, pool).prompt)
    }

    @Test
    fun articleQuestionHidesArticleAndHints() {
        val question = buildArticleQuestion(pool.last())
        assertEquals("band", question.prompt)
        assertEquals("de", question.options[question.correctIndex])

        val hospital = buildArticleQuestion(word(7, "The hospital", "Het ziekenhuis"))
        assertEquals("ziekenhuis", hospital.prompt)
        assertEquals("het", hospital.options[hospital.correctIndex])
    }

    @Test
    fun onlyDeAndHetCount() {
        assertEquals("het", articleOf("HET huis"))
        assertNull(articleOf("Groot"))
        assertNull(articleOf("Het"))
        assertNull(articleOf("Deur"))
    }
}
