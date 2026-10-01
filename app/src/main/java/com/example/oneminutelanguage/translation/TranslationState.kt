package com.example.oneminutelanguage.translation

sealed class TranslationState {
    object Idle : TranslationState()
    object DownloadingModel : TranslationState()
    object Translating : TranslationState()

    /** A model has to be downloaded and the phone is not on Wi-Fi: ask before using mobile data. */
    data class NeedsWifi(val languageName: String) : TranslationState()
    data class Success(val translatedText: String) : TranslationState()
    data class Error(val message: String) : TranslationState()
}
