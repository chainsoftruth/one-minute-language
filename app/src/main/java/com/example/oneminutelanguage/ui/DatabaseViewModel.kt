package com.example.oneminutelanguage.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oneminutelanguage.course.backfillTopics
import com.example.oneminutelanguage.course.loadTopicTitles
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.TopicCount
import com.example.oneminutelanguage.data.WordEntity
import com.example.oneminutelanguage.widget.WidgetUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseViewModel(application: Application) : AndroidViewModel(application) {
    private val wordDao = DatabaseProvider.getDatabase(application).wordDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /** The topic chip that is selected; null = all words. */
    private val _topic = MutableStateFlow<String?>(null)
    val topic: StateFlow<String?> = _topic

    val words: StateFlow<List<WordEntity>> = combine(_searchQuery, _topic) { query, topic -> query to topic }
        .flatMapLatest { (query, topic) -> wordDao.searchWords(query, topic) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Topics that have words, with how many are switched on. */
    val topics: StateFlow<List<TopicCount>> = wordDao.getTopicCounts()
        .map { counts -> counts.sortedBy { it.topic } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var topicTitles by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    init {
        viewModelScope.launch {
            topicTitles = loadTopicTitles(getApplication())
            backfillTopics(getApplication())
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectTopic(topic: String?) {
        _topic.value = topic
    }

    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            wordDao.deleteWord(word)

            WidgetUpdater.refreshWidget(getApplication())
        }
    }

    /** Undo for [deleteWord]: re-inserts the entity with its original id. */
    fun restoreWord(word: WordEntity) {
        viewModelScope.launch {
            wordDao.insertWord(word)

            WidgetUpdater.refreshWidget(getApplication())
        }
    }

    fun updateWord(word: WordEntity) {
        viewModelScope.launch {
            wordDao.updateWord(word)

            WidgetUpdater.refreshWidget(getApplication())
        }
    }

    fun setWordEnabled(word: WordEntity, enabled: Boolean) {
        viewModelScope.launch {
            wordDao.setWordEnabled(word.id, enabled)

            WidgetUpdater.refreshWidget(getApplication())
        }
    }

    /** Applies to the selected topic, or to every word when no topic is selected. */
    fun setAllWordsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val topic = _topic.value
            if (topic == null) wordDao.setAllWordsEnabled(enabled) else wordDao.setTopicEnabled(topic, enabled)

            WidgetUpdater.refreshWidget(getApplication())
        }
    }
}
