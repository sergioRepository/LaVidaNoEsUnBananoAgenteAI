package com.lavidanoesunbanano.domain.engine

import com.lavidanoesunbanano.domain.model.BlockReason
import com.lavidanoesunbanano.domain.model.InterventionDecision
import com.lavidanoesunbanano.domain.model.InterventionReason
import com.lavidanoesunbanano.domain.model.RuleDecision
import com.lavidanoesunbanano.domain.model.RuleState
import com.lavidanoesunbanano.domain.model.UserSettings
import com.lavidanoesunbanano.domain.model.InterventionRecord
import com.lavidanoesunbanano.domain.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor de reglas de intervención.
 * Función pura que evalúa el estado del sistema en estricto orden de precedencia.
 * Kotlin puro sin dependencias de Android.
 */
@Singleton
class RuleEngine @Inject constructor(
    private val clock: Clock
) {
    companion object {
        const val SNOOZE_DURATION_SECONDS = 15 * 60L // 15 min
        const val GRACE_DURATION_SECONDS = 10 * 60L // 10 min
        const val COOLDOWN_DURATION_SECONDS = 10 * 60L // 10 min
        const val HOURLY_WINDOW_SECONDS = 60 * 60L // 60 min (1 h)
    }

    /**
     * Evalúa el estado actual y decide si se debe intervenir o bloquear.
     */
    fun evaluate(state: RuleState): RuleDecision {
        // 0. Si ya hay una intervención en pantalla, ignorar nuevos disparos
        if (state.isInterventionVisible) {
            return RuleDecision.NoAction(BlockReason.INTERVENTION_ALREADY_VISIBLE)
        }

        // 1. Pausa global activa ("Pausar monitoreo 1 h") -> NoAction
        if (state.globalPauseUntil != null && state.currentTime.isBefore(state.globalPauseUntil)) {
            return RuleDecision.NoAction(BlockReason.GLOBAL_PAUSE)
        }

        // Las razones aplican únicamente a apps gatillo
        if (!state.isTriggerApp) {
            return RuleDecision.NoAction(BlockReason.NOT_TRIGGER_APP)
        }

        // 2. Posponer vigente para esa app (15 min) -> NoAction
        val lastSnooze = state.recentInterventionsForApp
            .filter { it.decision == InterventionDecision.SNOOZE }
            .maxByOrNull { it.timestamp }

        if (lastSnooze != null) {
            val elapsedAfterSnooze = Duration.between(lastSnooze.timestamp, state.currentTime).seconds
            if (elapsedAfterSnooze < SNOOZE_DURATION_SECONDS) {
                return RuleDecision.NoAction(BlockReason.SNOOZE)
            }
        }

        // 3. Gracia vigente para esa app (10 min tras "Continuar") -> NoAction
        val lastContinue = state.recentInterventionsForApp
            .filter { it.decision == InterventionDecision.CONTINUE }
            .maxByOrNull { it.timestamp }

        if (lastContinue != null) {
            val elapsedAfterContinue = Duration.between(lastContinue.timestamp, state.currentTime).seconds
            if (elapsedAfterContinue < GRACE_DURATION_SECONDS) {
                return RuleDecision.NoAction(BlockReason.GRACE)
            }
        }

        // 4. Cooldown vigente para esa app (10 min tras cualquier intervención) -> NoAction
        val lastAnyIntervention = state.recentInterventionsForApp
            .maxByOrNull { it.timestamp }

        if (lastAnyIntervention != null) {
            val elapsedAfterIntervention = Duration.between(lastAnyIntervention.timestamp, state.currentTime).seconds
            if (elapsedAfterIntervention < COOLDOWN_DURATION_SECONDS) {
                return RuleDecision.NoAction(BlockReason.COOLDOWN)
            }
        }

        // 5. Límite por hora alcanzado (por defecto 4, ventana móvil de 60 min, configurable) -> NoAction
        val windowStart = state.currentTime.minusSeconds(HOURLY_WINDOW_SECONDS)
        val hourlyCount = state.recentInterventionsGlobal.count {
            !it.timestamp.isBefore(windowStart) && !it.timestamp.isAfter(state.currentTime)
        }
        if (hourlyCount >= state.maxHourlyInterventions) {
            return RuleDecision.NoAction(BlockReason.HOURLY_LIMIT)
        }

        // 6. Evaluar razones de intervención
        val localTime = LocalDateTime.ofInstant(state.currentTime, state.zoneId).toLocalTime()
        val inRiskWindow = state.riskSchedule.isWithin(localTime)
        val thresholdExceeded = state.sessionDurationSeconds >= (state.sessionThresholdMinutes * 60L)

        // Si se cumplen ambas, la razón registrada es TRIGGER_APP_IN_RISK_WINDOW
        return when {
            inRiskWindow -> RuleDecision.Intervene(InterventionReason.TRIGGER_APP_IN_RISK_WINDOW)
            thresholdExceeded -> RuleDecision.Intervene(InterventionReason.SESSION_THRESHOLD_EXCEEDED)
            else -> RuleDecision.NoAction(BlockReason.NOT_TRIGGERED)
        }
    }

    /**
     * Sobrecarga de conveniencia que utiliza el reloj inyectado.
     */
    fun evaluateCurrent(
        currentPackage: String,
        isTriggerApp: Boolean,
        sessionDurationSeconds: Long,
        isInterventionVisible: Boolean,
        settings: UserSettings,
        recentForApp: List<InterventionRecord>,
        recentGlobal: List<InterventionRecord>
    ): RuleDecision {
        val now = clock.now()
        val zone = clock.zoneId()
        val pauseInstant = settings.globalPauseUntilEpochMilli?.let { java.time.Instant.ofEpochMilli(it) }

        val state = RuleState(
            currentPackage = currentPackage,
            isTriggerApp = isTriggerApp,
            sessionDurationSeconds = sessionDurationSeconds,
            currentTime = now,
            zoneId = zone,
            isInterventionVisible = isInterventionVisible,
            globalPauseUntil = pauseInstant,
            riskSchedule = settings.riskSchedule,
            sessionThresholdMinutes = settings.sessionThresholdMinutes,
            maxHourlyInterventions = settings.maxHourlyInterventions,
            recentInterventionsForApp = recentForApp,
            recentInterventionsGlobal = recentGlobal
        )
        return evaluate(state)
    }
}
