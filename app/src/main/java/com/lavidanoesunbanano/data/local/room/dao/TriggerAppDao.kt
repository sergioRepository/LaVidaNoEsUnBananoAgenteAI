package com.lavidanoesunbanano.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lavidanoesunbanano.data.local.room.entity.TriggerAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerAppDao {
    @Query("SELECT * FROM trigger_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<TriggerAppEntity>>

    @Query("SELECT * FROM trigger_apps WHERE isTrigger = 1 ORDER BY appName ASC")
    fun getTriggerApps(): Flow<List<TriggerAppEntity>>

    @Query("SELECT packageName FROM trigger_apps WHERE isTrigger = 1")
    suspend fun getTriggerPackageNames(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM trigger_apps WHERE packageName = :packageName AND isTrigger = 1)")
    suspend fun isTriggerApp(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApps(apps: List<TriggerAppEntity>)

    @Query("UPDATE trigger_apps SET isTrigger = :isTrigger WHERE packageName = :packageName")
    suspend fun setTrigger(packageName: String, isTrigger: Boolean)

    @Query("DELETE FROM trigger_apps")
    suspend fun clearAll()
}
