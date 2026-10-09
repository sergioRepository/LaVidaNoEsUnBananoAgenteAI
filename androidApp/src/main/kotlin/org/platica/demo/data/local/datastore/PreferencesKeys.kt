package org.platica.demo.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
    val SESSION_THRESHOLD_MINUTES = intPreferencesKey("session_threshold_minutes")
    val RISK_START_HOUR = intPreferencesKey("risk_start_hour")
    val RISK_START_MINUTE = intPreferencesKey("risk_start_minute")
    val RISK_END_HOUR = intPreferencesKey("risk_end_hour")
    val RISK_END_MINUTE = intPreferencesKey("risk_end_minute")
    val AI_RESPONSES_ENABLED = booleanPreferencesKey("ai_responses_enabled")
    val MAX_HOURLY_INTERVENTIONS = intPreferencesKey("max_hourly_interventions")
    val GLOBAL_PAUSE_UNTIL = longPreferencesKey("global_pause_until")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}
