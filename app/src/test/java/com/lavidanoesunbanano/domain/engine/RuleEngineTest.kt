package com.lavidanoesunbanano.domain.engine

import com.lavidanoesunbanano.domain.model.BlockReason
import com.lavidanoesunbanano.domain.model.InterventionDecision
import com.lavidanoesunbanano.domain.model.InterventionReason
import com.lavidanoesunbanano.domain.model.InterventionRecord
import com.lavidanoesunbanano.domain.model.RiskSchedule
import com.lavidanoesunbanano.domain.model.RuleDecision
import com.lavidanoesunbanano.domain.model.RuleState
import com.lavidanoesunbanano.test.fakes.FakeClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class RuleEngineTest {

    private lateinit var fakeClock: FakeClock
    private lateinit var ruleEngine: RuleEngine

    private val testPackage = "com.instagram.android"
    private val zoneId = ZoneId.of("America/Bogota") // UTC-5
    private val scheduleAcrossMidnight = RiskSchedule(startHour = 22, startMinute = 0, endHour = 2, endMinute = 0)

    @Before
    fun setup() {
        // En America/Bogota (UTC-5), 2026-10-07T22:30:00 local es 2026-10-08T03:30:00Z
        fakeClock = FakeClock(
            currentInstant = Instant.parse("2026-10-08T03:30:00Z"),
            currentZoneId = zoneId
        )
        ruleEngine = RuleEngine(fakeClock)
    }

    private fun createState(
        currentPackage: String = testPackage,
        isTriggerApp: Boolean = true,
        sessionDurationSeconds: Long = 0,
        currentTime: Instant = fakeClock.now(),
        isInterventionVisible: Boolean = false,
        globalPauseUntil: Instant? = null,
        riskSchedule: RiskSchedule = scheduleAcrossMidnight,
        sessionThresholdMinutes: Int = 15,
        maxHourlyInterventions: Int = 4,
        recentForApp: List<InterventionRecord> = emptyList(),
        recentGlobal: List<InterventionRecord> = emptyList()
    ): RuleState {
        return RuleState(
            currentPackage = currentPackage,
            isTriggerApp = isTriggerApp,
            sessionDurationSeconds = sessionDurationSeconds,
            currentTime = currentTime,
            zoneId = zoneId,
            isInterventionVisible = isInterventionVisible,
            globalPauseUntil = globalPauseUntil,
            riskSchedule = riskSchedule,
            sessionThresholdMinutes = sessionThresholdMinutes,
            maxHourlyInterventions = maxHourlyInterventions,
            recentInterventionsForApp = recentForApp,
            recentInterventionsGlobal = recentGlobal
        )
    }

    @Test
    fun evaluate_riskWindowCrossingMidnight_insideWindow_intervenes() {
        // 22:30 local -> dentro del horario de riesgo 22:00-02:00
        val state = createState(sessionDurationSeconds = 60)
        val decision = ruleEngine.evaluate(state)

        assertEquals(RuleDecision.Intervene(InterventionReason.TRIGGER_APP_IN_RISK_WINDOW), decision)
    }

    @Test
    fun evaluate_riskWindowCrossingMidnight_boundaries() {
        // Inicio inclusivo: 22:00 local (2026-10-08T03:00:00Z)
        val atStart = createState(currentTime = Instant.parse("2026-10-08T03:00:00Z"))
        assertEquals(RuleDecision.Intervene(InterventionReason.TRIGGER_APP_IN_RISK_WINDOW), ruleEngine.evaluate(atStart))

        // Antes del inicio: 21:59:59 local (2026-10-08T02:59:59Z)
        val beforeStart = createState(currentTime = Instant.parse("2026-10-08T02:59:59Z"))
        assertEquals(RuleDecision.NoAction(BlockReason.NOT_TRIGGERED), ruleEngine.evaluate(beforeStart))

        // Justo antes del fin: 01:59:59 local (2026-10-08T06:59:59Z)
        val beforeEnd = createState(currentTime = Instant.parse("2026-10-08T06:59:59Z"))
        assertEquals(RuleDecision.Intervene(InterventionReason.TRIGGER_APP_IN_RISK_WINDOW), ruleEngine.evaluate(beforeEnd))

        // Fin exclusivo: 02:00 local (2026-10-08T07:00:00Z) -> fuera
        val atEnd = createState(currentTime = Instant.parse("2026-10-08T07:00:00Z"))
        assertEquals(RuleDecision.NoAction(BlockReason.NOT_TRIGGERED), ruleEngine.evaluate(atEnd))
    }

    @Test
    fun evaluate_sessionThreshold_belowEqualAbove() {
        // Fuera del horario de riesgo (15:00 local = 2026-10-07T20:00:00Z)
        val dayTime = Instant.parse("2026-10-07T20:00:00Z")
        val thresholdMinutes = 15 // 900 segundos

        // Justo por debajo: 899s
        val belowState = createState(
            currentTime = dayTime,
            sessionThresholdMinutes = thresholdMinutes,
            sessionDurationSeconds = 899
        )
        assertEquals(RuleDecision.NoAction(BlockReason.NOT_TRIGGERED), ruleEngine.evaluate(belowState))

        // Igual al umbral: 900s
        val equalState = createState(
            currentTime = dayTime,
            sessionThresholdMinutes = thresholdMinutes,
            sessionDurationSeconds = 900
        )
        assertEquals(RuleDecision.Intervene(InterventionReason.SESSION_THRESHOLD_EXCEEDED), ruleEngine.evaluate(equalState))

        // Por encima del umbral: 901s
        val aboveState = createState(
            currentTime = dayTime,
            sessionThresholdMinutes = thresholdMinutes,
            sessionDurationSeconds = 901
        )
        assertEquals(RuleDecision.Intervene(InterventionReason.SESSION_THRESHOLD_EXCEEDED), ruleEngine.evaluate(aboveState))
    }

    @Test
    fun evaluate_precedence_bothReasonsMet_registersTriggerAppInRiskWindow() {
        // 22:30 local (en horario de riesgo) y sesión de 20 min (umbral 15 min superado)
        val state = createState(
            sessionDurationSeconds = 20 * 60
        )
        val decision = ruleEngine.evaluate(state)

        assertEquals(RuleDecision.Intervene(InterventionReason.TRIGGER_APP_IN_RISK_WINDOW), decision)
    }

    @Test
    fun evaluate_globalPause_blocksAllInterventions() {
        val now = fakeClock.now()
        val pauseUntil = now.plusSeconds(3600) // 1 hora de pausa
        val state = createState(
            globalPauseUntil = pauseUntil,
            sessionDurationSeconds = 30 * 60
        )

        assertEquals(RuleDecision.NoAction(BlockReason.GLOBAL_PAUSE), ruleEngine.evaluate(state))
    }

    @Test
    fun evaluate_snoozeFor15Minutes_blocksUntilExpired() {
        val now = fakeClock.now()
        val snoozeEvent = InterventionRecord(
            id = 1,
            timestamp = now.minusSeconds(14 * 60), // Pasaron 14 min (faltan 1 min para expirar)
            packageName = testPackage,
            reason = InterventionReason.TRIGGER_APP_IN_RISK_WINDOW,
            decision = InterventionDecision.SNOOZE,
            sessionDurationSeconds = 100
        )

        val stateActive = createState(recentForApp = listOf(snoozeEvent))
        assertEquals(RuleDecision.NoAction(BlockReason.SNOOZE), ruleEngine.evaluate(stateActive))

        // Al pasar 15 minutos exactos, se libera el snooze
        val expiredSnooze = snoozeEvent.copy(timestamp = now.minusSeconds(15 * 60))
        val stateExpired = createState(recentForApp = listOf(expiredSnooze))
        // Ahora sí interviene
        assertTrue(ruleEngine.evaluate(stateExpired) is RuleDecision.Intervene)
    }

    @Test
    fun evaluate_graceFor10MinutesAfterContinue_blocksUntilExpired() {
        val now = fakeClock.now()
        val continueEvent = InterventionRecord(
            id = 1,
            timestamp = now.minusSeconds(9 * 60), // Pasaron 9 min (gracia de 10 min activa)
            packageName = testPackage,
            reason = InterventionReason.TRIGGER_APP_IN_RISK_WINDOW,
            decision = InterventionDecision.CONTINUE,
            sessionDurationSeconds = 300
        )

        val stateActive = createState(recentForApp = listOf(continueEvent))
        assertEquals(RuleDecision.NoAction(BlockReason.GRACE), ruleEngine.evaluate(stateActive))

        // Al pasar 10 minutos exactos, se libera la gracia
        val expiredContinue = continueEvent.copy(timestamp = now.minusSeconds(10 * 60))
        val stateExpired = createState(recentForApp = listOf(expiredContinue))
        assertTrue(ruleEngine.evaluate(stateExpired) is RuleDecision.Intervene)
    }

    @Test
    fun evaluate_cooldownFor10MinutesAfterAnyIntervention_blocks() {
        val now = fakeClock.now()
        val anyEvent = InterventionRecord(
            id = 1,
            timestamp = now.minusSeconds(5 * 60), // Pasaron 5 min
            packageName = testPackage,
            reason = InterventionReason.TRIGGER_APP_IN_RISK_WINDOW,
            decision = InterventionDecision.EXIT,
            sessionDurationSeconds = 200
        )

        val state = createState(recentForApp = listOf(anyEvent))
        assertEquals(RuleDecision.NoAction(BlockReason.COOLDOWN), ruleEngine.evaluate(state))
    }

    @Test
    fun evaluate_hourlyLimit_blocksWhenReached_andReleasesAfter60Minutes() {
        val now = fakeClock.now()
        // 4 intervenciones en los últimos 40 minutos
        val recentList = listOf(
            InterventionRecord(1, now.minusSeconds(40 * 60), "app1", InterventionReason.TRIGGER_APP_IN_RISK_WINDOW, InterventionDecision.EXIT, 10),
            InterventionRecord(2, now.minusSeconds(30 * 60), "app2", InterventionReason.TRIGGER_APP_IN_RISK_WINDOW, InterventionDecision.EXIT, 10),
            InterventionRecord(3, now.minusSeconds(20 * 60), "app3", InterventionReason.TRIGGER_APP_IN_RISK_WINDOW, InterventionDecision.EXIT, 10),
            InterventionRecord(4, now.minusSeconds(10 * 60), "app4", InterventionReason.TRIGGER_APP_IN_RISK_WINDOW, InterventionDecision.EXIT, 10)
        )

        val stateBlocked = createState(
            maxHourlyInterventions = 4,
            recentGlobal = recentList
        )
        assertEquals(RuleDecision.NoAction(BlockReason.HOURLY_LIMIT), ruleEngine.evaluate(stateBlocked))

        // Si la más antigua ocurrió hace 61 minutos, ahora solo hay 3 en la ventana de 60 min -> se libera
        val releasedList = listOf(
            InterventionRecord(1, now.minusSeconds(61 * 60), "app1", InterventionReason.TRIGGER_APP_IN_RISK_WINDOW, InterventionDecision.EXIT, 10),
            InterventionRecord(2, now.minusSeconds(30 * 60), "app2", InterventionReason.TRIGGER_APP_IN_RISK_WINDOW, InterventionDecision.EXIT, 10),
            InterventionRecord(3, now.minusSeconds(20 * 60), "app3", InterventionReason.TRIGGER_APP_IN_RISK_WINDOW, InterventionDecision.EXIT, 10),
            InterventionRecord(4, now.minusSeconds(10 * 60), "app4", InterventionReason.TRIGGER_APP_IN_RISK_WINDOW, InterventionDecision.EXIT, 10)
        )

        val stateReleased = createState(
            maxHourlyInterventions = 4,
            recentGlobal = releasedList
        )
        assertTrue(ruleEngine.evaluate(stateReleased) is RuleDecision.Intervene)
    }

    @Test
    fun evaluate_notTriggerApp_noAction() {
        val state = createState(isTriggerApp = false)
        assertEquals(RuleDecision.NoAction(BlockReason.NOT_TRIGGER_APP), ruleEngine.evaluate(state))
    }

    @Test
    fun evaluate_interventionAlreadyVisible_ignoresNewTriggers() {
        val state = createState(isInterventionVisible = true)
        assertEquals(RuleDecision.NoAction(BlockReason.INTERVENTION_ALREADY_VISIBLE), ruleEngine.evaluate(state))
    }
}
