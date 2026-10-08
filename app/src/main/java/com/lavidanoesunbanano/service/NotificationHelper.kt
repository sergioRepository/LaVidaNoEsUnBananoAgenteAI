package com.lavidanoesunbanano.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lavidanoesunbanano.MainActivity
import com.lavidanoesunbanano.R
import com.lavidanoesunbanano.ui.intervention.InterventionActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_MONITORING_ID = "channel_monitoring"
        const val CHANNEL_ALERTS_ID = "channel_alerts"
        const val CHANNEL_INTERVENTIONS_ID = "channel_interventions"

        const val NOTIFICATION_ID_FOREGROUND = 1001
        const val NOTIFICATION_ID_REVOKED = 1002
        const val NOTIFICATION_ID_INTERVENTION = 1003

        const val ACTION_PAUSE_ONE_HOUR = "com.lavidanoesunbanano.ACTION_PAUSE_ONE_HOUR"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val monitoringChannel = NotificationChannel(
                CHANNEL_MONITORING_ID,
                "Acompañamiento Activo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación persistente de monitoreo"
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Alertas y Permisos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos sobre permisos revocados"
            }

            val interventionsChannel = NotificationChannel(
                CHANNEL_INTERVENTIONS_ID,
                "Intervenciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos a pantalla completa cuando se requiere pausa reflexiva"
            }

            notificationManager.createNotificationChannels(
                listOf(monitoringChannel, alertsChannel, interventionsChannel)
            )
        }
    }

    fun buildForegroundNotification(): Notification {
        val appName = context.getString(R.string.app_name)
        val title = "$appName está acompañándote"
        val content = context.getString(R.string.monitoring_notification_content)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(context, UsageMonitorService::class.java).apply {
            action = ACTION_PAUSE_ONE_HOUR
        }
        val pausePendingIntent = PendingIntent.getService(
            context,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_MONITORING_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.action_pause_one_hour),
                pausePendingIntent
            )
            .build()
    }

    fun showPermissionRevokedNotification() {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.permission_revoked_title))
            .setContentText(context.getString(R.string.permission_revoked_content))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID_REVOKED, notification)
    }

    fun buildInterventionFullScreenNotification(
        packageName: String,
        reason: String,
        sessionDurationSeconds: Long
    ): Notification {
        val interventionIntent = Intent(context, InterventionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(InterventionActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(InterventionActivity.EXTRA_REASON, reason)
            putExtra(InterventionActivity.EXTRA_DURATION_SECONDS, sessionDurationSeconds)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_INTERVENTION,
            interventionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_INTERVENTIONS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Momento de pausa reflexiva")
            .setContentText("Toca para abrir la intervención guiada")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()
    }

    fun showInterventionFallback(
        packageName: String,
        reason: String,
        sessionDurationSeconds: Long
    ) {
        val notification = buildInterventionFullScreenNotification(packageName, reason, sessionDurationSeconds)
        notificationManager.notify(NOTIFICATION_ID_INTERVENTION, notification)
    }
}
