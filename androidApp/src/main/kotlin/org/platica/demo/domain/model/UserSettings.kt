package org.platica.demo.domain.model

data class UserSettings(
    val userId: String = "",
    val monitoringEnabled: Boolean = false,
    val sessionThresholdMinutes: Int = 15,
    val riskSchedule: RiskSchedule = RiskSchedule(),
    val aiResponsesEnabled: Boolean = false,
    val maxHourlyInterventions: Int = 4,
    val globalPauseUntilEpochMilli: Long? = null,
    val onboardingCompleted: Boolean = false
) {
    fun isGlobalPaused(currentEpochMilli: Long): Boolean {
        return globalPauseUntilEpochMilli != null && currentEpochMilli < globalPauseUntilEpochMilli
    }
}
