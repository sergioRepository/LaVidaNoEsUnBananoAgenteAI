package com.lavidanoesunbanano.domain.model

enum class InterventionReason(val value: String) {
    TRIGGER_APP_IN_RISK_WINDOW("TRIGGER_APP_IN_RISK_WINDOW"),
    SESSION_THRESHOLD_EXCEEDED("SESSION_THRESHOLD_EXCEEDED")
}
