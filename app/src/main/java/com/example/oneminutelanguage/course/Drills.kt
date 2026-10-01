package com.example.oneminutelanguage.course

import kotlin.random.Random

const val DRILL_SIZE = 10

/**
 * Practice drills: up to [n] `listen` / `speak` items from lessons the learner has completed, topped up with the
 * example sentences of lexicon [entries] (dictation / speaking) when the lessons don't have enough.
 */
fun listeningDrill(lessonItems: List<Item>, entries: List<LexEntry>, random: Random, n: Int = DRILL_SIZE): List<Item> {
    val own = lessonItems.filterIsInstance<Item.Listen>().shuffled(random).take(n)
    val topUp = entries.mapNotNull { it.ex }.shuffled(random).take(n - own.size).map { Item.Listen(it) }
    return (own + topUp).shuffled(random)
}

fun speakingDrill(lessonItems: List<Item>, entries: List<LexEntry>, random: Random, n: Int = DRILL_SIZE): List<Item> {
    val own = lessonItems.filterIsInstance<Item.Speak>().shuffled(random).take(n)
    val topUp = entries.filter { it.ex != null && it.exEn != null }.shuffled(random).take(n - own.size).map { Item.Speak(it.ex!!, it.exEn!!) }
    return (own + topUp).shuffled(random)
}
