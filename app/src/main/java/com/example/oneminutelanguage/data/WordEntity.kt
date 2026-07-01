package com.example.oneminutelanguage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val language1Word: String,
    val language2Word: String,
    val dateAdded: Long
)