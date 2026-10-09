package org.platica.demo.domain.session

data class SessionSnapshot(
    val packageName: String,
    val startTimeEpochMilli: Long,
    val accumulatedDurationSeconds: Long,
    val lastUpdatedEpochMilli: Long
)

/**
 * Gestor de seguimiento de sesiones activas.
 * Lógica pura sin dependencias de Android.
 */
class SessionTracker(
    private val ownPackageName: String = "org.platica.demo",
    private var systemPackages: Set<String> = setOf("com.android.systemui")
) {
    companion object {
        const val TIMEOUT_MILLIS = 30_000L // 30 segundos
    }

    var activePackage: String? = null
        private set

    var startTimeEpochMilli: Long = 0L
        private set

    var accumulatedDurationSeconds: Long = 0L
        private set

    var lastSeenEpochMilli: Long = 0L
        private set

    var timeAwayStartEpochMilli: Long? = null
        private set

    fun updateSystemPackages(packages: Set<String>) {
        systemPackages = packages + "com.android.systemui"
    }

    /**
     * Procesa la aparición de un paquete en primer plano.
     */
    fun onPackageForeground(
        packageName: String,
        timestampEpochMilli: Long,
        isTriggerApp: Boolean
    ): SessionSnapshot? {
        // La propia app es neutral: no inicia ni cierra sesiones
        if (packageName == ownPackageName) {
            return currentSnapshot()
        }

        // Launcher o SystemUI cuentan como "fuera de la app"
        if (packageName in systemPackages) {
            handleAway(timestampEpochMilli)
            return currentSnapshot()
        }

        if (isTriggerApp) {
            if (activePackage == packageName) {
                // Volvió o sigue en la misma app gatillo
                if (timeAwayStartEpochMilli != null) {
                    val awayTime = timestampEpochMilli - timeAwayStartEpochMilli!!
                    if (awayTime < TIMEOUT_MILLIS) {
                        // Volvió en menos de 30 s: la sesión continúa
                        timeAwayStartEpochMilli = null
                        lastSeenEpochMilli = timestampEpochMilli
                    } else {
                        // Estuvo fuera >= 30 s: se cerró la sesión y arranca una nueva
                        closeSession()
                        startSession(packageName, timestampEpochMilli)
                    }
                } else {
                    // Permanencia continua
                    val delta = (timestampEpochMilli - lastSeenEpochMilli).coerceAtLeast(0) / 1000L
                    accumulatedDurationSeconds += delta
                    lastSeenEpochMilli = timestampEpochMilli
                }
            } else {
                // Cambió a una app gatillo distinta
                closeSession()
                startSession(packageName, timestampEpochMilli)
            }
        } else {
            // App ordinaria que no es gatillo: cuenta como fuera de la app
            handleAway(timestampEpochMilli)
        }

        return currentSnapshot()
    }

    /**
     * Pantalla apagada cuenta como "fuera de la app".
     */
    fun onScreenOff(timestampEpochMilli: Long): SessionSnapshot? {
        handleAway(timestampEpochMilli)
        return currentSnapshot()
    }

    /**
     * Evaluación periódica (tick).
     */
    fun onTick(timestampEpochMilli: Long): SessionSnapshot? {
        if (activePackage == null) return null

        if (timeAwayStartEpochMilli != null) {
            val awayTime = timestampEpochMilli - timeAwayStartEpochMilli!!
            if (awayTime >= TIMEOUT_MILLIS) {
                closeSession()
                return null
            }
        } else {
            val delta = (timestampEpochMilli - lastSeenEpochMilli).coerceAtLeast(0) / 1000L
            if (delta > 0) {
                accumulatedDurationSeconds += delta
                lastSeenEpochMilli = timestampEpochMilli
            }
        }

        return currentSnapshot()
    }

    /**
     * Decisión "Salir" cierra la sesión inmediatamente.
     */
    fun onExitDecision() {
        closeSession()
    }

    /**
     * Restaura una sesión previa desde persistencia.
     * Si transcurrieron menos de 30 s, continúa; si no, se descarta.
     */
    fun restoreSession(snapshot: SessionSnapshot, currentTimestampEpochMilli: Long): Boolean {
        val elapsed = currentTimestampEpochMilli - snapshot.lastUpdatedEpochMilli
        return if (elapsed < TIMEOUT_MILLIS) {
            activePackage = snapshot.packageName
            startTimeEpochMilli = snapshot.startTimeEpochMilli
            accumulatedDurationSeconds = snapshot.accumulatedDurationSeconds
            lastSeenEpochMilli = currentTimestampEpochMilli
            timeAwayStartEpochMilli = null
            true
        } else {
            closeSession()
            false
        }
    }

    fun currentSnapshot(): SessionSnapshot? {
        val pkg = activePackage ?: return null
        return SessionSnapshot(
            packageName = pkg,
            startTimeEpochMilli = startTimeEpochMilli,
            accumulatedDurationSeconds = accumulatedDurationSeconds,
            lastUpdatedEpochMilli = lastSeenEpochMilli
        )
    }

    private fun startSession(packageName: String, timestampEpochMilli: Long) {
        activePackage = packageName
        startTimeEpochMilli = timestampEpochMilli
        accumulatedDurationSeconds = 0L
        lastSeenEpochMilli = timestampEpochMilli
        timeAwayStartEpochMilli = null
    }

    private fun handleAway(timestampEpochMilli: Long) {
        if (activePackage != null) {
            if (timeAwayStartEpochMilli == null) {
                timeAwayStartEpochMilli = timestampEpochMilli
            } else {
                val awayTime = timestampEpochMilli - timeAwayStartEpochMilli!!
                if (awayTime >= TIMEOUT_MILLIS) {
                    closeSession()
                }
            }
        }
    }

    fun closeSession() {
        activePackage = null
        startTimeEpochMilli = 0L
        accumulatedDurationSeconds = 0L
        lastSeenEpochMilli = 0L
        timeAwayStartEpochMilli = null
    }
}
