package com.example.oneminutelanguage.course

/**
 * The order items are shown in. A wrong item is re-queued once at the end (Duolingo-style) but counts
 * as wrong only once for the score. [requeue] = false for checkpoints, which give no second chance.
 */
class LessonQueue(private val items: List<Item>, private val requeue: Boolean = true) {
    private val pending = ArrayDeque(items.indices.toList())
    private val requeued = mutableSetOf<Int>()
    private val wrong = sortedSetOf<Int>()
    private var skipped = 0
    private var slotsDone = 0

    /** Index into the lesson's items of the item to show now; null when finished. */
    val currentIndex: Int? get() = pending.firstOrNull()
    val isDone: Boolean get() = pending.isEmpty()

    /** Items answered so far / items to answer in all (grows when a wrong item is re-queued). */
    val progressDone: Int get() = slotsDone
    val progressTotal: Int get() = slotsDone + pending.size

    val mistakes: Int get() = wrong.size
    val wrongItems: List<Item> get() = wrong.map { items[it] }
    val score: Int get() {
        val scored = items.size - skipped
        return if (scored <= 0) 100 else (scored - wrong.size) * 100 / scored
    }

    fun answer(correct: Boolean) {
        val i = pending.removeFirst()
        slotsDone++
        if (!correct) {
            wrong += i
            if (requeue && requeued.add(i)) pending.addLast(i)
        }
    }

    /** For items the app can't show yet; they count neither way. */
    fun skip() {
        pending.removeFirst()
        skipped++
    }
}
