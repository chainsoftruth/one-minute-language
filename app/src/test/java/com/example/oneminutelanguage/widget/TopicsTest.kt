package com.example.oneminutelanguage.widget

import com.example.oneminutelanguage.course.LexEntry
import com.example.oneminutelanguage.course.topicIndex
import com.example.oneminutelanguage.course.topicOf
import com.example.oneminutelanguage.data.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TopicsTest {
    private fun lex(nl: String, art: String?, topic: String, pos: String = "noun") =
        LexEntry(id = nl, nl = nl, en = nl, pos = pos, lvl = "A1", topic = topic, art = art)

    private val index = topicIndex(
        listOf(
            lex("fiets", "de", "travel"),
            lex("huis", "het", "home_housing"),
            lex("mail", "de/het", "daily_life"),
            lex("lopen", null, "daily_life", pos = "verb")
        )
    )

    @Test
    fun storedWordsFindTheirTopic() {
        assertEquals("travel", topicOf(index, "De fiets"))
        assertEquals("home_housing", topicOf(index, "Het huis"))
        assertEquals("daily_life", topicOf(index, "Lopen"))
    }

    @Test
    fun hintsAndDoubleArticlesAreHandled() {
        assertEquals("home_housing", topicOf(index, "Het huis (gebouw)"))
        assertEquals("daily_life", topicOf(index, "de mail"))
        assertEquals("daily_life", topicOf(index, "het mail"))
    }

    @Test
    fun unknownWordHasNoTopic() {
        assertNull(topicOf(index, "De fietsenmaker"))
    }

    private fun word(id: Long, topic: String?) =
        WordEntity(id = id, language1Word = "w$id", language2Word = "w$id", dateAdded = 0, topic = topic)

    private val enabled = listOf(word(1, "travel"), word(2, "travel"), word(3, "food_drink"), word(4, null))

    @Test
    fun poolIsNarrowedToTheTopic() {
        assertEquals(listOf(1L, 2L), widgetPool(enabled, "travel").map { it.id })
    }

    @Test
    fun noTopicMeansEveryEnabledWord() {
        assertEquals(listOf(1L, 2L, 3L, 4L), widgetPool(enabled, null).map { it.id })
    }

    @Test
    fun aTopicWithNothingSwitchedOnFallsBackToAllWords() {
        assertEquals(listOf(1L, 2L, 3L, 4L), widgetPool(enabled, "health_body").map { it.id })
    }

    @Test
    fun focusSetStaysInsideTheTopic() {
        val travel = (1L..60L).map { word(it, if (it % 2 == 0L) "travel" else "food_drink") }
        val pool = widgetPool(travel, "travel")
        assertEquals(20, focusSet(pool).size)
        assertEquals(true, focusSet(pool).all { it.topic == "travel" })
    }
}
