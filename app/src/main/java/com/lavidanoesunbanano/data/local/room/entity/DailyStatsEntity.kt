package com.lavidanoesunbanano.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val logicalDate: String, // YYYY-MM-DD
    val totalTriggerTimeSeconds: Long = 0,
    val interventionCount: Int = 0,
    val exitCount: Int = 0,
    val continueCount: Int = 0,
    val snoozeCount: Int = 0
)
