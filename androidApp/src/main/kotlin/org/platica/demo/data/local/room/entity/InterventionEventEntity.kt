package org.platica.demo.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intervention_events")
data class InterventionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMilli: Long,
    val packageName: String,
    val reason: String,
    val emotion: String?,
    val userText: String?,
    val decision: String,
    val sessionDurationSeconds: Long
)
