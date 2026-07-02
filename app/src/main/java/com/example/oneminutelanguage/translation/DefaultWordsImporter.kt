package com.example.oneminutelanguage.translation

import android.content.Context
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.WordEntity
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object DefaultWordsImporter {
    suspend fun importDefaultWords(
        context: Context,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ) {
        val wordDao = DatabaseProvider.getDatabase(context).wordDao()
        val englishWords = loadWordsFromAssets(context)

        val sourceCode = LanguageSettingsStore.getSourceLanguage(context)
        val targetCode = LanguageSettingsStore.getTargetLanguage(context)

        val sourceHelper = if (sourceCode != TranslateLanguage.ENGLISH) {
            TranslationHelper(sourceLanguage = TranslateLanguage.ENGLISH, targetLanguage = sourceCode)
        } else null

        val targetHelper = if (targetCode != TranslateLanguage.ENGLISH) {
            TranslationHelper(sourceLanguage = TranslateLanguage.ENGLISH, targetLanguage = targetCode)
        } else null

        try {
            sourceHelper?.ensureModelDownloaded()
            targetHelper?.ensureModelDownloaded()

            englishWords.forEachIndexed { index, englishWord ->
                onProgress(index + 1, englishWords.size)

                val sourceText = capitalize(sourceHelper?.translate(englishWord) ?: englishWord)
                val existing = wordDao.findByLanguage1Word(sourceText)

                if (existing != null) {
                    if (!existing.isDefault) {
                        wordDao.updateWord(existing.copy(isDefault = true))
                    }
                    return@forEachIndexed
                }

                val targetText = capitalize(targetHelper?.translate(englishWord) ?: englishWord)

                wordDao.insertWord(
                    WordEntity(
                        language1Word = sourceText,
                        language2Word = targetText,
                        dateAdded = System.currentTimeMillis(),
                        isDefault = true
                    )
                )
            }
        } finally {
            sourceHelper?.close()
            targetHelper?.close()
        }
    }

    suspend fun removeDefaultWords(context: Context) {
        DatabaseProvider.getDatabase(context).wordDao().deleteAllDefaultWords()
    }

    private suspend fun loadWordsFromAssets(context: Context): List<String> {
        return withContext(Dispatchers.IO) {
            val json = context.assets.open("words.json").bufferedReader().use { it.readText() }
            val array = JSONObject(json).getJSONArray("words")
            (0 until array.length()).map { array.getString(it) }
        }
    }

    private fun capitalize(text: String): String {
        return text.trim().replaceFirstChar { it.titlecase() }
    }
}
