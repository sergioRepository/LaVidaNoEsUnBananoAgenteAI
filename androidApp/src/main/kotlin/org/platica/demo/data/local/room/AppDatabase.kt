package org.platica.demo.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import org.platica.demo.data.local.room.dao.ActiveSessionDao
import org.platica.demo.data.local.room.dao.DailyStatsDao
import org.platica.demo.data.local.room.dao.InterventionEventDao
import org.platica.demo.data.local.room.dao.TriggerAppDao
import org.platica.demo.data.local.room.entity.ActiveSessionEntity
import org.platica.demo.data.local.room.entity.DailyStatsEntity
import org.platica.demo.data.local.room.entity.InterventionEventEntity
import org.platica.demo.data.local.room.entity.TriggerAppEntity

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
