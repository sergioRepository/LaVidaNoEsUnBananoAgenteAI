package com.lavidanoesunbanano.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.lavidanoesunbanano.domain.model.InterventionReason
import com.lavidanoesunbanano.ui.intervention.InterventionActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHelper: NotificationHelper
) {
    companion object {
        private const val TAG = "InterventionLauncher"
        val isInterventionCurrentlyVisible = AtomicBoolean(false)
    }

    fun isInterventionVisible(): Boolean = isInterventionCurrentlyVisible.get()

    fun launch(
        packageName: String,
        reason: InterventionReason,
        sessionDurationSeconds: Long
    ) {
        if (isInterventionVisible()) {
            Log.d(TAG, "Intervención ya visible en pantalla. Ignorando nuevo disparo.")
            return
        }

        var directLaunchSuccess = false

        // 1. Intento directo con SYSTEM_ALERT_WINDOW
        if (Settings.canDrawOverlays(context)) {
            try {
                val intent = Intent(context, InterventionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(InterventionActivity.EXTRA_PACKAGE_NAME, packageName)
                    putExtra(InterventionActivity.EXTRA_REASON, reason.value)
                    putExtra(InterventionActivity.EXTRA_DURATION_SECONDS, sessionDurationSeconds)
                }
                context.startActivity(intent)
                directLaunchSuccess = true
                Log.d(TAG, "InterventionActivity lanzada directamente con FLAG_ACTIVITY_NEW_TASK.")
            } catch (e: Exception) {
                Log.e(TAG, "Fallo al lanzar InterventionActivity directamente", e)
            }
        }

        // 2. Respaldo obligatorio con fullScreenIntent para Android 10-15
        if (!directLaunchSuccess) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val canUseFullScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                notificationManager.canUseFullScreenIntent()
            } else {
                true
            }

            if (canUseFullScreen) {
                notificationHelper.showInterventionFallback(
                    packageName = packageName,
                    reason = reason.value,
                    sessionDurationSeconds = sessionDurationSeconds
                )
                Log.d(TAG, "Notificación fullScreenIntent emitida como respaldo.")
            } else {
                Log.w(TAG, "canUseFullScreenIntent() es false; no se pudo disparar fullScreenIntent.")
            }
        }
    }

    /**
     * Dispara una simulación inmediata del flujo completo de intervención sin depender de UsageStats.
     */
    fun launchSimulation() {
        launch(
            packageName = context.packageName,
            reason = InterventionReason.TRIGGER_APP_IN_RISK_WINDOW,
            sessionDurationSeconds = 12 * 60L // 12 minutos
        )
    }
}
