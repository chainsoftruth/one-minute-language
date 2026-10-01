package com.example.oneminutelanguage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewCardDao {
    @Upsert
    suspend fun upsert(card: ReviewCardEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(card: ReviewCardEntity)

    @Query("SELECT * FROM review_cards WHERE courseId = :courseId AND dueAt <= :now ORDER BY dueAt LIMIT :limit")
    suspend fun due(courseId: String, now: Long, limit: Int): List<ReviewCardEntity>

    @Query("SELECT COUNT(*) FROM review_cards WHERE courseId = :courseId AND dueAt <= :now")
    fun dueCount(courseId: String, now: Long): Flow<Int>

    /** When the next card falls due (null = no cards at all). */
    @Query("SELECT MIN(dueAt) FROM review_cards WHERE courseId = :courseId")
    suspend fun nextDueAt(courseId: String): Long?

    @Query("SELECT COUNT(*) FROM review_cards WHERE courseId = :courseId AND intervalDays >= 21")
    fun masteredCount(courseId: String): Flow<Int>

    @Query("DELETE FROM review_cards WHERE cardId = :cardId")
    suspend fun delete(cardId: String)
}
