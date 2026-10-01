package com.example.oneminutelanguage.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FitTextTest {
    // Fake fixed-width font: every character is half an sp wide, a line is 1.2 sp high.
    private val measure = { text: String, sp: Int -> text.length * sp * 0.5f }
    private val lineHeight = { sp: Int -> sp * 1.2f }

    private fun fit(text: String, width: Float, height: Float = 1000f) =
        fitSp(text, width, height, lineHeightPx = lineHeight, measure = measure)

    @Test
    fun shortWordStaysAtMaxSize() {
        assertEquals(28, fit("huis", 200f))
    }

    @Test
    fun longWordShrinksUntilItFitsOnOneLine() {
        // 20 chars * 0.5 * sp <= 200 -> sp <= 20
        val sp = fit("verantwoordelijkheid", 200f)
        assertEquals(20, sp)
        assertTrue(measure("verantwoordelijkheid", sp) <= 200f)
    }

    @Test
    fun neverGoesBelowTheFloor() {
        // Would need 10sp to fit; the floor wins and the TextView wraps inside the word.
        assertEquals(14, fit("verantwoordelijkheid", 100f))
    }

    @Test
    fun aboveTheFloorNoWordBreaks() {
        val text = "het zelfstandig naamwoord"
        for (width in 60..400 step 10) {
            val sp = fit(text, width.toFloat())
            if (sp > 14) {
                assertTrue(text.split(' ').all { measure(it, sp) <= width })
                assertTrue(lineCount(text, width.toFloat(), sp, measure) <= 2)
            }
        }
    }

    @Test
    fun wrapsOntoTwoLinesBeforeShrinking() {
        // "de patiënt" alone needs 140px at 28sp, the whole text 280px.
        assertEquals(28, fit("de patiënt in huis", 150f))
        assertEquals(2, lineCount("de patiënt in huis", 150f, 28, measure))
    }

    @Test
    fun shrinksToFitTheHeight() {
        // One line at 28sp is 33.6px high; 24px allows at most 20sp.
        assertEquals(20, fit("huis", 200f, height = 24f))
    }
}
