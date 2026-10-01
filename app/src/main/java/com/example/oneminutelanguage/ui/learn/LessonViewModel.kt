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
import com.example.oneminutelanguage.course.NEW_CARD
import com.example.oneminutelanguage.course.dueAt
import com.example.oneminutelanguage.course.gradeChoice
import com.example.oneminutelanguage.course.gradeMatch
import com.example.oneminutelanguage.course.gradeText
import com.example.oneminutelanguage.course.lessonAfter
import com.example.oneminutelanguage.course.unitIdOf
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.LessonProgressEntity
import com.example.oneminutelanguage.data.ReviewCardEntity
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface LessonPhase {
    data object Loading : LessonPhase
    data class Failed(val message: String) : LessonPhase
    data object Explain : LessonPhase
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

    private var courseId = ""
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
            lesson = found
            if (found.explain.isEmpty()) startItems() else phase = LessonPhase.Explain
        }
    }

    fun startItems() {
        val l = lesson ?: return
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
            if (hasItems) queue.wrongItems.forEach { item ->
                db.reviewCardDao().insertIfAbsent(
                    ReviewCardEntity("item:$lessonId#${item.hashCode()}", courseId, dueAt(now, NEW_CARD),
                        NEW_CARD.intervalDays, NEW_CARD.ease, NEW_CARD.reps, NEW_CARD.lapses)
                )
            }
            nextLesson = lessonAfter(CourseRepository.path(getApplication(), courseId), lessonId)
        }
    }
}
