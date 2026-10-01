package com.example.oneminutelanguage.translation

import android.content.Context
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object DefaultWordsImporter {
    suspend fun importDefaultWords(
        context: Context,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ) {
        val wordDao = DatabaseProvider.getDatabase(context).wordDao()
        val pairs = loadWordPairs(
            context,
            LanguageSettingsStore.getSourceLanguage(context),
            LanguageSettingsStore.getTargetLanguage(context)
        )

        pairs.forEachIndexed { index, (sourceRaw, targetRaw) ->
            onProgress(index + 1, pairs.size)

            val sourceText = capitalize(sourceRaw.withoutNumericHints())
            val existing = wordDao.findByLanguage1Word(sourceText)

            if (existing != null) {
                if (!existing.isDefault) {
                    wordDao.updateWord(existing.copy(isDefault = true))
                }
                return@forEachIndexed
            }

            wordDao.insertWord(
                WordEntity(
                    language1Word = sourceText,
                    language2Word = capitalize(targetRaw.withoutNumericHints()),
                    dateAdded = System.currentTimeMillis(),
                    isDefault = true
                )
            )
        }
    }

    suspend fun removeDefaultWords(context: Context) {
        DatabaseProvider.getDatabase(context).wordDao().deleteAllDefaultWords()
    }

    /** words.json is an array of {"en": "...", "nl": "...", ...}; entries missing either language are skipped. */
    private suspend fun loadWordPairs(context: Context, source: String, target: String): List<Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            val json = context.assets.open("words.json").bufferedReader().use { it.readText() }
            val array = JSONArray(json)
            (0 until array.length())
                .map { array.getJSONObject(it) }
                .filter { it.has(source) && it.has(target) }
                .map { it.getString(source) to it.getString(target) }
        }
    }

    private fun capitalize(text: String): String {
        return text.trim().replaceFirstChar { it.titlecase() }
    }
}
