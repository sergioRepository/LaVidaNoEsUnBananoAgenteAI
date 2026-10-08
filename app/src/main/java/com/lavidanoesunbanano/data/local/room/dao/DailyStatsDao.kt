package com.lavidanoesunbanano.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lavidanoesunbanano.data.local.room.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats WHERE logicalDate = :logicalDate")
    fun getStatsForDate(logicalDate: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE logicalDate = :logicalDate")
    suspend fun getStatsForDateDirect(logicalDate: String): DailyStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: DailyStatsEntity)

    @Query("DELETE FROM daily_stats")
    suspend fun clearAll()
}
