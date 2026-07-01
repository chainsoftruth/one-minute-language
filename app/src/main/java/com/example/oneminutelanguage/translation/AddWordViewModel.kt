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
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.launch

class AddWordViewModel(application: Application) : AndroidViewModel(application) {

    // Hardcoded for now: English -> Dutch.
    // Later this can be made configurable based on the user's settings.
    private val translationHelper = TranslationHelper(
        sourceLanguage = TranslateLanguage.ENGLISH,
        targetLanguage = TranslateLanguage.DUTCH
    )

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

        viewModelScope.launch {
            wordDao.insertWord(
                WordEntity(
                    language1Word = originalWord,
                    language2Word = currentState.translatedText,
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