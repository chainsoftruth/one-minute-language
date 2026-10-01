package com.example.oneminutelanguage.course

fun wordCount(text: String): Int = text.trim().split(Regex("\\s+")).count { it.any { c -> c.isLetterOrDigit() } }

/** Self-assessed writing score, 0..1: the share of required points covered; 0 when the text is shorter than the minimum. */
fun writingScore(covered: Int, points: Int, words: Int, minWords: Int): Double =
    if (words < minWords) 0.0 else if (points <= 0) 1.0 else covered.coerceIn(0, points).toDouble() / points
