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
import com.example.oneminutelanguage.course.DRILL_SIZE
import com.example.oneminutelanguage.course.Match
import com.example.oneminutelanguage.course.SPEECH_ALMOST
import com.example.oneminutelanguage.course.SPEECH_CORRECT
import com.example.oneminutelanguage.course.SPEECH_TRIES
import com.example.oneminutelanguage.course.best
import com.example.oneminutelanguage.course.gradeListen
import com.example.oneminutelanguage.course.gradeSelf
import com.example.oneminutelanguage.course.gradeSpeech
import com.example.oneminutelanguage.course.listeningDrill
import com.example.oneminutelanguage.course.speakingDrill
import com.example.oneminutelanguage.course.CourseRepository
import com.example.oneminutelanguage.course.FlagType
import com.example.oneminutelanguage.course.WordLookup
import com.example.oneminutelanguage.course.canSendToWidget
import com.example.oneminutelanguage.course.expectedAnswer
import com.example.oneminutelanguage.course.flagLine
import com.example.oneminutelanguage.course.sendToWidget
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

sealed interface LessonPhase {
    data object Loading : LessonPhase
    data class Failed(val message: String) : LessonPhase
    data object Explain : LessonPhase

    /** Vocab lessons: swipeable word cards before the exercises. */
    data object Intro : LessonPhase

    /** Listening lessons: the dialogue player, before the questions. */
    data object Dialogue : LessonPhase

    /** Speaking lessons with a dialogue: the learner plays speaker B. */
    data object Roleplay : LessonPhase

    /** Reading lessons: the text with tap-to-translate, before the questions. */
    data object Reading : LessonPhase
    data object Items : LessonPhase
    data object Done : LessonPhase
}

class LessonViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    /** "listening" or "speaking" for a practice drill (route drill/{kind}); null for a normal lesson. */
    private val drillKind: String? = savedStateHandle["kind"]
    val isDrill = drillKind != null
    val lessonId: String = savedStateHandle["lessonId"] ?: "drill.$drillKind"
    private val db = DatabaseProvider.getDatabase(application)

    var phase by mutableStateOf<LessonPhase>(LessonPhase.Loading); private set
    var lesson by mutableStateOf<Lesson?>(null); private set
    val unitId: String = unitIdOf(lessonId)
    var ttsLocale = "nl-NL"; private set

    /** The words of a vocab lesson, for the intro cards. */
    var introEntries by mutableStateOf<List<LexEntry>>(emptyList()); private set

    /** Reading lessons: finds the lexicon entry behind a tapped word. */
    private var wordLookup: WordLookup? = null
    val canSend: Boolean = canSendToWidget(application)

    private var courseId = ""
    private var introDone = false
    private var dialogueDone = false
    private var readingDone = false
    /** Generated vocab items -> the words they test, so a wrong answer schedules the right review cards. */
    private var lexOf: Map<Item, List<String>> = emptyMap()
    private lateinit var queue: LessonQueue

    /** Changes every time an item is shown (a re-queued item shows twice), so the views reset their state. */
    var step by mutableIntStateOf(0); private set
    var currentItem by mutableStateOf<Item?>(null); private set

    /** Where the current item sits in the lesson file (-1 outside the exercises); reported with a flag. */
    private var itemIndex = -1

    /** What the learner has picked or typed, not checked yet. A choice is its option index as text. */
    var pending by mutableStateOf<String?>(null)
    var result by mutableStateOf<ItemResult?>(null); private set
    var progressDone by mutableIntStateOf(0); private set
    var progressTotal by mutableIntStateOf(0); private set

    var score by mutableIntStateOf(0); private set
    var mistakes by mutableIntStateOf(0); private set
    var nextLesson by mutableStateOf<Lesson?>(null); private set

    /** Speaking items: tries used so far and the last close-but-not-good guess (shown so the learner can retry). */
    var speechTries by mutableIntStateOf(0); private set
    var speechTry by mutableStateOf<Match?>(null); private set

    init {
        viewModelScope.launch {
            val id = CoursePrefs.selectedCourse(application)
            val found = if (drillKind != null) null else id?.let { CourseRepository.lesson(application, it, lessonId) }
            if (id == null || (found == null && drillKind == null)) {
                phase = LessonPhase.Failed("This lesson isn't available.")
                return@launch
            }
            courseId = id
            ttsLocale = CourseRepository.courses(application).firstOrNull { it.id == id }?.ttsLocale ?: ttsLocale
            if (drillKind != null) return@launch startDrill(drillKind)
            if (found == null) return@launch
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
                if (found.kind == LessonKind.READING && found.text != null) {
                    wordLookup = WordLookup(CourseRepository.lexicon(application, id).values)
                }
            }
            if (found.explain.isEmpty()) startItems() else phase = LessonPhase.Explain
        }
    }

    /** Ten listening or speaking items from finished lessons, topped up with the example sentences of known words. */
    private suspend fun startDrill(kind: String) {
        val app = getApplication<Application>()
        val lexicon = CourseRepository.lexicon(app, courseId)
        val done = db.lessonProgressDao().getAll(courseId).first().mapNotNull { CourseRepository.lesson(app, courseId, it.lessonId) }
        val lessonItems = done.flatMap { it.items }
        val items = if (kind == "listening") {
            listeningDrill(lessonItems, lexicon.values.toList(), Random)
        } else {
            val reviewed = db.reviewCardDao().lexCardIds(courseId).mapNotNull { lexicon[it.removePrefix("lex:")] }
            speakingDrill(lessonItems, reviewed.ifEmpty { lexicon.values.toList() }, Random)
        }
        if (items.isEmpty()) {
            phase = LessonPhase.Failed("There is nothing to practise yet. Finish a lesson first.")
            return
        }
        lesson = Lesson(
            id = lessonId, title = if (kind == "listening") "Listening drill" else "Speaking drill",
            kind = if (kind == "listening") LessonKind.LISTENING else LessonKind.SPEAKING, items = items.take(DRILL_SIZE)
        )
        startItems()
    }

    fun startItems() {
        val l = lesson ?: return
        if (l.kind == LessonKind.VOCAB && !introDone) {
            introDone = true
            phase = LessonPhase.Intro
            return
        }
        if (l.dialogue.isNotEmpty() && !dialogueDone && (l.kind == LessonKind.LISTENING || l.kind == LessonKind.SPEAKING)) {
            dialogueDone = true
            phase = if (l.kind == LessonKind.LISTENING) LessonPhase.Dialogue else LessonPhase.Roleplay
            return
        }
        if (l.kind == LessonKind.READING && l.text != null && !readingDone) {
            readingDone = true
            phase = LessonPhase.Reading
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
        itemIndex = index
        step++
        pending = null
        result = null
        speechTries = 0
        speechTry = null
        progressDone = queue.progressDone
        progressTotal = queue.progressTotal
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

    /**
     * The recogniser guesses for a speak item. Close but not good enough (0.5 to 0.8) allows another try, up to
     * [SPEECH_TRIES]; clearly right or clearly wrong is graded at once.
     */
    fun submitSpeech(guesses: List<String>) {
        val item = currentItem as? Item.Speak ?: return
        if (result != null || guesses.isEmpty()) return
        val match = best(guesses, item.nl)
        speechTries++
        if (match.score >= SPEECH_CORRECT || match.score < SPEECH_ALMOST || speechTries >= SPEECH_TRIES) {
            record(gradeSpeech(match, item.nl))
        } else {
            speechTry = match
        }
    }

    /** Shadowing (no recogniser): the learner grades themselves. */
    fun selfGrade(good: Boolean) {
        val item = currentItem as? Item.Speak ?: return
        record(gradeSelf(good, item.nl))
    }

    /** An open speaking prompt or a writing task is finished, not graded: it counts as done and the next item shows. */
    fun finishUnscored() {
        if (currentItem !is Item.OpenPrompt && currentItem !is Item.Write) return
        queue.answer(true)
        progressDone = queue.progressDone
        progressTotal = queue.progressTotal
        viewModelScope.launch { db.dailyStatsDao().addExercises(LocalDate.now().toString(), 1) }
        showNext()
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

    /** One draft per writing task: the lesson id, plus the item position when a lesson has several tasks. */
    val draftKey: String get() = if ((lesson?.items?.count { it is Item.Write } ?: 0) > 1) "$lessonId#$itemIndex" else lessonId

    fun lookup(token: String) = wordLookup?.lookup(token).orEmpty()

    /** Returns (added, already there), like the dictionary. */
    suspend fun addToWords(entry: LexEntry) = sendToWidget(getApplication(), listOf(entry))

    /**
     * Saves a reported problem for Settings -> Reported problems. Items of vocab lessons and drills are made on the
     * spot, so their position means nothing later: the expected answer goes into the text instead.
     */
    fun flag(type: FlagType, text: String) {
        val item = currentItem.takeIf { phase == LessonPhase.Items }
        val generated = isDrill || lesson?.kind == LessonKind.VOCAB
        val detail = if (generated && item != null) "[${expectedAnswer(item)}] " else ""
        CoursePrefs.addFlag(getApplication(), flagLine(lessonId, if (item == null) -1 else itemIndex, type, detail + text))
    }

    /** Skips an item (speaking in a quiet place, or an item type that arrives in a later stage); it counts neither way. */
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
        // A drill is practice only: no lesson progress, no review cards.
        if (isDrill) return
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
