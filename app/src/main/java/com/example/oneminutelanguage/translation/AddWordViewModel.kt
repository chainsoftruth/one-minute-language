package com.example.oneminutelanguage.translation

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.WordEntity
import com.example.oneminutelanguage.widget.WidgetUpdater
import kotlinx.coroutines.launch

class AddWordViewModel(application: Application) : AndroidViewModel(application) {
    private val sourceLanguageCode = LanguageSettingsStore.getSourceLanguage(application)
    private val targetLanguageCode = LanguageSettingsStore.getTargetLanguage(application)

    private val translationHelper = TranslationHelper(
        sourceLanguage = sourceLanguageCode,
        targetLanguage = targetLanguageCode
    )

    // For a word you heard: type it in the learning language, get the native translation.
    private val reverseTranslationHelper = TranslationHelper(
        sourceLanguage = targetLanguageCode,
        targetLanguage = sourceLanguageCode
    )

    private val sourceName = SupportedLanguages.displayNameFor(sourceLanguageCode)
    private val targetName = SupportedLanguages.displayNameFor(targetLanguageCode)

    private val wordDao = DatabaseProvider.getDatabase(application).wordDao()

    var targetFirst by mutableStateOf(false)
        private set

    val inputLanguageName: String get() = if (targetFirst) targetName else sourceName
    val outputLanguageName: String get() = if (targetFirst) sourceName else targetName

    var translationState by mutableStateOf<TranslationState>(TranslationState.Idle)
        private set

    fun toggleDirection() {
        targetFirst = !targetFirst
        translationState = TranslationState.Idle
    }

    fun translateWord(input: String) {
        if (input.isBlank()) return
        val helper = if (targetFirst) reverseTranslationHelper else translationHelper

        viewModelScope.launch {
            try {
                translationState = TranslationState.DownloadingModel
                helper.ensureModelDownloaded()

                translationState = TranslationState.Translating
                val result = helper.translate(input)

                translationState = TranslationState.Success(result)
            } catch (e: Exception) {
                translationState = TranslationState.Error(e.message ?: "Translation failed")
            }
        }
    }

    fun saveWord(originalWord: String, translatedWord: String, onSaved: () -> Unit) {
        if (translationState !is TranslationState.Success) return
        if (translatedWord.isBlank()) return

        val capitalizedOriginal = originalWord.trim().replaceFirstChar { it.titlecase() }
        val capitalizedTranslation = translatedWord.trim().replaceFirstChar { it.titlecase() }
        val language1Word = if (targetFirst) capitalizedTranslation else capitalizedOriginal
        val language2Word = if (targetFirst) capitalizedOriginal else capitalizedTranslation

        viewModelScope.launch {
            val duplicate = wordDao.findDuplicate(language1Word, language2Word)
            if (duplicate != null) {
                translationState = TranslationState.Error(
                    "\"${duplicate.language2Word} – ${duplicate.language1Word}\" is already in your list."
                )
                return@launch
            }

            wordDao.insertWord(
                WordEntity(
                    language1Word = language1Word,
                    language2Word = language2Word,
                    dateAdded = System.currentTimeMillis()
                )
            )

            WidgetUpdater.refreshWidget(getApplication())

            onSaved()
        }
    }

    override fun onCleared() {
        super.onCleared()
        translationHelper.close()
        reverseTranslationHelper.close()
    }
}
