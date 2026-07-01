package com.example.oneminutelanguage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val date: String, // format: "yyyy-MM-dd"

    val widgetViewCount: Int = 0
)