package com.example.oneminutelanguage.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert
    suspend fun insertWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("SELECT * FROM words ORDER BY dateAdded DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    fun getWordCount(): Flow<Int>

    @Query("SELECT * FROM words ORDER BY dateAdded DESC LIMIT 1")
    suspend fun getLatestWord(): WordEntity?

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): WordEntity?

    @Query("SELECT * FROM words WHERE id != :excludeId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordExcluding(excludeId: Long): WordEntity?
}