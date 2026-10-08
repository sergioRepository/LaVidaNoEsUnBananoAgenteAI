package com.lavidanoesunbanano.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lavidanoesunbanano.data.local.room.entity.ActiveSessionEntity

@Dao
interface ActiveSessionDao {
    @Query("SELECT * FROM active_sessions LIMIT 1")
    suspend fun getActiveSession(): ActiveSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: ActiveSessionEntity)

    @Query("DELETE FROM active_sessions WHERE packageName = :packageName")
    suspend fun deleteSession(packageName: String)

    @Query("DELETE FROM active_sessions")
    suspend fun clearAll()
}
