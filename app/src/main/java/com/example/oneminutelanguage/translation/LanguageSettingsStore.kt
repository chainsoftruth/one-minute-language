package com.example.oneminutelanguage.translation

import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage

object LanguageSettingsStore {
    private const val PREFS_NAME = "language_settings"
    private const val KEY_SOURCE = "source_language"
    private const val KEY_TARGET = "target_language"

    private val DEFAULT_SOURCE = TranslateLanguage.ENGLISH
    private val DEFAULT_TARGET = TranslateLanguage.DUTCH

    fun getSourceLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SOURCE, DEFAULT_SOURCE) ?: DEFAULT_SOURCE
    }

    fun getTargetLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TARGET, DEFAULT_TARGET) ?: DEFAULT_TARGET
    }

    fun setSourceLanguage(context: Context, code: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SOURCE, code)
            .apply()
    }

    fun setTargetLanguage(context: Context, code: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TARGET, code)
            .apply()
    }
}
