package org.platica.demo.domain.repository

import org.platica.demo.domain.model.InterventionRecord
import kotlinx.coroutines.flow.Flow

interface InterventionRepository {
    suspend fun recordIntervention(
        record: InterventionRecord,
        emotion: String? = null,
        userText: String? = null
    ): Long

    fun getRecentInterventions(limit: Int): Flow<List<InterventionRecord>>
    suspend fun getInterventionsForApp(packageName: String): List<InterventionRecord>
    suspend fun getInterventionsGlobalSince(sinceEpochMilli: Long): List<InterventionRecord>
    fun getInterventionsBetween(startEpochMilli: Long, endEpochMilli: Long): Flow<List<InterventionRecord>>
    suspend fun getInterventionsBetweenDirect(startEpochMilli: Long, endEpochMilli: Long): List<InterventionRecord>
    suspend fun clearAll()
}
