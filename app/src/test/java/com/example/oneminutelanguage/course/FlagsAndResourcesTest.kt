package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FlagsAndResourcesTest {
    @Test fun aFlagLineHasFourFieldsEvenWhenTheTextHasPipesAndNewlines() {
        val line = flagLine("a1.g04.l2", 3, FlagType.WRONG_DUTCH, "het | de\nstoel  is wrong ")
        assertEquals("a1.g04.l2|3|wrong_dutch|het / de stoel is wrong", line)
        assertEquals(4, line.split('|').size)
    }

    @Test fun anEmptyTextStillGivesAFullLine() {
        assertEquals("a1.t01.l2|-1|unclear|", flagLine("a1.t01.l2", -1, FlagType.UNCLEAR, ""))
    }

    @Test fun exportIsOneSortedLinePerFlag() {
        assertEquals("a|1|other|x\nb|0|audio|y", flagsExport(setOf("b|0|audio|y", "a|1|other|x")))
    }

    private val shipped = parseResources(File("src/main/assets/courses/nl/resources.json").readText())

    @Test fun levelFilterKeepsResourcesWithoutLevels() {
        val dictionaries = resourcesFor(shipped, "dictionary", "A1")
        assertTrue(dictionaries.isNotEmpty())
        assertTrue(resourcesFor(shipped, "grammar", "A1").all { "A1" in it.levels || it.levels.isEmpty() })
        assertTrue(resourcesFor(shipped, "exam", "A1").isEmpty())
    }
}
