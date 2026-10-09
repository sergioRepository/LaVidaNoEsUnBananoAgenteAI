package org.platica.demo.data.local.room

import org.platica.demo.data.local.room.dao.DailyStatsDao
import org.platica.demo.data.local.room.dao.InterventionEventDao
import org.platica.demo.data.local.room.entity.DailyStatsEntity
import org.platica.demo.data.local.room.entity.InterventionEventEntity
import org.platica.demo.domain.model.InterventionDecision
import org.platica.demo.domain.model.InterventionReason
import org.platica.demo.domain.model.InterventionRecord
import org.platica.demo.domain.repository.InterventionRepository
import org.platica.demo.domain.time.Clock
import org.platica.demo.domain.time.LogicalDateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionRepositoryImpl @Inject constructor(
    private val eventDao: InterventionEventDao,
    private val statsDao: DailyStatsDao,
    private val clock: Clock
) : InterventionRepository {

    override suspend fun recordIntervention(
        record: InterventionRecord,
        emotion: String?,
        userText: String?
    ): Long {
        val entity = InterventionEventEntity(
            timestampEpochMilli = record.timestamp.toEpochMilli(),
            packageName = record.packageName,
            reason = record.reason.value,
            emotion = emotion,
            userText = userText,
            decision = record.decision.value,
            sessionDurationSeconds = record.sessionDurationSeconds
        )
        val id = eventDao.insertEvent(entity)

        // Actualizar estadísticas del día lógico
        val logicalDateStr = LogicalDateUtils.getLogicalDate(record.timestamp, clock.zoneId()).toString()
        val currentStats = statsDao.getStatsForDateDirect(logicalDateStr) ?: DailyStatsEntity(logicalDate = logicalDateStr)

        val updatedStats = currentStats.copy(
            interventionCount = currentStats.interventionCount + 1,
            exitCount = currentStats.exitCount + if (record.decision == InterventionDecision.EXIT) 1 else 0,
            continueCount = currentStats.continueCount + if (record.decision == InterventionDecision.CONTINUE) 1 else 0,
            snoozeCount = currentStats.snoozeCount + if (record.decision == InterventionDecision.SNOOZE) 1 else 0
        )
        statsDao.upsertStats(updatedStats)

        return id
    }

    override fun getRecentInterventions(limit: Int): Flow<List<InterventionRecord>> {
        return eventDao.getRecentEvents(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getInterventionsForApp(packageName: String): List<InterventionRecord> {
        return eventDao.getEventsForApp(packageName).map { it.toDomain() }
    }

    override suspend fun getInterventionsGlobalSince(sinceEpochMilli: Long): List<InterventionRecord> {
        val now = clock.now().toEpochMilli()
        return eventDao.getEventsBetweenDirect(sinceEpochMilli, now + 1000).map { it.toDomain() }
    }

    override fun getInterventionsBetween(
        startEpochMilli: Long,
        endEpochMilli: Long
    ): Flow<List<InterventionRecord>> {
        return eventDao.getEventsBetween(startEpochMilli, endEpochMilli).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getInterventionsBetweenDirect(
        startEpochMilli: Long,
        endEpochMilli: Long
    ): List<InterventionRecord> {
        return eventDao.getEventsBetweenDirect(startEpochMilli, endEpochMilli).map { it.toDomain() }
    }

    override suspend fun clearAll() {
        eventDao.clearAll()
        statsDao.clearAll()
    }

    private fun InterventionEventEntity.toDomain(): InterventionRecord {
        val reasonEnum = try {
            InterventionReason.valueOf(reason)
        } catch (_: Exception) {
            InterventionReason.TRIGGER_APP_IN_RISK_WINDOW
        }
        val decisionEnum = try {
            InterventionDecision.valueOf(decision)
        } catch (_: Exception) {
            InterventionDecision.SNOOZE
        }
        return InterventionRecord(
            id = id,
            timestamp = Instant.ofEpochMilli(timestampEpochMilli),
            packageName = packageName,
            reason = reasonEnum,
            decision = decisionEnum,
            sessionDurationSeconds = sessionDurationSeconds
        )
    }
}
