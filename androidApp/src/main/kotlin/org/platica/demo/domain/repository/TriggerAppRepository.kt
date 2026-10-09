package org.platica.demo.domain.repository

import org.platica.demo.domain.model.TriggerApp
import kotlinx.coroutines.flow.Flow

interface TriggerAppRepository {
    fun getApps(): Flow<List<TriggerApp>>
    suspend fun getTriggerPackageNames(): List<String>
    suspend fun isTriggerApp(packageName: String): Boolean
    suspend fun saveOrUpdateApps(apps: List<TriggerApp>)
    suspend fun setTrigger(packageName: String, isTrigger: Boolean)
    suspend fun clearAll()
}
