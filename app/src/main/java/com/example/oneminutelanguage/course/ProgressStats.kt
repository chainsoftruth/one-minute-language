package com.example.oneminutelanguage.course

import kotlin.math.roundToInt

/** The skills the Progress screen shows a meter for; the lesson kinds map one to one (checkpoint `test` lessons don't count). */
val SKILL_KINDS = listOf(
    LessonKind.READING, LessonKind.LISTENING, LessonKind.SPEAKING, LessonKind.WRITING, LessonKind.GRAMMAR, LessonKind.VOCAB
)

/** Average best score (0..100) of the lessons of each kind that were really done; null = none yet. */
fun skillMeters(path: List<PathLevel>, scores: Map<String, Int>): Map<LessonKind, Int?> {
    val lessons = path.flatMap { it.units }.mapNotNull { it.unit }.flatMap { it.lessons }
    return SKILL_KINDS.associateWith { kind ->
        val done = lessons.filter { it.kind == kind }.mapNotNull { scores[it.id] }
        if (done.isEmpty()) null else done.average().roundToInt()
    }
}

/** Completed / total lessons of a level, counting only units that exist. */
fun levelCounts(level: PathLevel, completed: Set<String>): Pair<Int, Int> {
    val lessons = level.units.mapNotNull { it.unit }.flatMap { it.lessons }
    return lessons.count { it.id in completed } to lessons.size
}
