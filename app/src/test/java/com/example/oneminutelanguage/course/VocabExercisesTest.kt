package com.example.oneminutelanguage.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class VocabExercisesTest {
    private fun noun(id: String, art: String, en: String, topic: String = "t") =
        LexEntry(id, id, en, "noun", "A1", topic, art = art, pl = "${id}s")
    private fun verb(id: String, en: String) =
        LexEntry(id, id, en, "verb", "A1", "t", pres = "${id}t", past = "${id}te/${id}ten", pp = "ge${id}d", aux = "heeft")

    private val entries = listOf(
        noun("fiets", "de", "bicycle"), noun("huis", "het", "house"), noun("stoel", "de", "chair"),
        noun("boek", "het", "book"), noun("tafel", "de", "table"), noun("bed", "het", "bed"),
        verb("loop", "to walk"), verb("werk", "to work")
    )
    private val pool = entries + listOf(noun("kat", "de", "cat"), noun("hond", "de", "dog"), noun("lamp", "de", "lamp"), verb("praat", "to talk"))

    @Test fun everyWordGetsAChoiceWithItsMeaningAndNoDuplicateOptions() {
        val items = buildLesson(entries, pool, Random(1)).filterIsInstance<Item.Choice>().filter { it.options.size > 2 }
        assertEquals(entries.size, items.size)
        for (item in items) {
            val entry = entries.single { it.display() == item.q }
            assertEquals(entry.en, item.options[item.answer])
            assertEquals(item.options.size, item.options.toSet().size)
            assertEquals(4, item.options.size)
        }
    }

    @Test fun distractorsNeverShareTheEnglishMeaning() {
        val twin = noun("rijwiel", "het", "bicycle")
        val items = buildLesson(entries, pool + twin, Random(2)).filterIsInstance<Item.Choice>().filter { it.q == "de fiets" }
        assertEquals(1, items.size)
        assertEquals(1, items[0].options.count { it == "bicycle" })
    }

    @Test fun nounsAreTypedWithTheirArticleAndVerbsWithout() {
        val translate = buildLesson(entries, pool, Random(3)).filterIsInstance<Item.Translate>()
        assertEquals(4, translate.size)
        for (t in translate) {
            val entry = entries.single { it.en == t.en }
            assertEquals(entry.answers(), t.answers)
            if (entry.pos == "noun") assertTrue(t.answers.single().startsWith(entry.art!! + " "))
        }
    }

    @Test fun nounsGetADeHetQuestion() {
        val article = buildLesson(entries, pool, Random(4)).filterIsInstance<Item.Choice>().filter { it.options == listOf("de", "het") }
        assertEquals(entries.count { it.pos == "noun" }, article.size)
        assertTrue(article.all { c -> entries.single { it.nl == c.q }.art == c.options[c.answer] })
    }

    @Test fun oneMatchPerFiveWordsAndALeftoverJoinsTheLastGroup() {
        val matches = buildLesson(entries, pool, Random(5)).filterIsInstance<Item.Match>()
        // 8 words: 5 + 3 pairs.
        assertEquals(listOf(5, 3), matches.map { it.pairs.size })
        // 7 words would leave a group of 2, so the last group borrows one: 4 + 3.
        val seven = buildLesson(entries.take(7), pool, Random(5)).filterIsInstance<Item.Match>()
        assertEquals(listOf(4, 3), seven.map { it.pairs.size })
    }

    @Test fun sameSeedGivesTheSameLesson() {
        assertEquals(buildLesson(entries, pool, Random(42)), buildLesson(entries, pool, Random(42)))
    }

    @Test fun aTinyPoolFallsBackToTranslate() {
        val items = buildLesson(listOf(entries[0]), listOf(entries[0]), Random(1))
        assertTrue(items.none { it is Item.Choice && it.options.size > 2 })
        assertTrue(items.any { it is Item.Translate })
    }

    @Test fun reviewItemsGrowFromChoiceToTyping() {
        assertTrue(buildReviewItem(entries[0], pool, 0, Random(1)) is Item.Choice)
        assertTrue(buildReviewItem(entries[0], pool, 1, Random(1)) is Item.Choice)
        assertTrue(buildReviewItem(entries[0], pool, 2, Random(1)) is Item.Translate)
        assertTrue(buildReviewItem(entries[0], pool, 6, Random(1)) is Item.Translate)
    }

    @Test fun bareNounIsAlmostRightAndNeedsTheArticle() {
        val item = Item.Translate("bicycle", listOf("de fiets"))
        assertEquals(Verdict.CORRECT, gradeText(item, "de fiets").verdict)
        val bare = gradeText(item, "Fiets")
        assertEquals(Verdict.ALMOST, bare.verdict)
        assertTrue(bare.correct)
        assertEquals("Don't forget the article: de fiets", bare.note)
        assertEquals(Grade.HARD, gradeFor(bare))
        assertEquals(Verdict.WRONG, gradeText(item, "het fiets").verdict)
        assertEquals(Grade.AGAIN, gradeFor(gradeText(item, "stoel")))
        assertEquals(Grade.GOOD, gradeFor(gradeText(item, "de fiets")))
    }

    @Test fun displayAnswersAndWidgetPair() {
        val fiets = noun("fiets", "de", "bicycle")
        assertEquals("de fiets", fiets.display())
        assertEquals("Bicycle" to "De fiets", fiets.widgetPair())
        val both = fiets.copy(art = "de/het")
        assertEquals(listOf("de fiets", "het fiets"), both.answers())
        val country = fiets.copy(nl = "Nederland", art = "-", pl = "-", en = "the Netherlands")
        assertEquals("Nederland", country.display())
        assertEquals(listOf("Nederland"), country.answers())
        assertEquals("loop · loopte · geloopd (heeft)", verb("loop", "to walk").copy(past = "loopte/loopten", pp = "geloopd").formsLine())
    }
}
