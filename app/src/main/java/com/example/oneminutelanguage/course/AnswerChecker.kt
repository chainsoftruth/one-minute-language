package com.example.oneminutelanguage.course

import java.text.Normalizer

/** ACCENT = only diacritics differ (ë/e, é/e). */
enum class Verdict { CORRECT, ACCENT, ALMOST, WRONG }

fun normalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFC)
        .lowercase()
        .replace('’', '\'').replace('‘', '\'')
        .replace('“', '"').replace('”', '"')
        .trim()
        .replace(Regex("\\s+"), " ")
        .replace(Regex("[.!?]+$"), "")
        .replace(Regex("\\s+([,;])"), "$1")
        .trim()

fun stripDiacritics(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

/**
 * [lenient] = typo tolerance (translate, speech): Levenshtein <= 1 per 8 chars of the expected answer.
 * Strict items (gap, transform, order, dictation) never return ALMOST: one letter is often the grammar point.
 */
fun check(input: String, accepted: List<String>, lenient: Boolean): Verdict {
    val got = normalize(input)
    if (got.isEmpty()) return Verdict.WRONG
    val options = accepted.map { normalize(it) }
    if (got in options) return Verdict.CORRECT
    val gotPlain = stripDiacritics(got)
    if (options.any { stripDiacritics(it) == gotPlain }) return Verdict.ACCENT
    if (lenient && options.any { levenshtein(gotPlain, stripDiacritics(it)) <= it.length / 8 }) return Verdict.ALMOST
    return Verdict.WRONG
}

/** A noun typed without its article (`fiets` for `de fiets`): ALMOST, for vocab exercises. */
fun missingArticle(input: String, accepted: List<String>): Boolean {
    val got = stripDiacritics(normalize(input))
    return got.isNotEmpty() && accepted.any {
        val parts = normalize(it).split(' ', limit = 2)
        parts.size == 2 && parts[0] in setOf("de", "het") && stripDiacritics(parts[1]) == got
    }
}

fun levenshtein(a: String, b: String): Int {
    var prev = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        val cur = IntArray(b.length + 1)
        cur[0] = i
        for (j in 1..b.length) {
            cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
        }
        prev = cur
    }
    return prev[b.length]
}

/** The expected answer's words, each flagged: does it appear in the input? Used to colour feedback. */
fun diffWords(input: String, expected: String): List<Pair<String, Boolean>> {
    val got = normalize(input).split(' ').filter { it.isNotEmpty() }
    val want = expected.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    // LCS over normalised words, so one wrong word doesn't mark everything after it.
    val w = want.map { normalize(it) }
    val dp = Array(got.size + 1) { IntArray(w.size + 1) }
    for (i in got.indices.reversed()) for (j in w.indices.reversed()) {
        dp[i][j] = if (got[i] == w[j]) dp[i + 1][j + 1] + 1 else maxOf(dp[i + 1][j], dp[i][j + 1])
    }
    val matched = BooleanArray(w.size)
    var i = 0
    var j = 0
    while (i < got.size && j < w.size) {
        when {
            got[i] == w[j] -> { matched[j] = true; i++; j++ }
            dp[i + 1][j] >= dp[i][j + 1] -> i++
            else -> j++
        }
    }
    return want.mapIndexed { idx, word -> word to matched[idx] }
}
