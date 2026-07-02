package com.example.oneminutelanguage.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

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

    suspend fun ensureModelDownloaded(requireWifi: Boolean = true) {
        val conditionsBuilder = DownloadConditions.Builder()
        if (requireWifi) conditionsBuilder.requireWifi()
        translator.downloadModelIfNeeded(conditionsBuilder.build()).await()
    }

    suspend fun translate(text: String): String {
        return translator.translate(text).await()
    }

    fun close() {
        translator.close()
    }
}
