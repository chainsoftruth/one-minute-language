package com.example.oneminutelanguage.translation

sealed class TranslationState {
    object Idle : TranslationState()
    object DownloadingModel : TranslationState()
    object Translating : TranslationState()
    data class Success(val translatedText: String) : TranslationState()
    data class Error(val message: String) : TranslationState()
}
