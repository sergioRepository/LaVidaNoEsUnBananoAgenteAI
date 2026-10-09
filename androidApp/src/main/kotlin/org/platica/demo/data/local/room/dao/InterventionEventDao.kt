package org.platica.demo.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.platica.demo.data.local.room.entity.InterventionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterventionEventDao {
    @Insert
    suspend fun insertEvent(event: InterventionEventEntity): Long

    @Query("SELECT * FROM intervention_events WHERE timestampEpochMilli >= :startEpochMilli AND timestampEpochMilli < :endEpochMilli ORDER BY timestampEpochMilli DESC")
    fun getEventsBetween(startEpochMilli: Long, endEpochMilli: Long): Flow<List<InterventionEventEntity>>

    @Query("SELECT * FROM intervention_events WHERE timestampEpochMilli >= :startEpochMilli AND timestampEpochMilli < :endEpochMilli ORDER BY timestampEpochMilli DESC")
    suspend fun getEventsBetweenDirect(startEpochMilli: Long, endEpochMilli: Long): List<InterventionEventEntity>

    @Query("SELECT * FROM intervention_events ORDER BY timestampEpochMilli DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<InterventionEventEntity>>

    @Query("SELECT * FROM intervention_events WHERE packageName = :packageName ORDER BY timestampEpochMilli DESC")
    suspend fun getEventsForApp(packageName: String): List<InterventionEventEntity>

    @Query("SELECT * FROM intervention_events ORDER BY timestampEpochMilli DESC")
    fun getAllEvents(): Flow<List<InterventionEventEntity>>

    @Query("DELETE FROM intervention_events")
    suspend fun clearAll()
}
