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
        val dutchWords = loadWordsFromAssets(context, "words_nl.json")
        val ukrainianWords = loadWordsFromAssets(context, "words_uk.json")
        val frenchWords = loadWordsFromAssets(context, "words_fr.json")
        val germanWords = loadWordsFromAssets(context, "words_de.json")
        val italianWords = loadWordsFromAssets(context, "words_it.json")
        val spanishWords = loadWordsFromAssets(context, "words_es.json")
        val portugueseWords = loadWordsFromAssets(context, "words_pt.json")
        val polishWords = loadWordsFromAssets(context, "words_pl.json")
        val romanianWords = loadWordsFromAssets(context, "words_ro.json")

        val sourceCode = LanguageSettingsStore.getSourceLanguage(context)
        val targetCode = LanguageSettingsStore.getTargetLanguage(context)

        val hardcodedLanguages = setOf(TranslateLanguage.ENGLISH, TranslateLanguage.DUTCH, TranslateLanguage.UKRAINIAN, TranslateLanguage.FRENCH, TranslateLanguage.GERMAN, TranslateLanguage.ITALIAN, TranslateLanguage.SPANISH, TranslateLanguage.PORTUGUESE, TranslateLanguage.POLISH, TranslateLanguage.ROMANIAN)

        val sourceHelper = if (sourceCode !in hardcodedLanguages) {
            TranslationHelper(sourceLanguage = TranslateLanguage.ENGLISH, targetLanguage = sourceCode)
        } else null

        val targetHelper = if (targetCode !in hardcodedLanguages) {
            TranslationHelper(sourceLanguage = TranslateLanguage.ENGLISH, targetLanguage = targetCode)
        } else null

        try {
            sourceHelper?.ensureModelDownloaded()
            targetHelper?.ensureModelDownloaded()

            englishWords.forEachIndexed { index, englishWord ->
                onProgress(index + 1, englishWords.size)

                val sourceRaw = when {
                    sourceCode == TranslateLanguage.DUTCH -> dutchWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.UKRAINIAN -> ukrainianWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.FRENCH -> frenchWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.GERMAN -> germanWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.ITALIAN -> italianWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.SPANISH -> spanishWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.PORTUGUESE -> portugueseWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.POLISH -> polishWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.ROMANIAN -> romanianWords.getOrElse(index) { englishWord }
                    sourceCode == TranslateLanguage.ENGLISH -> englishWord
                    else -> sourceHelper?.translate(englishWord) ?: englishWord
                }
                val sourceText = capitalize(stripNumericAnnotation(sourceRaw))
                val existing = wordDao.findByLanguage1Word(sourceText)

                if (existing != null) {
                    if (!existing.isDefault) {
                        wordDao.updateWord(existing.copy(isDefault = true))
                    }
                    return@forEachIndexed
                }

                val targetRaw = when {
                    targetCode == TranslateLanguage.DUTCH -> dutchWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.UKRAINIAN -> ukrainianWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.FRENCH -> frenchWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.GERMAN -> germanWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.ITALIAN -> italianWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.SPANISH -> spanishWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.PORTUGUESE -> portugueseWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.POLISH -> polishWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.ROMANIAN -> romanianWords.getOrElse(index) { englishWord }
                    targetCode == TranslateLanguage.ENGLISH -> englishWord
                    else -> targetHelper?.translate(englishWord) ?: englishWord
                }
                val targetText = capitalize(stripNumericAnnotation(targetRaw))

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

    private suspend fun loadWordsFromAssets(context: Context, fileName: String = "words.json"): List<String> {
        return withContext(Dispatchers.IO) {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val array = JSONObject(json).getJSONArray("words")
            (0 until array.length()).map { array.getString(it) }
        }
    }

    private fun capitalize(text: String): String {
        return text.trim().replaceFirstChar { it.titlecase() }
    }

    private val numericParenthetical = Regex("""\s*\([^)]*\d[^)]*\)""")

    private fun stripNumericAnnotation(text: String): String {
        return numericParenthetical.replace(text, "").trim()
    }
}
