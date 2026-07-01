package com.example.oneminutelanguage.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WordEntity::class, DailyStatsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun dailyStatsDao(): DailyStatsDao
}