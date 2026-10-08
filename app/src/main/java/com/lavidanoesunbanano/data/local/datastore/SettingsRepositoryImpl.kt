package com.lavidanoesunbanano.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.lavidanoesunbanano.domain.model.RiskSchedule
import com.lavidanoesunbanano.domain.model.UserSettings
import com.lavidanoesunbanano.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun getSettings(): Flow<UserSettings> = dataStore.data.map { prefs ->
        val userId = prefs[PreferencesKeys.USER_ID] ?: run {
            // Generar UUID diferido si aún no existe
            val newId = UUID.randomUUID().toString()
            newId
        }
        val monitoring = prefs[PreferencesKeys.MONITORING_ENABLED] ?: false
        val threshold = prefs[PreferencesKeys.SESSION_THRESHOLD_MINUTES] ?: 15
        val startH = prefs[PreferencesKeys.RISK_START_HOUR] ?: 22
        val startM = prefs[PreferencesKeys.RISK_START_MINUTE] ?: 0
        val endH = prefs[PreferencesKeys.RISK_END_HOUR] ?: 2
        val endM = prefs[PreferencesKeys.RISK_END_MINUTE] ?: 0
        val aiEnabled = prefs[PreferencesKeys.AI_RESPONSES_ENABLED] ?: false
        val maxHourly = prefs[PreferencesKeys.MAX_HOURLY_INTERVENTIONS] ?: 4
        val pauseUntil = prefs[PreferencesKeys.GLOBAL_PAUSE_UNTIL]
        val onboarding = prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false

        UserSettings(
            userId = userId,
            monitoringEnabled = monitoring,
            sessionThresholdMinutes = threshold,
            riskSchedule = RiskSchedule(startH, startM, endH, endM),
            aiResponsesEnabled = aiEnabled,
            maxHourlyInterventions = maxHourly,
            globalPauseUntilEpochMilli = pauseUntil,
            onboardingCompleted = onboarding
        )
    }

    override suspend fun updateMonitoringEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.MONITORING_ENABLED] = enabled }
    }

    override suspend fun updateSessionThresholdMinutes(minutes: Int) {
        dataStore.edit { it[PreferencesKeys.SESSION_THRESHOLD_MINUTES] = minutes }
    }

    override suspend fun updateRiskSchedule(schedule: RiskSchedule) {
        dataStore.edit {
            it[PreferencesKeys.RISK_START_HOUR] = schedule.startHour
            it[PreferencesKeys.RISK_START_MINUTE] = schedule.startMinute
            it[PreferencesKeys.RISK_END_HOUR] = schedule.endHour
            it[PreferencesKeys.RISK_END_MINUTE] = schedule.endMinute
        }
    }

    override suspend fun updateAiResponsesEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.AI_RESPONSES_ENABLED] = enabled }
    }

    override suspend fun updateMaxHourlyInterventions(max: Int) {
        dataStore.edit { it[PreferencesKeys.MAX_HOURLY_INTERVENTIONS] = max }
    }

    override suspend fun pauseMonitoringForOneHour(currentEpochMilli: Long) {
        val oneHourLater = currentEpochMilli + (60 * 60 * 1000L)
        dataStore.edit { it[PreferencesKeys.GLOBAL_PAUSE_UNTIL] = oneHourLater }
    }

    override suspend fun clearPause() {
        dataStore.edit { it.remove(PreferencesKeys.GLOBAL_PAUSE_UNTIL) }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun resetAllData(): String {
        val newUserId = UUID.randomUUID().toString()
        dataStore.edit { prefs ->
            prefs.clear()
            prefs[PreferencesKeys.USER_ID] = newUserId
        }
        return newUserId
    }
}
