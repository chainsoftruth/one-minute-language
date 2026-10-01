package com.example.oneminutelanguage.ui.learn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.course.CourseInfo
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.CourseRepository
import com.example.oneminutelanguage.course.CourseUnit
import com.example.oneminutelanguage.course.LessonKind
import com.example.oneminutelanguage.course.canSendToWidget
import com.example.oneminutelanguage.course.sendToWidget
import com.example.oneminutelanguage.course.PathLevel
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.LessonProgressEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface LearnState {
    data object Loading : LearnState
    data object NoCourse : LearnState
    class Ready(
        val course: CourseInfo,
        val path: List<PathLevel>,
        /** lessonId -> best result. */
        val progress: Map<String, LessonProgressEntity>
    ) : LearnState
}

/** The selected course with its path and progress; re-emits when the course or any lesson result changes. */
@OptIn(ExperimentalCoroutinesApi::class)
fun learnStateFlow(app: Application): Flow<LearnState> {
    val progressDao = DatabaseProvider.getDatabase(app).lessonProgressDao()
    return CoursePrefs.selectedCourseFlow(app).flatMapLatest { id ->
        if (id == null) flowOf(LearnState.NoCourse)
        else progressDao.getAll(id).map { rows ->
            val course = CourseRepository.courses(app).firstOrNull { it.id == id && it.available }
            if (course == null) LearnState.NoCourse
            else LearnState.Ready(course, CourseRepository.path(app, id), rows.associateBy { it.lessonId })
        }
    }
}

class LearnViewModel(application: Application) : AndroidViewModel(application) {
    val state: StateFlow<LearnState> = learnStateFlow(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnState.Loading)
}

sealed interface UnitState {
    data object Loading : UnitState
    data object Missing : UnitState
    class Ready(val unit: CourseUnit, val progress: Map<String, LessonProgressEntity>) : UnitState
}

class UnitViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val unitId: String = checkNotNull(savedStateHandle["unitId"])

    val state: StateFlow<UnitState> = learnStateFlow(application)
        .map { learn ->
            val unit = (learn as? LearnState.Ready)?.path?.flatMap { it.units }?.firstOrNull { it.id == unitId }?.unit
            if (learn is LearnState.Ready && unit != null) UnitState.Ready(unit, learn.progress) else UnitState.Missing
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UnitState.Loading)

    /** Course words only fit "My words" when the app's pair is English -> Dutch. */
    val canSendToWidget: Boolean = canSendToWidget(application)

    /** Adds every word of the unit's vocab lessons to "My words". Returns (added, already there). */
    suspend fun sendWordsToWidget(unit: CourseUnit): Pair<Int, Int> {
        val app = getApplication<Application>()
        val courseId = CoursePrefs.selectedCourse(app) ?: return 0 to 0
        val lexicon = CourseRepository.lexicon(app, courseId)
        val entries = unit.lessons.filter { it.kind == LessonKind.VOCAB }.flatMap { it.vocab }.distinct().mapNotNull { lexicon[it] }
        return sendToWidget(app, entries)
    }
}
