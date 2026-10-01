package com.example.oneminutelanguage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val date: String,

    val widgetViewCount: Int = 0,

    @ColumnInfo(defaultValue = "0")
    val exercisesDone: Int = 0
)
