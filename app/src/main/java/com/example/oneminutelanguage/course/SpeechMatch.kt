package com.example.oneminutelanguage.course

/** [score] = words of the target that were heard, in order, divided by the words of the target. */
data class Match(val score: Double, val words: List<Pair<String, Boolean>>)

/** Recognisers write digits, the lexicon writes words. */
private val DUTCH_NUMBERS = listOf(
    "nul", "een", "twee", "drie", "vier", "vijf", "zes", "zeven", "acht", "negen", "tien",
    "elf", "twaalf", "dertien", "veertien", "vijftien", "zestien", "zeventien", "achttien", "negentien", "twintig"
).withIndex().associate { (i, w) -> i.toString() to w }

/** Lower case, no accents, no punctuation, digits 0-20 spelled out. */
private fun key(word: String): String =
    stripDiacritics(normalize(word)).filter { it.isLetterOrDigit() || it == '\'' }.let { DUTCH_NUMBERS[it] ?: it }

private fun keys(s: String) = s.trim().split(Regex("\\s+")).map(::key).filter { it.isNotEmpty() }

/** Compares what the recogniser heard with the sentence the learner should say. LCS, so swapped words are penalised. */
fun score(heard: String, target: String): Match {
    val shown = target.trim().split(Regex("\\s+")).filter { key(it).isNotEmpty() }
    val want = shown.map(::key)
    val got = keys(heard)
    val dp = Array(got.size + 1) { IntArray(want.size + 1) }
    for (i in got.indices.reversed()) for (j in want.indices.reversed()) {
        dp[i][j] = if (got[i] == want[j]) dp[i + 1][j + 1] + 1 else maxOf(dp[i + 1][j], dp[i][j + 1])
    }
    val matched = BooleanArray(want.size)
    var i = 0
    var j = 0
    while (i < got.size && j < want.size) {
        when {
            got[i] == want[j] -> { matched[j] = true; i++; j++ }
            dp[i + 1][j] >= dp[i][j + 1] -> i++
            else -> j++
        }
    }
    val score = if (want.isEmpty()) 0.0 else matched.count { it }.toDouble() / want.size
    return Match(score, shown.mapIndexed { idx, w -> w to matched[idx] })
}

/** The recogniser returns several guesses; the best one counts. */
fun best(results: List<String>, target: String): Match =
    results.map { score(it, target) }.maxByOrNull { it.score } ?: score("", target)

const val SPEECH_CORRECT = 0.8
const val SPEECH_ALMOST = 0.5
const val SPEECH_TRIES = 3

/**
 * What the feedback panel shows for a spoken sentence. 0.8 or more is right, 0.5-0.8 is "almost" (counts, the
 * learner may have been misheard), less is wrong. [match] words are flagged heard / missed.
 */
fun gradeSpeech(match: Match, target: String): ItemResult = when {
    match.score >= SPEECH_CORRECT -> ItemResult(true, Verdict.CORRECT, target, diff = match.words)
    match.score >= SPEECH_ALMOST -> ItemResult(true, Verdict.ALMOST, target, "Almost: $target", match.words)
    else -> ItemResult(false, Verdict.WRONG, target, diff = match.words)
}

/** The learner graded themselves (shadowing, no recogniser available). */
fun gradeSelf(good: Boolean, target: String): ItemResult =
    if (good) ItemResult(true, Verdict.CORRECT, target) else ItemResult(false, Verdict.WRONG, target)
