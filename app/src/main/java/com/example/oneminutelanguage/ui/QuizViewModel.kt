package com.example.oneminutelanguage.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.WordEntity
import com.example.oneminutelanguage.speech.WordSpeaker
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.translation.withoutHints
import com.example.oneminutelanguage.translation.withoutNumericHints
import com.example.oneminutelanguage.widget.WidgetUpdater
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.launch

data class QuizQuestion(
    val wordId: Long,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int
)

enum class QuizMode { MEANING, REVERSE, ARTICLE }

sealed interface QuizPhase {
    /** [articleWords] is null when the target language isn't Dutch (no de / het mode). */
    data class Setup(val availableWords: Int, val articleWords: Int?) : QuizPhase
    object NotEnoughWords : QuizPhase
    object Running : QuizPhase
    object Finished : QuizPhase
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val wordDao = DatabaseProvider.getDatabase(application).wordDao()
    private val isDutch = LanguageSettingsStore.getTargetLanguage(application) == TranslateLanguage.DUTCH

    var phase by mutableStateOf<QuizPhase>(QuizPhase.Setup(0, null))
        private set

    var mode by mutableStateOf(QuizMode.MEANING)
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
            val words = wordDao.getEnabledWordsOnce()
            phase = if (words.size < MIN_WORDS) {
                QuizPhase.NotEnoughWords
            } else {
                QuizPhase.Setup(words.size, if (isDutch) words.count { articleOf(it.language2Word) != null } else null)
            }
        }
    }

    fun selectMode(newMode: QuizMode) {
        mode = newMode
    }

    fun startQuiz(requestedCount: Int?) {
        viewModelScope.launch {
            val pool = wordDao.getEnabledWordsOnce()
            if (pool.size < MIN_WORDS) {
                phase = QuizPhase.NotEnoughWords
                return@launch
            }

            val quizPool = if (mode == QuizMode.ARTICLE) pool.filter { articleOf(it.language2Word) != null } else pool
            val selected = quizPool.shuffled().take(requestedCount ?: quizPool.size)
            if (selected.isEmpty()) return@launch

            // Reverse = the same question with the columns swapped.
            val reversedPool by lazy { pool.map { it.reversed() } }
            questions = selected.map { word ->
                when (mode) {
                    QuizMode.MEANING -> buildQuestion(word, pool)
                    QuizMode.REVERSE -> buildQuestion(word.reversed(), reversedPool)
                    QuizMode.ARTICLE -> buildArticleQuestion(word)
                }
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

        // Meaning mode speaks the prompt; the other modes speak the answer once it's given.
        if (mode != QuizMode.MEANING) speakCurrentWord()

        if (index == DONT_KNOW) {
            dontKnowCount++
            return
        }

        val question = questions[currentIndex]
        if (index == question.correctIndex) {
            score++
            correctWordIds = correctWordIds + question.wordId
            viewModelScope.launch { wordDao.markLearned(question.wordId, System.currentTimeMillis()) }
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
        val answer = question.options[question.correctIndex]
        WordSpeaker.speak(
            getApplication(),
            when (mode) {
                QuizMode.MEANING -> question.prompt
                QuizMode.REVERSE -> answer
                QuizMode.ARTICLE -> "$answer ${question.prompt}"
            },
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
    }
}

private val ARTICLES = listOf("de", "het")

/** "Het ziekenhuis" -> "het"; null when the word doesn't start with "de " or "het ". */
internal fun articleOf(dutch: String): String? =
    ARTICLES.firstOrNull { dutch.startsWith("$it ", ignoreCase = true) }

/** The noun without its article or hints; the options are de / het. */
internal fun buildArticleQuestion(word: WordEntity): QuizQuestion {
    val article = requireNotNull(articleOf(word.language2Word))
    return QuizQuestion(
        wordId = word.id,
        prompt = word.language2Word.withoutHints().drop(article.length).trim(),
        options = ARTICLES,
        correctIndex = ARTICLES.indexOf(article)
    )
}

private fun WordEntity.reversed() = copy(language1Word = language2Word, language2Word = language1Word)

internal fun buildQuestion(word: WordEntity, pool: List<WordEntity>): QuizQuestion {
    val prompt = word.language2Word.withoutHints()
    val correctRaw = word.language1Word.withoutNumericHints()
    val correctHasBrackets = correctRaw.contains("(")

    val candidates = pool
        .asSequence()
        .filter { it.id != word.id }
        // Synonyms ("groot" = big / large) would be marked wrong, so skip them.
        .filter { !it.language2Word.withoutHints().equals(prompt, ignoreCase = true) }
        .map { it.language1Word.withoutNumericHints() }
        .filter { it.isNotBlank() && !it.equals(correctRaw, ignoreCase = true) }
        .distinct()
        .toList()

    val matching = candidates
        .filter { it.contains("(") == correctHasBrackets }
        .shuffled()

    val (correctAnswer, distractors) = if (matching.size >= 3) {
        correctRaw to matching.take(3)
    } else {
        val correctPlain = correctRaw.withoutHints()
        val plainDistractors = candidates
            .map { it.withoutHints() }
            .filter { it.isNotBlank() && !it.equals(correctPlain, ignoreCase = true) }
            .distinct()
            .shuffled()
            .take(3)
        correctPlain to plainDistractors
    }

    val options = (distractors + correctAnswer).shuffled()

    return QuizQuestion(
        wordId = word.id,
        prompt = prompt,
        options = options,
        correctIndex = options.indexOf(correctAnswer)
    )
}
