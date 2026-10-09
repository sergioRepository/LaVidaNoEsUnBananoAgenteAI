package org.platica.demo.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_sessions")
data class ActiveSessionEntity(
    @PrimaryKey val packageName: String,
    val startTimeEpochMilli: Long,
    val accumulatedDurationSeconds: Long,
    val lastUpdatedEpochMilli: Long
)
