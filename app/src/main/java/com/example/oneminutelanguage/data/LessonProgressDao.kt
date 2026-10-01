package com.example.oneminutelanguage.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonProgressDao {
    @Upsert
    suspend fun upsert(entity: LessonProgressEntity)

    @Query("SELECT * FROM lesson_progress WHERE courseId = :courseId")
    fun getAll(courseId: String): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun get(lessonId: String): LessonProgressEntity?
}
