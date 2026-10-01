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

/** Typed items: gap, order (tiles joined by spaces), transform strict; translate lenient. */
fun gradeText(item: Item, input: String): ItemResult {
    val accepted = when (item) {
        is Item.Gap -> item.answers
        is Item.Order -> listOf(item.answer) + item.alt
        is Item.Transform -> item.answers
        is Item.Translate -> item.answers
        else -> error("Not a typed item: $item")
    }
    val shown = when (item) {
        is Item.Gap -> item.text.replace("___", item.answers.first())
        else -> accepted.first()
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
