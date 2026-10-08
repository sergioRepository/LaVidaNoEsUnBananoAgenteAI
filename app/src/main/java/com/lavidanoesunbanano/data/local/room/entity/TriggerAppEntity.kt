package com.lavidanoesunbanano.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_apps")
data class TriggerAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isTrigger: Boolean,
    val category: String
)
