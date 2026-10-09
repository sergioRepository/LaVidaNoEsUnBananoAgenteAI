package org.platica.demo.domain.repository

import org.platica.demo.domain.session.SessionSnapshot

interface SessionRepository {
    suspend fun getActiveSession(): SessionSnapshot?
    suspend fun saveActiveSession(session: SessionSnapshot)
    suspend fun clearActiveSession(packageName: String)
    suspend fun clearAll()
}
