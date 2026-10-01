package com.example.oneminutelanguage.course

import kotlin.random.Random

/** A generated item and the lexicon entries it tests (so a wrong answer can schedule the right review cards). */
class VocabItem(val item: Item, val lexIds: List<String>)

/**
 * The practice for a vocab lesson: a `match` per 5 words, then `choice` nl -> en for every word,
 * `translate` en -> nl for half of the words, and de / het for every noun with a single article.
 */
fun buildVocabLesson(entries: List<LexEntry>, pool: List<LexEntry>, random: Random): List<VocabItem> {
    val out = mutableListOf<VocabItem>()
    for (chunk in matchChunks(entries)) {
        // Left and right sides must be unique, or two words would look identical.
        val unique = chunk.distinctBy { it.display() }.distinctBy { it.en }
        if (unique.size >= 3) out += VocabItem(Item.Match(unique.map { listOf(it.display(), it.en) }), unique.map { it.id })
    }
    entries.forEach { out += VocabItem(choiceNlEn(it, pool, random), listOf(it.id)) }
    entries.shuffled(random).take((entries.size + 1) / 2)
        .forEach { out += VocabItem(Item.Translate(it.en, it.answers()), listOf(it.id)) }
    entries.filter { it.hasSingleArticle() }.forEach {
        out += VocabItem(Item.Choice(q = it.nl, options = listOf("de", "het"), answer = if (it.art == "de") 0 else 1, en = it.en), listOf(it.id))
    }
    return out
}

fun buildLesson(entries: List<LexEntry>, pool: List<LexEntry>, random: Random): List<Item> =
    buildVocabLesson(entries, pool, random).map { it.item }

/**
 * One question for a due `lex:` card. New cards get multiple choice, known ones must type the word.
 * ponytail: from 4 repetitions on the plan wants listen + type; until the listening exercises (Stage 4) it is translate.
 */
fun buildReviewItem(entry: LexEntry, pool: List<LexEntry>, reps: Int, random: Random): Item =
    if (reps >= 2) Item.Translate(entry.en, entry.answers()) else choiceNlEn(entry, pool, random)

/** Groups of 5; a leftover of 1-2 words borrows from the previous group so every match has 3-6 pairs. */
private fun matchChunks(entries: List<LexEntry>): List<List<LexEntry>> {
    val chunks = entries.chunked(5).toMutableList()
    if (chunks.size > 1 && chunks.last().size < 3) {
        val last = chunks.removeAt(chunks.size - 1)
        val prev = chunks.removeAt(chunks.size - 1)
        val need = 3 - last.size
        chunks += prev.dropLast(need)
        chunks += prev.takeLast(need) + last
    }
    return chunks
}

/** Dutch word -> English meaning with 3 distractors; a translate item when the pool is too small to make a choice. */
private fun choiceNlEn(e: LexEntry, pool: List<LexEntry>, random: Random): Item {
    val wrong = distractors(e, pool, random)
    if (wrong.isEmpty()) return Item.Translate(e.en, e.answers())
    val options = (wrong + e.en).shuffled(random)
    return Item.Choice(q = e.display(), options = options, answer = options.indexOf(e.en))
}

/** Same part of speech and topic first, then same part of speech, then anything; never the same English meaning. */
private fun distractors(e: LexEntry, pool: List<LexEntry>, random: Random, n: Int = 3): List<String> {
    val others = pool.filter { it.id != e.id && it.en != e.en }
    val picked = LinkedHashSet<String>()
    for (tier in listOf(others.filter { it.pos == e.pos && it.topic == e.topic }, others.filter { it.pos == e.pos }, others)) {
        for (c in tier.shuffled(random)) {
            if (picked.size == n) break
            picked += c.en
        }
        if (picked.size == n) break
    }
    return picked.toList()
}
