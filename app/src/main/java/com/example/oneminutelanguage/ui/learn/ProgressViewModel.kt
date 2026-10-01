package com.example.oneminutelanguage.ui.learn

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.CourseRepository
import com.example.oneminutelanguage.course.LessonKind
import com.example.oneminutelanguage.course.PathLevel
import com.example.oneminutelanguage.course.STREAK_MIN_EXERCISES
import com.example.oneminutelanguage.course.activityStart
import com.example.oneminutelanguage.course.longestStreak
import com.example.oneminutelanguage.course.skillMeters
import com.example.oneminutelanguage.course.streak
import com.example.oneminutelanguage.data.DatabaseProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class ProgressData(
    val path: List<PathLevel>,
    val completed: Set<String>,
    /** Lessons marked as skipped by the placement test (attempts = 0). */
    val skipped: Set<String>,
    val skills: Map<LessonKind, Int?>,
    val mastered: Int,
    val lexiconSize: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    /** Exercises per day for the activity grid. */
    val exercises: Map<LocalDate, Int>
)

sealed interface ProgressState {
    data object Loading : ProgressState
    data object NoCourse : ProgressState
    class Failed(val message: String) : ProgressState
    class Ready(val data: ProgressData) : ProgressState
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)

    val state: StateFlow<ProgressState> = learnStateFlow(application)
        .flatMapLatest { learn -> if (learn is LearnState.Ready) ready(learn) else flowOf(when (learn) { is LearnState.NoCourse -> ProgressState.NoCourse; is LearnState.Failed -> ProgressState.Failed(learn.message); else -> ProgressState.Loading }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressState.Loading)

    private fun ready(learn: LearnState.Ready): Flow<ProgressState> =
        combine(db.reviewCardDao().masteredCount(learn.course.id), db.dailyStatsDao().getExercisesForDate(LocalDate.now().toString())) { mastered, _ ->
            val app = getApplication<Application>()
            val today = LocalDate.now()
            val active = db.dailyStatsDao().activeDays(STREAK_MIN_EXERCISES).map(LocalDate::parse)
            val days = db.dailyStatsDao().since(activityStart(today).toString()).associate { LocalDate.parse(it.date) to it.exercisesDone }
            val scores = learn.progress.filterValues { it.attempts > 0 }.mapValues { it.value.bestScore }
            ProgressState.Ready(
                ProgressData(
                    path = learn.path,
                    completed = learn.progress.keys,
                    skipped = learn.progress.filterValues { it.attempts == 0 }.keys,
                    skills = skillMeters(learn.path, scores),
                    mastered = mastered,
                    lexiconSize = CourseRepository.lexicon(app, learn.course.id).size,
                    currentStreak = streak(active, today),
                    longestStreak = longestStreak(active),
                    exercises = days
                )
            )
        }

    /** Ticked "I can…" statements. */
    var canDo by mutableStateOf(CoursePrefs.canDo(application)); private set

    fun toggleCanDo(id: String, on: Boolean) {
        CoursePrefs.setCanDo(getApplication(), id, on)
        canDo = CoursePrefs.canDo(getApplication())
    }
}
