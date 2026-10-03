package com.example.oneminutelanguage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val language1Word: String,
    val language2Word: String,
    val dateAdded: Long,

    @ColumnInfo(defaultValue = "0")
    val isDefault: Boolean = false,

    @ColumnInfo(defaultValue = "1")
    val isEnabled: Boolean = true,

    /** First correct quiz answer; set = the word has left the widget's focus set. */
    val learnedAt: Long? = null,

    /** Course topic id (`food_drink`); null for words the lexicon doesn't know. */
    val topic: String? = null
)

/** One row of the topic list: how many words a topic has and how many of them are switched on. */
data class TopicCount(val topic: String, val total: Int, val enabled: Int)
