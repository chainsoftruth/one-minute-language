package com.example.oneminutelanguage.course

import kotlin.random.Random

/** What the feedback panel shows after an item is checked. */
data class ItemResult(
    val correct: Boolean,
    val verdict: Verdict,
    /** The answer to show when wrong (or nearly right). */
    val expected: String,
    /** Extra line under "Correct!" (accent / almost / article hints). */
    val note: String? = null,
    /** Expected words flagged matched / missed, for text answers. */
    val diff: List<Pair<String, Boolean>>? = null
)

fun gradeChoice(item: Item.Choice, picked: Int): ItemResult =
    ItemResult(picked == item.answer, if (picked == item.answer) Verdict.CORRECT else Verdict.WRONG, item.options[item.answer])

/** The item is correct if the learner made at most one wrong pairing. */
fun gradeMatch(item: Item.Match, mistakes: Int): ItemResult =
    ItemResult(mistakes <= 1, if (mistakes <= 1) Verdict.CORRECT else Verdict.WRONG, item.pairs.joinToString("  ·  ") { it.joinToString(" = ") })

/** The answer shown when an item is wrong or given up (empty for items without one). */
fun expectedAnswer(item: Item): String = when (item) {
    is Item.Choice -> item.options[item.answer]
    is Item.Gap -> item.text.replace("___", item.answers.first())
    is Item.Order -> item.answer
    is Item.Transform -> item.answers.first()
    is Item.Translate -> item.answers.first()
    is Item.Match -> item.pairs.joinToString("  ·  ") { it.joinToString(" = ") }
    is Item.Listen -> if (item.options.isEmpty()) item.nl else item.options[item.answer]
    is Item.Speak -> item.nl
    is Item.OpenPrompt, is Item.Write -> ""
}

/** Listen items: a picked option, or (no options) a dictation checked strictly, accents forgiven. */
fun gradeListen(item: Item.Listen, input: String): ItemResult =
    if (item.options.isEmpty()) gradeText(item, input) else gradeChoice(Item.Choice(item.nl, item.options, item.answer), input.toInt())

/** Review grade for a result: right = GOOD, almost (typo, missing article) = HARD, wrong = AGAIN. */
fun gradeFor(result: ItemResult): Grade = when (result.verdict) {
    Verdict.CORRECT, Verdict.ACCENT -> Grade.GOOD
    Verdict.ALMOST -> Grade.HARD
    Verdict.WRONG -> Grade.AGAIN
}

/** Typed items: gap, order (tiles joined by spaces), transform and dictation strict; translate lenient. */
fun gradeText(item: Item, input: String): ItemResult {
    val accepted = when (item) {
        is Item.Gap -> item.answers
        is Item.Order -> listOf(item.answer) + item.alt
        is Item.Transform -> item.answers
        is Item.Translate -> item.answers
        is Item.Listen -> listOf(item.nl)
        else -> error("Not a typed item: $item")
    }
    val shown = expectedAnswer(item)
    // A noun typed without its article (vocab exercises): counts, but the learner is told.
    if ((item is Item.Translate || item is Item.Listen) && missingArticle(input, accepted)) {
        return ItemResult(true, Verdict.ALMOST, shown, "Don't forget the article: $shown", diffWords(input, shown))
    }
    val verdict = check(input, accepted, lenient = item is Item.Translate)
    val note = when (verdict) {
        Verdict.ACCENT -> "Watch the accent: $shown"
        Verdict.ALMOST -> "Almost: $shown"
        else -> null
    }
    val needsDiff = verdict == Verdict.WRONG || verdict == Verdict.ALMOST
    return ItemResult(
        correct = verdict != Verdict.WRONG,
        verdict = verdict,
        expected = shown,
        note = note,
        diff = if (needsDiff) diffWords(input, shown) else null
    )
}

/**
 * Tiles for an `order` item: the answer's words (final .?! removed) plus `extra`, shuffled.
 * ponytail: the first word is lowercased unless it is Ik / U, so a sentence-initial name loses its capital.
 * The checker ignores case, so only the look of the tile suffers. Upgrade path: ask the lexicon.
 */
fun orderTiles(item: Item.Order, random: Random = Random): List<String> {
    val words = item.answer.trim().trimEnd('.', '!', '?').split(' ').filter { it.isNotEmpty() }.toMutableList()
    if (words.isNotEmpty() && words[0] != "Ik" && words[0] != "U") words[0] = words[0].replaceFirstChar { it.lowercase() }
    return (words + item.extra).shuffled(random)
}
