package com.example.oneminutelanguage.course

import android.content.Context
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.WordEntity
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.widget.WidgetUpdater
import com.google.mlkit.nl.translate.TranslateLanguage

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
        dao.insertWord(WordEntity(language1Word = english, language2Word = dutch, dateAdded = System.currentTimeMillis(), isDefault = false))
        added++
    }
    if (added > 0) WidgetUpdater.refreshWidget(context)
    return added to (entries.size - added)
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
