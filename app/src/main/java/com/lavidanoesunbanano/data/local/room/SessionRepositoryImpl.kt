package com.lavidanoesunbanano.data.local.room

import com.lavidanoesunbanano.data.local.room.dao.ActiveSessionDao
import com.lavidanoesunbanano.data.local.room.entity.ActiveSessionEntity
import com.lavidanoesunbanano.domain.repository.SessionRepository
import com.lavidanoesunbanano.domain.session.SessionSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val activeSessionDao: ActiveSessionDao
) : SessionRepository {

    override suspend fun getActiveSession(): SessionSnapshot? {
        val entity = activeSessionDao.getActiveSession() ?: return null
        return SessionSnapshot(
            packageName = entity.packageName,
            startTimeEpochMilli = entity.startTimeEpochMilli,
            accumulatedDurationSeconds = entity.accumulatedDurationSeconds,
            lastUpdatedEpochMilli = entity.lastUpdatedEpochMilli
        )
    }

    override suspend fun saveActiveSession(session: SessionSnapshot) {
        val entity = ActiveSessionEntity(
            packageName = session.packageName,
            startTimeEpochMilli = session.startTimeEpochMilli,
            accumulatedDurationSeconds = session.accumulatedDurationSeconds,
            lastUpdatedEpochMilli = session.lastUpdatedEpochMilli
        )
        activeSessionDao.upsertSession(entity)
    }

    override suspend fun clearActiveSession(packageName: String) {
        activeSessionDao.deleteSession(packageName)
    }

    override suspend fun clearAll() {
        activeSessionDao.clearAll()
    }
}
