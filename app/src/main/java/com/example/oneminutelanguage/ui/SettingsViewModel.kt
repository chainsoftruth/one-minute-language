package com.example.oneminutelanguage.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.translation.DefaultWordsImporter
import com.example.oneminutelanguage.translation.DefaultWordsPrefs
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.translation.TranslationHelper
import com.example.oneminutelanguage.widget.WidgetUpdater
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val wordDao = database.wordDao()

    var sourceLanguage by mutableStateOf(LanguageSettingsStore.getSourceLanguage(application))
        private set

    var targetLanguage by mutableStateOf(LanguageSettingsStore.getTargetLanguage(application))
        private set

    var applyState by mutableStateOf<SettingsApplyState>(SettingsApplyState.Idle)
        private set

    var defaultWordsEnabled by mutableStateOf(DefaultWordsPrefs.isEnabled(application))
        private set

    var defaultWordsState by mutableStateOf<DefaultWordsState>(DefaultWordsState.Idle)
        private set

    var showDisableDefaultWordsConfirmation by mutableStateOf(false)
        private set

    fun selectSourceLanguage(code: String) {
        sourceLanguage = code
    }

    fun selectTargetLanguage(code: String) {
        targetLanguage = code
    }

    fun swapLanguages() {
        val previous = sourceLanguage
        sourceLanguage = targetLanguage
        targetLanguage = previous
    }

    fun applyChanges() {
        val context: Application = getApplication()
        val previousSource = LanguageSettingsStore.getSourceLanguage(context)
        val previousTarget = LanguageSettingsStore.getTargetLanguage(context)
        val newSource = sourceLanguage
        val newTarget = targetLanguage

        if (previousSource == newSource && previousTarget == newTarget) {
            applyState = SettingsApplyState.Success
            return
        }

        if (previousSource == newTarget && previousTarget == newSource) {
            viewModelScope.launch {
                try {
                    wordDao.swapLanguageColumns()

                    LanguageSettingsStore.setSourceLanguage(context, newSource)
                    LanguageSettingsStore.setTargetLanguage(context, newTarget)

                    WidgetUpdater.refreshWidget(context)

                    applyState = SettingsApplyState.Success
                } catch (e: Exception) {
                    applyState = SettingsApplyState.Error(e.message ?: "Failed to swap languages")
                }
            }
            return
        }

        viewModelScope.launch {
            var sourceMigrator: TranslationHelper? = null
            var targetMigrator: TranslationHelper? = null

            try {
                applyState = SettingsApplyState.DownloadingModels

                if (previousSource != newSource) {
                    sourceMigrator = TranslationHelper(
                        sourceLanguage = previousSource,
                        targetLanguage = newSource
                    )
                    sourceMigrator.ensureModelDownloaded()
                }

                if (previousTarget != newTarget) {
                    targetMigrator = TranslationHelper(
                        sourceLanguage = previousTarget,
                        targetLanguage = newTarget
                    )
                    targetMigrator.ensureModelDownloaded()
                }

                if (sourceMigrator != null || targetMigrator != null) {
                    val words = wordDao.getAllWordsOnce()

                    if (words.isNotEmpty()) {
                        val updatedWords = words.mapIndexed { index, word ->
                            applyState = SettingsApplyState.Translating(index + 1, words.size)

                            var updated = word
                            sourceMigrator?.let {
                                updated = updated.copy(language1Word = it.translate(word.language1Word))
                            }
                            targetMigrator?.let {
                                updated = updated.copy(language2Word = it.translate(word.language2Word))
                            }
                            updated
                        }

                        database.withTransaction {
                            updatedWords.forEach { wordDao.updateWord(it) }
                        }
                    }
                }

                LanguageSettingsStore.setSourceLanguage(context, newSource)
                LanguageSettingsStore.setTargetLanguage(context, newTarget)

                WidgetUpdater.refreshWidget(context)

                applyState = SettingsApplyState.Success
            } catch (e: Exception) {
                applyState = SettingsApplyState.Error(e.message ?: "Failed to update settings")
            } finally {
                sourceMigrator?.close()
                targetMigrator?.close()
            }
        }
    }

    fun onToggleDefaultWords(turnOn: Boolean) {
        if (turnOn) {
            enableDefaultWords()
        } else {
            showDisableDefaultWordsConfirmation = true
        }
    }

    fun confirmDisableDefaultWords() {
        showDisableDefaultWordsConfirmation = false
        disableDefaultWords()
    }

    fun cancelDisableDefaultWords() {
        showDisableDefaultWordsConfirmation = false
    }

    private fun enableDefaultWords() {
        val context: Application = getApplication()

        DefaultWordsPrefs.setEnabled(context, true)
        defaultWordsEnabled = true

        viewModelScope.launch {
            try {
                defaultWordsState = DefaultWordsState.DownloadingModels
                DefaultWordsImporter.importDefaultWords(context) { current, total ->
                    defaultWordsState = DefaultWordsState.Importing(current, total)
                }
                WidgetUpdater.refreshWidget(context)
                defaultWordsState = DefaultWordsState.Done
            } catch (e: Exception) {
                defaultWordsState = DefaultWordsState.Error(e.message ?: "Failed to import default words")
            }
        }
    }

    private fun disableDefaultWords() {
        val context: Application = getApplication()

        viewModelScope.launch {
            try {
                defaultWordsState = DefaultWordsState.Removing
                wordDao.deleteAllDefaultWords()
                DefaultWordsPrefs.setEnabled(context, false)
                defaultWordsEnabled = false
                WidgetUpdater.refreshWidget(context)
                defaultWordsState = DefaultWordsState.Done
            } catch (e: Exception) {
                defaultWordsState = DefaultWordsState.Error(e.message ?: "Failed to remove default words")
            }
        }
    }
}
