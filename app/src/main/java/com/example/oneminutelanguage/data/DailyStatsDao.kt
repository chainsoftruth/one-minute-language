package com.example.oneminutelanguage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(stats: DailyStatsEntity)

    @Update
    suspend fun update(stats: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsForDate(date: String): DailyStatsEntity?

    @Query("SELECT widgetViewCount FROM daily_stats WHERE date = :date")
    fun getViewCountForDate(date: String): Flow<Int?>

    @Query("UPDATE daily_stats SET widgetViewCount = widgetViewCount + 1 WHERE date = :date")
    suspend fun incrementViewCountRaw(date: String)

    suspend fun incrementViewCount(date: String) {
        insertIfNotExists(DailyStatsEntity(date = date, widgetViewCount = 0))
        incrementViewCountRaw(date)
    }
}