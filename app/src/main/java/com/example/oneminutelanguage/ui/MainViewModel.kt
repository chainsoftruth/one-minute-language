package com.example.oneminutelanguage.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.course.STREAK_MIN_EXERCISES
import com.example.oneminutelanguage.course.streak
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.ui.learn.LearnState
import com.example.oneminutelanguage.ui.learn.dueCountFlow
import com.example.oneminutelanguage.ui.learn.learnStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val wordDao = database.wordDao()
    private val statsDao = database.dailyStatsDao()

    val totalWordsCount: Flow<Int> = wordDao.getWordCount()

    val todayViewCount: Flow<Int> = statsDao.getViewCountForDate(LocalDate.now().toString())
        .map { it ?: 0 }

    val learnState: StateFlow<LearnState> = learnStateFlow(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnState.Loading)

    val dueCount: StateFlow<Int> = dueCountFlow(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Exercises done today and the current streak; re-emits on every answered exercise. */
    val exercisesAndStreak: StateFlow<Pair<Int, Int>> = statsDao.getExercisesForDate(LocalDate.now().toString())
        .map { done ->
            val today = LocalDate.now()
            val active = statsDao.activeDays(STREAK_MIN_EXERCISES).map(LocalDate::parse)
            (done ?: 0) to streak(active, today)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0 to 0)
}
