package com.example.oneminutelanguage.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert
    suspend fun insertWord(word: WordEntity)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("SELECT * FROM words")
    suspend fun getAllWordsOnce(): List<WordEntity>

    @Query("SELECT * FROM words WHERE isEnabled = 1")
    suspend fun getEnabledWordsOnce(): List<WordEntity>

    @Query("UPDATE words SET isEnabled = 0 WHERE id IN (:ids)")
    suspend fun disableWords(ids: List<Long>)

    @Query("SELECT * FROM words ORDER BY dateAdded DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    fun getWordCount(): Flow<Int>

    @Query("SELECT * FROM words ORDER BY dateAdded DESC LIMIT 1")
    suspend fun getLatestWord(): WordEntity?

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Long): WordEntity?

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getTotalWordCountOnce(): Int

    @Query("SELECT COUNT(*) FROM words WHERE isEnabled = 1 AND id != :excludeId")
    suspend fun getWordCountExcluding(excludeId: Long): Int

    @Query("SELECT * FROM words WHERE isEnabled = 1 LIMIT 1 OFFSET :offset")
    suspend fun getWordAtOffset(offset: Int): WordEntity?

    @Query("SELECT * FROM words WHERE isEnabled = 1 AND id != :excludeId LIMIT 1 OFFSET :offset")
    suspend fun getWordAtOffsetExcluding(excludeId: Long, offset: Int): WordEntity?

    @Query("UPDATE words SET isEnabled = :enabled WHERE id = :id")
    suspend fun setWordEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE words SET isEnabled = :enabled")
    suspend fun setAllWordsEnabled(enabled: Boolean)

    @Query("UPDATE words SET language1Word = language2Word, language2Word = language1Word")
    suspend fun swapLanguageColumns()

    @Query("SELECT EXISTS(SELECT 1 FROM words WHERE LOWER(language1Word) = LOWER(:word))")
    suspend fun wordExists(word: String): Boolean

    @Query("SELECT * FROM words WHERE LOWER(language1Word) = LOWER(:word) LIMIT 1")
    suspend fun findByLanguage1Word(word: String): WordEntity?

    @Query("DELETE FROM words WHERE isDefault = 1")
    suspend fun deleteAllDefaultWords()

    @Query(
        "SELECT * FROM words " +
            "WHERE language1Word LIKE '%' || :query || '%' " +
            "OR language2Word LIKE '%' || :query || '%' " +
            "ORDER BY dateAdded DESC"
    )
    fun searchWords(query: String): Flow<List<WordEntity>>
}
