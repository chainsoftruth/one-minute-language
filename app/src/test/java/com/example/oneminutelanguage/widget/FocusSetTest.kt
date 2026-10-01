package com.example.oneminutelanguage.widget

import com.example.oneminutelanguage.data.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusSetTest {
    private fun word(id: Long, isDefault: Boolean = true, isEnabled: Boolean = true, learnedAt: Long? = null) =
        WordEntity(
            id = id, language1Word = "w$id", language2Word = "w$id", dateAdded = 0,
            isDefault = isDefault, isEnabled = isEnabled, learnedAt = learnedAt
        )

    private val words = (1L..30L).map { word(it) }

    /** Simulates [count] screen-ons and returns the ids shown. */
    private fun rotate(words: List<WordEntity>, count: Int, start: Long = -1L): List<Long> {
        var last = start
        return List(count) { nextFocusWord(words, last)!!.id.also { last = it } }
    }

    @Test
    fun cyclesWithinTheFirst20Words() {
        assertEquals((1L..20L) + (1L..20L), rotate(words, 40))
    }

    @Test
    fun learnedWordIsReplacedByTheNextUnlearnedOne() {
        val learned = words.map { if (it.id == 5L) it.copy(learnedAt = 1) else it }
        val shown = rotate(learned, 20).toSet()
        assertEquals((1L..21L).toSet() - 5L, shown)
    }

    @Test
    fun disabledWordsAreSkipped() {
        val someDisabled = words.map { if (it.id <= 3) it.copy(isEnabled = false) else it }
        assertEquals((4L..23L).toList(), focusSet(someDisabled).map { it.id })
    }

    @Test
    fun usersOwnWordsComeFirst() {
        val withUserWord = words + word(99, isDefault = false)
        assertEquals(99L, focusSet(withUserWord).first().id)
        assertEquals(19L, focusSet(withUserWord).last().id)
    }

    @Test
    fun continuesAfterALearnedLastWord() {
        // The widget showed word 7, then the user learned it: carry on with 8, not back to 1.
        val learned = words.map { if (it.id == 7L) it.copy(learnedAt = 1) else it }
        assertEquals(8L, nextFocusWord(learned, 7)!!.id)
    }

    @Test
    fun deletedLastWordRestartsAtTheTop() {
        assertEquals(1L, nextFocusWord(words, 999)!!.id)
    }

    @Test
    fun everythingLearnedFallsBackToRandom() {
        assertNull(nextFocusWord(words.map { it.copy(learnedAt = 1) }, 1))
    }
}
