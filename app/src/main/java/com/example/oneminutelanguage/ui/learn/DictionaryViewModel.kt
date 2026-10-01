package com.example.oneminutelanguage.ui.learn

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.CourseRepository
import com.example.oneminutelanguage.course.LexEntry
import com.example.oneminutelanguage.course.Topic
import com.example.oneminutelanguage.course.canSendToWidget
import com.example.oneminutelanguage.course.sendToWidget
import kotlinx.coroutines.launch

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {
    var entries by mutableStateOf<List<LexEntry>>(emptyList()); private set
    var topics by mutableStateOf<List<Topic>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var ttsLocale = "nl-NL"; private set

    var query by mutableStateOf("")
    var level by mutableStateOf<String?>(null)
    var topic by mutableStateOf<String?>(null)

    val canSend: Boolean = canSendToWidget(application)

    init {
        viewModelScope.launch {
            // The dictionary is also reachable from the Words tab before a course is picked: default to Dutch.
            val id = CoursePrefs.selectedCourse(application) ?: "nl"
            ttsLocale = CourseRepository.courses(application).firstOrNull { it.id == id }?.ttsLocale ?: ttsLocale
            topics = CourseRepository.outline(application, id).topics
            entries = CourseRepository.lexicon(application, id).values.toList()
            loading = false
        }
    }

    /** Returns (added, already there). */
    suspend fun addToWords(entry: LexEntry) = sendToWidget(getApplication(), listOf(entry))
}
