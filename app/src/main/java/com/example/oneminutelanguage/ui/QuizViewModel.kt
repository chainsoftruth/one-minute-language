package com.example.oneminutelanguage.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.speech.WordSpeaker
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.widget.WidgetUpdater
import kotlinx.coroutines.launch

data class QuizQuestion(
    val wordId: Long,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int
)

sealed interface QuizPhase {
    data class Setup(val availableWords: Int) : QuizPhase
    object NotEnoughWords : QuizPhase
    object Running : QuizPhase
    object Finished : QuizPhase
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val wordDao = DatabaseProvider.getDatabase(application).wordDao()

    var phase by mutableStateOf<QuizPhase>(QuizPhase.Setup(0))
        private set

    var questions by mutableStateOf<List<QuizQuestion>>(emptyList())
        private set

    var currentIndex by mutableStateOf(0)
        private set

    var selectedOption by mutableStateOf<Int?>(null)
        private set

    var score by mutableStateOf(0)
        private set

    var correctWordIds by mutableStateOf<List<Long>>(emptyList())
        private set

    var correctWordsDisabled by mutableStateOf(false)
        private set

    var dontKnowCount by mutableStateOf(0)
        private set

    init {
        viewModelScope.launch {
            val count = wordDao.getEnabledWordsOnce().size
            phase = if (count < MIN_WORDS) QuizPhase.NotEnoughWords else QuizPhase.Setup(count)
        }
    }

    fun startQuiz(requestedCount: Int?) {
        viewModelScope.launch {
            val pool = wordDao.getEnabledWordsOnce()
            if (pool.size < MIN_WORDS) {
                phase = QuizPhase.NotEnoughWords
                return@launch
            }

            val selected = pool.shuffled().take(requestedCount ?: pool.size)

            questions = selected.map { word ->
                val prompt = stripNumericBrackets(word.language2Word)
                val correctRaw = stripNumericBrackets(word.language1Word)
                val correctHasBrackets = correctRaw.contains("(")

                val candidates = pool
                    .asSequence()
                    .filter { it.id != word.id }
                    .map { stripNumericBrackets(it.language1Word) }
                    .filter { it.isNotBlank() && !it.equals(correctRaw, ignoreCase = true) }
                    .distinct()
                    .toList()

                val matching = candidates
                    .filter { it.contains("(") == correctHasBrackets }
                    .shuffled()

                val (correctAnswer, distractors) = if (matching.size >= 3) {
                    correctRaw to matching.take(3)
                } else {
                    val correctPlain = stripAllBrackets(correctRaw)
                    val plainDistractors = candidates
                        .map { stripAllBrackets(it) }
                        .filter { it.isNotBlank() && !it.equals(correctPlain, ignoreCase = true) }
                        .distinct()
                        .shuffled()
                        .take(3)
                    correctPlain to plainDistractors
                }

                val options = (distractors + correctAnswer).shuffled()

                QuizQuestion(
                    wordId = word.id,
                    prompt = prompt,
                    options = options,
                    correctIndex = options.indexOf(correctAnswer)
                )
            }

            currentIndex = 0
            selectedOption = null
            score = 0
            correctWordIds = emptyList()
            correctWordsDisabled = false
            dontKnowCount = 0
            phase = QuizPhase.Running
        }
    }

    fun selectAnswer(index: Int) {
        if (selectedOption != null) return
        selectedOption = index

        if (index == DONT_KNOW) {
            dontKnowCount++
            return
        }

        val question = questions[currentIndex]
        if (index == question.correctIndex) {
            score++
            correctWordIds = correctWordIds + question.wordId
        }
    }

    fun nextQuestion() {
        if (selectedOption == null) return
        if (currentIndex + 1 >= questions.size) {
            phase = QuizPhase.Finished
        } else {
            currentIndex++
            selectedOption = null
        }
    }

    fun speakCurrentWord() {
        val question = questions.getOrNull(currentIndex) ?: return
        WordSpeaker.speak(
            getApplication(),
            question.prompt,
            LanguageSettingsStore.getTargetLanguage(getApplication())
        )
    }

    fun disableCorrectWords() {
        if (correctWordIds.isEmpty() || correctWordsDisabled) return
        viewModelScope.launch {
            wordDao.disableWords(correctWordIds)
            correctWordsDisabled = true
            WidgetUpdater.refreshWidget(getApplication())
        }
    }

    companion object {
        const val MIN_WORDS = 4
        const val DONT_KNOW = -1

        private val numericBrackets = Regex("""\s*\([^)]*\d[^)]*\)""")
        private val allBrackets = Regex("""\s*\([^)]*\)""")

        private fun stripNumericBrackets(text: String) = numericBrackets.replace(text, "").trim()
        private fun stripAllBrackets(text: String) = allBrackets.replace(text, "").trim()
    }
}
