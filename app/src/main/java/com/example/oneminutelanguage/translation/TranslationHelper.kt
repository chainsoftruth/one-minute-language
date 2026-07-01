package com.example.oneminutelanguage.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/**
 * Wraps a single ML Kit Translator client configured for one language pair.
 * Create one instance per language pair you need (e.g. English -> Dutch).
 */
class TranslationHelper(
    sourceLanguage: String,
    targetLanguage: String
) {
    private val translator: Translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()
    )

    /**
     * Downloads the on-device language model if it isn't already present.
     * Suspends until the download completes (or returns immediately if
     * the model is already downloaded).
     */
    suspend fun ensureModelDownloaded(requireWifi: Boolean = true) {
        val conditionsBuilder = DownloadConditions.Builder()
        if (requireWifi) conditionsBuilder.requireWifi()
        translator.downloadModelIfNeeded(conditionsBuilder.build()).await()
    }

    /**
     * Translates the given text. Assumes the model has already been downloaded
     * via ensureModelDownloaded() — call that first.
     */
    suspend fun translate(text: String): String {
        return translator.translate(text).await()
    }

    /**
     * Releases native resources held by the translator. Must be called
     * when this helper is no longer needed.
     */
    fun close() {
        translator.close()
    }
}