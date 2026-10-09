package org.platica.demo.domain.model

enum class BlockReason {
    GLOBAL_PAUSE,
    SNOOZE,
    GRACE,
    COOLDOWN,
    HOURLY_LIMIT,
    INTERVENTION_ALREADY_VISIBLE,
    NOT_TRIGGER_APP,
    NOT_TRIGGERED
}
