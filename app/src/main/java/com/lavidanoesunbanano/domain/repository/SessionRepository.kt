package com.lavidanoesunbanano.domain.repository

import com.lavidanoesunbanano.domain.session.SessionSnapshot

interface SessionRepository {
    suspend fun getActiveSession(): SessionSnapshot?
    suspend fun saveActiveSession(session: SessionSnapshot)
    suspend fun clearActiveSession(packageName: String)
    suspend fun clearAll()
}
