package com.example.oneminutelanguage.ui.learn

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.course.CardState
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.CourseRepository
import com.example.oneminutelanguage.course.Item
import com.example.oneminutelanguage.course.ItemResult
import com.example.oneminutelanguage.course.Verdict
import com.example.oneminutelanguage.course.buildReviewItem
import com.example.oneminutelanguage.course.dueAt
import com.example.oneminutelanguage.course.etaText
import com.example.oneminutelanguage.course.expectedAnswer
import com.example.oneminutelanguage.course.gradeChoice
import com.example.oneminutelanguage.course.gradeFor
import com.example.oneminutelanguage.course.gradeListen
import com.example.oneminutelanguage.course.gradeMatch
import com.example.oneminutelanguage.course.gradeText
import com.example.oneminutelanguage.course.schedule
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.ReviewCardEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

private const val SESSION_SIZE = 20

/** Cards due right now for the selected course; the clock is re-read every minute so the badge stays honest. */
@OptIn(ExperimentalCoroutinesApi::class)
fun dueCountFlow(app: Application): Flow<Int> {
    val dao = DatabaseProvider.getDatabase(app).reviewCardDao()
    val ticks = flow { while (true) { emit(System.currentTimeMillis()); delay(60_000) } }
    return CoursePrefs.selectedCourseFlow(app).flatMapLatest { id ->
        if (id == null) flowOf(0) else ticks.flatMapLatest { now -> dao.dueCount(id, now) }
    }
}

sealed interface ReviewPhase {
    data object Loading : ReviewPhase
    data object NoCourse : ReviewPhase
    /** A course file could not be read; the cards themselves are untouched. */
    data object Failed : ReviewPhase
    /** Nothing is due; [next] says when the next card is due. */
    data class Empty(val next: String?) : ReviewPhase
    data object Question : ReviewPhase
    data class Done(val reviewed: Int, val next: String?) : ReviewPhase
}

class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val cards = db.reviewCardDao()

    private class Question(val card: ReviewCardEntity, val item: Item)

    private val queue = ArrayDeque<Question>()
    private var courseId = ""

    var phase by mutableStateOf<ReviewPhase>(ReviewPhase.Loading); private set
    var ttsLocale = "nl-NL"; private set

    /** Changes with every question so the exercise views reset their state. */
    var step by mutableIntStateOf(0); private set
    var currentItem by mutableStateOf<Item?>(null); private set
    var pending by mutableStateOf<String?>(null)
    var result by mutableStateOf<ItemResult?>(null); private set
    var done by mutableIntStateOf(0); private set
    var total by mutableIntStateOf(0); private set

    val dueCount: StateFlow<Int> = dueCountFlow(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            val id = CoursePrefs.selectedCourse(application)
            if (id == null) {
                phase = ReviewPhase.NoCourse
                return@launch
            }
            courseId = id
            try {
                loadCards(application, id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                phase = ReviewPhase.Failed
            }
        }
    }

    private suspend fun loadCards(application: Application, id: String) {
        ttsLocale = CourseRepository.courses(application).firstOrNull { it.id == id }?.ttsLocale ?: ttsLocale
        val lexicon = CourseRepository.lexicon(application, id)
        val pool = lexicon.values.toList()
        for (card in cards.due(id, System.currentTimeMillis(), SESSION_SIZE)) {
            val item = when {
                card.cardId.startsWith("lex:") ->
                    lexicon[card.cardId.removePrefix("lex:")]?.let { buildReviewItem(it, pool, card.reps, Random) }
                card.cardId.startsWith("item:") -> {
                    val lessonId = card.cardId.removePrefix("item:").substringBefore('#')
                    val hash = card.cardId.substringAfter('#').toIntOrNull()
                    CourseRepository.lesson(application, id, lessonId)?.items?.firstOrNull { it.hashCode() == hash }
                }
                else -> null
            }
            // The word or the item no longer exists (content was edited): drop the card.
            if (item == null) cards.delete(card.cardId)
            else if (item.isShownHere()) queue += Question(card, item)
        }
        total = queue.size
        if (queue.isEmpty()) phase = ReviewPhase.Empty(nextText())
        else {
            phase = ReviewPhase.Question
            showNext()
        }
    }

    /** Speaking and writing items arrive in Stages 4-5 (they need the recogniser / a text box); their cards just wait. */
    private fun Item.isShownHere() = this is Item.Choice || this is Item.Gap || this is Item.Order ||
        this is Item.Transform || this is Item.Translate || this is Item.Match || this is Item.Listen

    private suspend fun nextText(): String? =
        cards.nextDueAt(courseId)?.let { etaText(System.currentTimeMillis(), it) }

    private fun showNext() {
        val q = queue.firstOrNull()
        if (q == null) {
            val reviewed = done
            phase = ReviewPhase.Done(reviewed, null)
            viewModelScope.launch { phase = ReviewPhase.Done(reviewed, nextText()) }
            return
        }
        currentItem = q.item
        step++
        pending = null
        result = null
    }

    fun check() {
        val answer = pending ?: return
        record(
            when (val item = currentItem) {
                is Item.Choice -> gradeChoice(item, answer.toInt())
                is Item.Gap, is Item.Order, is Item.Transform, is Item.Translate -> gradeText(item, answer)
                is Item.Listen -> gradeListen(item, answer)
                else -> return
            }
        )
    }

    fun submitMatch(mistakes: Int) {
        val item = currentItem as? Item.Match ?: return
        record(gradeMatch(item, mistakes))
    }

    /** "I don't know": counts as wrong and shows the answer. */
    fun giveUp() {
        val item = currentItem ?: return
        record(ItemResult(false, Verdict.WRONG, expectedAnswer(item)))
    }

    private fun record(r: ItemResult) {
        if (result != null) return
        result = r
        val q = queue.removeFirst()
        done++
        val grade = gradeFor(r)
        viewModelScope.launch {
            val next = schedule(CardState(q.card.intervalDays, q.card.ease, q.card.reps, q.card.lapses), grade)
            cards.upsert(
                q.card.copy(dueAt = dueAt(System.currentTimeMillis(), next), intervalDays = next.intervalDays,
                    ease = next.ease, reps = next.reps, lapses = next.lapses)
            )
            db.dailyStatsDao().addExercises(LocalDate.now().toString(), 1)
        }
    }

    fun continueNext() = showNext()
}
