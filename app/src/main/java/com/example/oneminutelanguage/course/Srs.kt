package com.example.oneminutelanguage.course

enum class Grade { AGAIN, HARD, GOOD, EASY }

data class CardState(val intervalDays: Double, val ease: Double, val reps: Int, val lapses: Int)

val NEW_CARD = CardState(0.0, 2.5, 0, 0)

// ponytail: SM-2-lite with fixed constants. Upgrade path: FSRS, if review load becomes a problem.
fun schedule(s: CardState, g: Grade): CardState = when (g) {
    Grade.AGAIN -> s.copy(intervalDays = 0.0, ease = maxOf(1.3, s.ease - 0.2), reps = 0, lapses = s.lapses + 1)
    Grade.HARD -> s.copy(intervalDays = maxOf(1.0, s.intervalDays * 1.2), ease = maxOf(1.3, s.ease - 0.15), reps = s.reps + 1)
    Grade.GOOD -> s.copy(intervalDays = when (s.reps) { 0 -> 1.0; 1 -> 3.0; else -> s.intervalDays * s.ease }, reps = s.reps + 1)
    Grade.EASY -> s.copy(intervalDays = if (s.reps == 0) 3.0 else s.intervalDays * s.ease * 1.3, ease = s.ease + 0.15, reps = s.reps + 1)
}.let { it.copy(intervalDays = minOf(it.intervalDays, 180.0)) }

fun dueAt(now: Long, s: CardState): Long =
    if (s.intervalDays == 0.0) now + 10 * 60_000L else now + (s.intervalDays * 86_400_000L).toLong()

/** Mastered = interval of at least 21 days. */
fun CardState.isMastered() = intervalDays >= 21
