package com.example.oneminutelanguage.course

/** One entry of the course path: [unit] is null while the unit's file doesn't exist yet ("Coming soon"). */
class PathUnit(val id: String, val unit: CourseUnit?)

class PathLevel(val outline: LevelOutline, val units: List<PathUnit>)

/** First lesson (in course order) that is not completed, with its unit. Null when everything is done. */
fun firstUnfinished(path: List<PathLevel>, completed: Set<String>): Pair<CourseUnit, Lesson>? {
    for (level in path) for (entry in level.units) {
        val unit = entry.unit ?: continue
        val lesson = unit.lessons.firstOrNull { it.id !in completed } ?: continue
        return unit to lesson
    }
    return null
}

/** Ids of every lesson in the levels before [level] (what "Start here" in the placement test marks as skipped). */
fun lessonsBefore(path: List<PathLevel>, level: String): List<String> =
    path.takeWhile { it.outline.level != level }.flatMap { it.units }.mapNotNull { it.unit }.flatMap { it.lessons }.map { it.id }

/** The lesson after [lessonId] in course order, skipping units without content. */
fun lessonAfter(path: List<PathLevel>, lessonId: String): Lesson? {
    val lessons = path.flatMap { it.units }.mapNotNull { it.unit }.flatMap { it.lessons }
    val i = lessons.indexOfFirst { it.id == lessonId }
    return if (i < 0) null else lessons.getOrNull(i + 1)
}
