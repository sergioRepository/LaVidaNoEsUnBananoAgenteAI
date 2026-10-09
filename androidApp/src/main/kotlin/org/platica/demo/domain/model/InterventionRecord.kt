package org.platica.demo.domain.model

import java.time.Instant

data class InterventionRecord(
    val id: Long = 0,
    val timestamp: Instant,
    val packageName: String,
    val reason: InterventionReason,
    val decision: InterventionDecision,
    val sessionDurationSeconds: Long
)
