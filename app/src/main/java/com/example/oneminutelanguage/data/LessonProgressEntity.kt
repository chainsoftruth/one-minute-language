package com.example.oneminutelanguage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val courseId: String,
    val completedAt: Long,
    val bestScore: Int,        // 0..100
    val attempts: Int
)
