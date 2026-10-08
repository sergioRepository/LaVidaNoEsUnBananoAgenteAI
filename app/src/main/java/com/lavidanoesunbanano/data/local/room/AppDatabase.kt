package com.lavidanoesunbanano.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lavidanoesunbanano.data.local.room.dao.ActiveSessionDao
import com.lavidanoesunbanano.data.local.room.dao.DailyStatsDao
import com.lavidanoesunbanano.data.local.room.dao.InterventionEventDao
import com.lavidanoesunbanano.data.local.room.dao.TriggerAppDao
import com.lavidanoesunbanano.data.local.room.entity.ActiveSessionEntity
import com.lavidanoesunbanano.data.local.room.entity.DailyStatsEntity
import com.lavidanoesunbanano.data.local.room.entity.InterventionEventEntity
import com.lavidanoesunbanano.data.local.room.entity.TriggerAppEntity

@Database(
    entities = [
        TriggerAppEntity::class,
        InterventionEventEntity::class,
        ActiveSessionEntity::class,
        DailyStatsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun triggerAppDao(): TriggerAppDao
    abstract fun interventionEventDao(): InterventionEventDao
    abstract fun activeSessionDao(): ActiveSessionDao
    abstract fun dailyStatsDao(): DailyStatsDao
}
