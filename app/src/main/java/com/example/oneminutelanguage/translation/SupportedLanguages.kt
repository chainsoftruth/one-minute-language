package com.example.oneminutelanguage.translation

import com.google.mlkit.nl.translate.TranslateLanguage

data class LanguageOption(val code: String, val displayName: String)

object SupportedLanguages {
    val all: List<LanguageOption> = listOf(
        LanguageOption(TranslateLanguage.ENGLISH, "English"),
        LanguageOption(TranslateLanguage.GERMAN, "German"),
        LanguageOption(TranslateLanguage.FRENCH, "French"),
        LanguageOption(TranslateLanguage.ITALIAN, "Italian"),
        LanguageOption(TranslateLanguage.SPANISH, "Spanish"),
        LanguageOption(TranslateLanguage.POLISH, "Polish"),
        LanguageOption(TranslateLanguage.ROMANIAN, "Romanian"),
        LanguageOption(TranslateLanguage.PORTUGUESE, "Portuguese"),
        LanguageOption(TranslateLanguage.DUTCH, "Dutch"),
        LanguageOption(TranslateLanguage.UKRAINIAN, "Ukrainian")
    )

    fun displayNameFor(code: String): String {
        return all.firstOrNull { it.code == code }?.displayName ?: code
    }
}
