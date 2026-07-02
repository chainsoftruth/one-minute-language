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

    val sourceLanguageName: String = SupportedLanguages.displayNameFor(sourceLanguageCode)
    val targetLanguageName: String = SupportedLanguages.displayNameFor(targetLanguageCode)

    private val wordDao = DatabaseProvider.getDatabase(application).wordDao()

    var translationState by mutableStateOf<TranslationState>(TranslationState.Idle)
        private set

    fun translateWord(input: String) {
        if (input.isBlank()) return

        viewModelScope.launch {
            try {
                translationState = TranslationState.DownloadingModel
                translationHelper.ensureModelDownloaded()

                translationState = TranslationState.Translating
                val result = translationHelper.translate(input)

                translationState = TranslationState.Success(result)
            } catch (e: Exception) {
                translationState = TranslationState.Error(e.message ?: "Translation failed")
            }
        }
    }

    fun saveWord(originalWord: String, onSaved: () -> Unit) {
        val currentState = translationState
        if (currentState !is TranslationState.Success) return

        val capitalizedOriginal = originalWord.trim().replaceFirstChar { it.titlecase() }
        val capitalizedTranslation = currentState.translatedText.trim().replaceFirstChar { it.titlecase() }

        viewModelScope.launch {
            if (wordDao.wordExists(capitalizedOriginal)) {
                translationState = TranslationState.Error("\"$capitalizedOriginal\" is already in your list.")
                return@launch
            }

            wordDao.insertWord(
                WordEntity(
                    language1Word = capitalizedOriginal,
                    language2Word = capitalizedTranslation,
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
    }
}
