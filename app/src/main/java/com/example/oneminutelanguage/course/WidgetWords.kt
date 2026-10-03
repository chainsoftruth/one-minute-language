package com.example.oneminutelanguage.course

import android.content.Context
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.WordEntity
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.translation.withoutHints
import com.example.oneminutelanguage.widget.WidgetUpdater
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.CancellationException

/** Course words can go into "My words" (and so onto the widget) only when the app's pair is English -> Dutch. */
fun canSendToWidget(context: Context): Boolean =
    LanguageSettingsStore.getSourceLanguage(context) == TranslateLanguage.ENGLISH &&
        LanguageSettingsStore.getTargetLanguage(context) == TranslateLanguage.DUTCH

/** Adds each entry to "My words" unless it is already there, then refreshes the widget. Returns (added, already there). */
suspend fun sendToWidget(context: Context, entries: List<LexEntry>): Pair<Int, Int> {
    val dao = DatabaseProvider.getDatabase(context).wordDao()
    var added = 0
    for (entry in entries) {
        val (english, dutch) = entry.widgetPair()
        if (dao.findDuplicate(english, dutch) != null) continue
        dao.insertWord(
            WordEntity(language1Word = english, language2Word = dutch, dateAdded = System.currentTimeMillis(), isDefault = false, topic = entry.topic)
        )
        added++
    }
    if (added > 0) WidgetUpdater.refreshWidget(context)
    return added to (entries.size - added)
}

/** "de fiets" / "het huis" / "lopen" (lowercase, every article a word takes) -> its topic id. */
fun topicIndex(lexicon: Collection<LexEntry>): Map<String, String> =
    lexicon.flatMap { e -> e.answers().map { it.lowercase() to e.topic } }.toMap()

/** Topic of a stored Dutch word ("De fiets", "Herfst (het seizoen)"), or null when the lexicon doesn't have it. */
fun topicOf(index: Map<String, String>, dutch: String): String? = index[dutch.withoutHints().lowercase()]

/** Empty when the course files can't be read: topics are a nice-to-have, never a reason to fail. */
suspend fun lexiconTopicIndex(context: Context): Map<String, String> =
    try {
        topicIndex(CourseRepository.lexicon(context, "nl").values)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        emptyMap()
    }

/** Topic id -> title ("food_drink" -> "Food & drink"). */
suspend fun loadTopicTitles(context: Context): Map<String, String> =
    try {
        CourseRepository.outline(context, "nl").topics.associate { it.id to it.title }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        emptyMap()
    }

/** Gives words saved before topics existed (and words typed by hand) their topic. Only when the app teaches Dutch. */
suspend fun backfillTopics(context: Context) {
    if (LanguageSettingsStore.getTargetLanguage(context) != TranslateLanguage.DUTCH) return
    val dao = DatabaseProvider.getDatabase(context).wordDao()
    val missing = dao.getAllWordsOnce().filter { it.topic == null }
    if (missing.isEmpty()) return
    val index = lexiconTopicIndex(context)
    for (word in missing) topicOf(index, word.language2Word)?.let { dao.setTopic(word.id, it) }
}

/** "12 words added, 3 already in your list". */
fun sendToWidgetMessage(added: Int, already: Int): String {
    fun words(n: Int) = if (n == 1) "1 word" else "$n words"
    return when {
        added == 0 -> "${words(already)} already in your list"
        already == 0 -> "${words(added)} added"
        else -> "${words(added)} added, $already already in your list"
    }
}
