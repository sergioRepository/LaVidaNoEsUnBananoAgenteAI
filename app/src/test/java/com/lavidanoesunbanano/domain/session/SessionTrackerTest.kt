package com.lavidanoesunbanano.domain.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionTrackerTest {

    private lateinit var tracker: SessionTracker
    private val ownPackage = "com.lavidanoesunbanano"
    private val triggerApp = "com.instagram.android"
    private val otherApp = "com.android.chrome"
    private val launcherApp = "com.google.android.apps.nexuslauncher"

    @Before
    fun setup() {
        tracker = SessionTracker(
            ownPackageName = ownPackage,
            systemPackages = setOf(launcherApp, "com.android.systemui")
        )
    }

    @Test
    fun startSession_whenTriggerAppEntersForeground() {
        val t0 = 1000000L
        val snapshot = tracker.onPackageForeground(triggerApp, t0, isTriggerApp = true)

        assertNotNull(snapshot)
        assertEquals(triggerApp, tracker.activePackage)
        assertEquals(t0, tracker.startTimeEpochMilli)
        assertEquals(0L, tracker.accumulatedDurationSeconds)
    }

    @Test
    fun sessionAccumulatesTime_onContinuousForegroundTicks() {
        val t0 = 1000000L
        tracker.onPackageForeground(triggerApp, t0, isTriggerApp = true)

        val t1 = t0 + 5000L // +5s
        tracker.onTick(t1)
        assertEquals(5L, tracker.accumulatedDurationSeconds)

        val t2 = t1 + 10000L // +10s
        tracker.onTick(t2)
        assertEquals(15L, tracker.accumulatedDurationSeconds)
    }

    @Test
    fun sessionCloses_after30SecondsOutsideApp() {
        val t0 = 1000000L
        tracker.onPackageForeground(triggerApp, t0, isTriggerApp = true)

        // Va al launcher a t0 + 10s
        val t1 = t0 + 10000L
        tracker.onPackageForeground(launcherApp, t1, isTriggerApp = false)
        assertNotNull(tracker.activePackage) // Sigue en período de gracia de 30s

        // Pasan 29 segundos fuera de la app
        val t2 = t1 + 29000L
        val snap2 = tracker.onTick(t2)
        assertNotNull(snap2) // Todavía no se cierra

        // Pasa a los 30 segundos fuera de la app
        val t3 = t1 + 30000L
        val snap3 = tracker.onTick(t3)
        assertNull(snap3) // Sesión cerrada
        assertNull(tracker.activePackage)
    }

    @Test
    fun ownAppIsNeutral_doesNotCloseOrStartSessions() {
        val t0 = 1000000L
        tracker.onPackageForeground(triggerApp, t0, isTriggerApp = true)

        // La pantalla de respiración (propia app) aparece a t0 + 10s y dura 30s
        val t1 = t0 + 10000L
        tracker.onPackageForeground(ownPackage, t1, isTriggerApp = false)

        val t2 = t1 + 30000L // 30s en respiración
        tracker.onPackageForeground(ownPackage, t2, isTriggerApp = false)

        // La sesión de la app gatillo sigue viva y no se cerró
        assertEquals(triggerApp, tracker.activePackage)
        assertNull(tracker.timeAwayStartEpochMilli)
    }

    @Test
    fun screenOff_countsAsOutsideApp() {
        val t0 = 1000000L
        tracker.onPackageForeground(triggerApp, t0, isTriggerApp = true)

        // Pantalla se apaga
        val t1 = t0 + 5000L
        tracker.onScreenOff(t1)

        assertEquals(t1, tracker.timeAwayStartEpochMilli)

        // Pasan 30 segundos con la pantalla apagada
        val t2 = t1 + 30000L
        val snap = tracker.onTick(t2)
        assertNull(snap)
        assertNull(tracker.activePackage)
    }

    @Test
    fun exitDecision_immediatelyClosesSession() {
        val t0 = 1000000L
        tracker.onPackageForeground(triggerApp, t0, isTriggerApp = true)

        tracker.onExitDecision()

        assertNull(tracker.activePackage)
        assertEquals(0L, tracker.accumulatedDurationSeconds)
    }

    @Test
    fun restoreSession_within30Seconds_continues() {
        val t0 = 1000000L
        val previousSnapshot = SessionSnapshot(
            packageName = triggerApp,
            startTimeEpochMilli = t0,
            accumulatedDurationSeconds = 120L,
            lastUpdatedEpochMilli = t0 + 120000L
        )

        val resumeTime = previousSnapshot.lastUpdatedEpochMilli + 15000L // 15 s después (< 30s)
        val restored = tracker.restoreSession(previousSnapshot, resumeTime)

        assertTrue(restored)
        assertEquals(triggerApp, tracker.activePackage)
        assertEquals(120L, tracker.accumulatedDurationSeconds)
    }

    @Test
    fun restoreSession_after30Seconds_closesAndDiscards() {
        val t0 = 1000000L
        val previousSnapshot = SessionSnapshot(
            packageName = triggerApp,
            startTimeEpochMilli = t0,
            accumulatedDurationSeconds = 120L,
            lastUpdatedEpochMilli = t0 + 120000L
        )

        val resumeTime = previousSnapshot.lastUpdatedEpochMilli + 35000L // 35 s después (>= 30s)
        val restored = tracker.restoreSession(previousSnapshot, resumeTime)

        assertFalse(restored)
        assertNull(tracker.activePackage)
    }
}
