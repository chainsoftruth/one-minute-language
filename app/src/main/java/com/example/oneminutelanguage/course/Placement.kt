package com.example.oneminutelanguage.course

import kotlin.random.Random

const val PLACEMENT_PER_LEVEL = 10

/** Right answers needed at a level to move on to the next one. */
const val PLACEMENT_PASS = 7

class PlacementItem(val level: String, val item: Item)

/**
 * Where the placement questions come from: the A1 and A2 checkpoint's first lesson and the practice lesson (the last one)
 * of every B1 grammar unit. Only choice, gap and order items: they need no audio, no text and no typing of long answers.
 * ponytail: B1 has no checkpoint of grammar items (b1.exam is reading/listening/writing/speaking), so its practice lessons stand in.
 */
fun placementPools(units: List<CourseUnit>): Map<String, List<Item>> {
    fun usable(lessons: List<Lesson>) = lessons.flatMap { it.items }.filter { it is Item.Choice || it is Item.Gap || it is Item.Order }
    fun first(id: String) = usable(units.filter { it.id == id }.mapNotNull { it.lessons.firstOrNull() })
    return mapOf(
        "A1" to first("a1.cp"),
        "A2" to first("a2.cp"),
        "B1" to usable(units.filter { it.level == "B1" && it.kind == UnitKind.GRAMMAR }.mapNotNull { it.lessons.lastOrNull() })
    )
}

/** [PLACEMENT_PER_LEVEL] random items per level, easiest level first. */
fun placementTest(units: List<CourseUnit>, random: Random = Random): List<PlacementItem> =
    placementPools(units).flatMap { (level, pool) -> pool.shuffled(random).take(PLACEMENT_PER_LEVEL).map { PlacementItem(level, it) } }

/** Right answers per level, given the items that were answered wrong. */
fun placementScores(test: List<PlacementItem>, wrong: List<Item>): Map<String, Int> =
    test.groupBy { it.level }.mapValues { (_, items) -> items.count { p -> wrong.none { it === p.item } } }

/** Under 7/10 at A1 -> A1; else under 7/10 at A2 -> A2; else B1. */
fun placementStart(scores: Map<String, Int>): String = when {
    (scores["A1"] ?: 0) < PLACEMENT_PASS -> "A1"
    (scores["A2"] ?: 0) < PLACEMENT_PASS -> "A2"
    else -> "B1"
}

class PlacementResult(val scores: Map<String, Int>, val start: String)
