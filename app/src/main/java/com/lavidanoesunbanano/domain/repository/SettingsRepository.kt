package com.lavidanoesunbanano.domain.repository

import com.lavidanoesunbanano.domain.model.RiskSchedule
import com.lavidanoesunbanano.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<UserSettings>
    suspend fun updateMonitoringEnabled(enabled: Boolean)
    suspend fun updateSessionThresholdMinutes(minutes: Int)
    suspend fun updateRiskSchedule(schedule: RiskSchedule)
    suspend fun updateAiResponsesEnabled(enabled: Boolean)
    suspend fun updateMaxHourlyInterventions(max: Int)
    suspend fun pauseMonitoringForOneHour(currentEpochMilli: Long)
    suspend fun clearPause()
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun resetAllData(): String
}
