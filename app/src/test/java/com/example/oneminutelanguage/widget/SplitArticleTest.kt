package com.example.oneminutelanguage.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class SplitArticleTest {
    @Test fun lowerCaseStarterWord() = assertEquals("het" to "huis", splitArticle("het huis"))

    @Test fun titleCasedWordLosesTheCapitalOnTheNoun() = assertEquals("de" to "fiets", splitArticle("De Fiets"))

    @Test fun nounKeepsItsOwnCase() = assertEquals("het" to "ziekenhuis", splitArticle("Het ziekenhuis"))

    @Test fun otherWordsAreLeftAlone() {
        assertEquals(null to "lopen", splitArticle("lopen"))
        assertEquals(null to "dertig", splitArticle("dertig"))
        assertEquals(null to "de", splitArticle("de"))
        assertEquals(null to "dezelfde man", splitArticle("dezelfde man"))
    }
}
