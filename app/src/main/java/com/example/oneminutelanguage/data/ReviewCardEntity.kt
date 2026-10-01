package com.example.oneminutelanguage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_cards")
data class ReviewCardEntity(
    @PrimaryKey val cardId: String,   // "lex:<lexId>" or "item:<lessonId>#<itemHash>"
    val courseId: String,
    val dueAt: Long,
    val intervalDays: Double,
    val ease: Double,
    val reps: Int,
    val lapses: Int
)
