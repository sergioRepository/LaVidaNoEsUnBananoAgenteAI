package org.platica.demo.domain.model

import java.time.Instant
import java.time.ZoneId

data class RuleState(
    val currentPackage: String,
    val isTriggerApp: Boolean,
    val sessionDurationSeconds: Long,
    val currentTime: Instant,
    val zoneId: ZoneId,
    val isInterventionVisible: Boolean = false,
    val globalPauseUntil: Instant? = null,
    val riskSchedule: RiskSchedule = RiskSchedule(),
    val sessionThresholdMinutes: Int = 15,
    val maxHourlyInterventions: Int = 4,
    val recentInterventionsForApp: List<InterventionRecord> = emptyList(),
    val recentInterventionsGlobal: List<InterventionRecord> = emptyList()
)
