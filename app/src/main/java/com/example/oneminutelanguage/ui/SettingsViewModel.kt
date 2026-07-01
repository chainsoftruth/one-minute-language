package com.example.oneminutelanguage.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.translation.TranslationHelper
import com.example.oneminutelanguage.widget.WidgetUpdater
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)
    private val wordDao = database.wordDao()

    // What's shown in the dropdowns. Nothing is persisted or re-translated
    // until the user taps "Update Settings".
    var sourceLanguage by mutableStateOf(LanguageSettingsStore.getSourceLanguage(application))
        private set

    var targetLanguage by mutableStateOf(LanguageSettingsStore.getTargetLanguage(application))
        private set

    var applyState by mutableStateOf<SettingsApplyState>(SettingsApplyState.Idle)
        private set

    fun selectSourceLanguage(code: String) {
        sourceLanguage = code
    }

    fun selectTargetLanguage(code: String) {
        targetLanguage = code
    }

    /**
     * Persists the selected languages. If either changed, downloads the model for
     * the old -> new pair and re-translates every existing word's corresponding
     * column (English -> Ukrainian if source changed, Dutch -> French if target
     * changed, etc.) before committing.
     */
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

                        // Translation (network/ML work) happens above, outside the
                        // transaction — only the fast DB writes are atomic here.
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
}
