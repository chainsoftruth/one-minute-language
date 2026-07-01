package com.example.oneminutelanguage.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.oneminutelanguage.data.DatabaseProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val wordDao = database.wordDao()
    private val statsDao = database.dailyStatsDao()

    val totalWordsCount: Flow<Int> = wordDao.getWordCount()
    
    val todayViewCount: Flow<Int> = statsDao.getViewCountForDate(LocalDate.now().toString())
        .map { it ?: 0 }
}
