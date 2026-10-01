package com.example.oneminutelanguage.ui.learn

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.CourseRepository
import com.example.oneminutelanguage.course.ItemResult
import com.example.oneminutelanguage.course.Item
import com.example.oneminutelanguage.course.Lesson
import com.example.oneminutelanguage.course.LessonKind
import com.example.oneminutelanguage.course.LessonQueue
import com.example.oneminutelanguage.course.LexEntry
import com.example.oneminutelanguage.course.NEW_CARD
import com.example.oneminutelanguage.course.buildVocabLesson
import com.example.oneminutelanguage.course.dueAt
import com.example.oneminutelanguage.course.gradeChoice
import com.example.oneminutelanguage.course.gradeMatch
import com.example.oneminutelanguage.course.gradeText
import com.example.oneminutelanguage.course.lessonAfter
import com.example.oneminutelanguage.course.unitIdOf
import com.example.oneminutelanguage.course.vocabCardSchedule
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.LessonProgressEntity
import com.example.oneminutelanguage.data.ReviewCardEntity
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

sealed interface LessonPhase {
    data object Loading : LessonPhase
    data class Failed(val message: String) : LessonPhase
    data object Explain : LessonPhase

    /** Vocab lessons: swipeable word cards before the exercises. */
    data object Intro : LessonPhase
    data object Items : LessonPhase
    data object Done : LessonPhase
}

class LessonViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])
    private val db = DatabaseProvider.getDatabase(application)

    var phase by mutableStateOf<LessonPhase>(LessonPhase.Loading); private set
    var lesson by mutableStateOf<Lesson?>(null); private set
    val unitId: String = unitIdOf(lessonId)
    var ttsLocale = "nl-NL"; private set

    /** The words of a vocab lesson, for the intro cards. */
    var introEntries by mutableStateOf<List<LexEntry>>(emptyList()); private set

    private var courseId = ""
    private var introDone = false
    /** Generated vocab items -> the words they test, so a wrong answer schedules the right review cards. */
    private var lexOf: Map<Item, List<String>> = emptyMap()
    private lateinit var queue: LessonQueue

    /** Changes every time an item is shown (a re-queued item shows twice), so the views reset their state. */
    var step by mutableIntStateOf(0); private set
    var currentItem by mutableStateOf<Item?>(null); private set

    /** What the learner has picked or typed, not checked yet. A choice is its option index as text. */
    var pending by mutableStateOf<String?>(null)
    var result by mutableStateOf<ItemResult?>(null); private set
    var progressDone by mutableIntStateOf(0); private set
    var progressTotal by mutableIntStateOf(0); private set

    var score by mutableIntStateOf(0); private set
    var mistakes by mutableIntStateOf(0); private set
    var nextLesson by mutableStateOf<Lesson?>(null); private set

    init {
        viewModelScope.launch {
            val id = CoursePrefs.selectedCourse(application)
            val found = id?.let { CourseRepository.lesson(application, it, lessonId) }
            if (id == null || found == null) {
                phase = LessonPhase.Failed("This lesson isn't available.")
                return@launch
            }
            courseId = id
            ttsLocale = CourseRepository.courses(application).firstOrNull { it.id == id }?.ttsLocale ?: ttsLocale
            if (found.kind == LessonKind.VOCAB) {
                val lexicon = CourseRepository.lexicon(application, id)
                val entries = found.vocab.mapNotNull { lexicon[it] }
                if (entries.isEmpty()) {
                    phase = LessonPhase.Failed("This lesson has no words yet.")
                    return@launch
                }
                val built = buildVocabLesson(entries, lexicon.values.toList(), Random)
                lexOf = built.associate { it.item to it.lexIds }
                introEntries = entries
                lesson = found.copy(items = built.map { it.item })
            } else {
                lesson = found
            }
            if (found.explain.isEmpty()) startItems() else phase = LessonPhase.Explain
        }
    }

    fun startItems() {
        val l = lesson ?: return
        if (l.kind == LessonKind.VOCAB && !introDone) {
            introDone = true
            phase = LessonPhase.Intro
            return
        }
        if (l.items.isEmpty()) return finish()
        // Checkpoints (test lessons) give no feedback until the end and no second chance.
        queue = LessonQueue(l.items, requeue = l.kind != LessonKind.TEST)
        phase = LessonPhase.Items
        showNext()
    }

    private fun showNext() {
        val index = queue.currentIndex
        if (index == null) return finish()
        currentItem = lesson!!.items[index]
        step++
        pending = null
        result = null
        progressDone = queue.progressDone
        progressTotal = queue.progressTotal
    }

    fun check() {
        val answer = pending ?: return
        record(
            when (val item = currentItem) {
                is Item.Choice -> gradeChoice(item, answer.toInt())
                is Item.Gap, is Item.Order, is Item.Transform, is Item.Translate -> gradeText(item, answer)
                else -> return
            }
        )
    }

    fun submitMatch(mistakes: Int) {
        val item = currentItem as? Item.Match ?: return
        record(gradeMatch(item, mistakes))
    }

    private fun record(r: ItemResult) {
        result = r
        queue.answer(r.correct)
        progressDone = queue.progressDone
        progressTotal = queue.progressTotal
        viewModelScope.launch { db.dailyStatsDao().addExercises(LocalDate.now().toString(), 1) }
        if (lesson?.kind == LessonKind.TEST) showNext()
    }

    fun continueNext() = showNext()

    /** For item types that arrive in later stages. */
    fun skip() {
        queue.skip()
        showNext()
    }

    private var finished = false

    private fun finish() {
        if (finished) return
        finished = true
        val hasItems = lesson?.items?.isNotEmpty() == true
        score = if (hasItems) queue.score else 100
        mistakes = if (hasItems) queue.mistakes else 0
        phase = LessonPhase.Done
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val old = db.lessonProgressDao().get(lessonId)
            db.lessonProgressDao().upsert(
                LessonProgressEntity(lessonId, courseId, now, maxOf(old?.bestScore ?: 0, score), (old?.attempts ?: 0) + 1)
            )
            val cards = db.reviewCardDao()
            if (lesson?.kind == LessonKind.VOCAB) {
                val wrong = queue.wrongItems.flatMap { lexOf[it].orEmpty() }.toSet()
                vocabCardSchedule(lesson?.vocab.orEmpty(), wrong, now).forEach { (cardId, due) -> cards.insertIfAbsent(newCard(cardId, due)) }
            } else if (hasItems) {
                queue.wrongItems.forEach { cards.insertIfAbsent(newCard("item:$lessonId#${it.hashCode()}", dueAt(now, NEW_CARD))) }
            }
            nextLesson = lessonAfter(CourseRepository.path(getApplication(), courseId), lessonId)
        }
    }

    private fun newCard(cardId: String, due: Long) =
        ReviewCardEntity(cardId, courseId, due, NEW_CARD.intervalDays, NEW_CARD.ease, NEW_CARD.reps, NEW_CARD.lapses)

}
