package com.example.oneminutelanguage.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslationHelper(
    private val sourceLanguage: String,
    private val targetLanguage: String
) {
    private val translator: Translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()
    )

    /** Language codes whose model is not on the phone yet (English is bundled, so it never shows up here). */
    suspend fun missingModels(): List<String> {
        val manager = RemoteModelManager.getInstance()
        return listOf(sourceLanguage, targetLanguage).filter {
            !manager.isModelDownloaded(TranslateRemoteModel.Builder(it).build()).await()
        }
    }

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
