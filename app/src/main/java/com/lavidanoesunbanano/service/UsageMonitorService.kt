package com.lavidanoesunbanano.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import com.lavidanoesunbanano.data.source.UsageEventSource
import com.lavidanoesunbanano.data.source.UsageEventType
import com.lavidanoesunbanano.domain.engine.RuleEngine
import com.lavidanoesunbanano.domain.model.RuleDecision
import com.lavidanoesunbanano.domain.repository.InterventionRepository
import com.lavidanoesunbanano.domain.repository.SessionRepository
import com.lavidanoesunbanano.domain.repository.SettingsRepository
import com.lavidanoesunbanano.domain.repository.TriggerAppRepository
import com.lavidanoesunbanano.domain.session.SessionTracker
import com.lavidanoesunbanano.domain.time.Clock
import com.lavidanoesunbanano.ui.onboarding.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UsageMonitorService : Service() {

    companion object {
        private const val TAG = "UsageMonitorService"
        private const val POLL_INTERVAL_MS = 2000L // 2 segundos
        private const val OVERLAP_MS = 1000L // 1 segundo de solapamiento
        private const val REPLAY_WINDOW_MS = 10 * 60 * 1000L // 10 minutos
    }

    @Inject lateinit var usageEventSource: UsageEventSource
    @Inject lateinit var ruleEngine: RuleEngine
    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var interventionRepository: InterventionRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var triggerAppRepository: TriggerAppRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var interventionLauncher: InterventionLauncher
    @Inject lateinit var clock: Clock

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private lateinit var sessionTracker: SessionTracker
    private var lastProcessedTimestamp: Long = 0L

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                Log.d(TAG, "Pantalla apagada detectada (cuenta como fuera de la app)")
                val now = clock.now().toEpochMilli()
                sessionTracker.onScreenOff(now)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Iniciando UsageMonitorService...")

        // Inicializar SessionTracker con exclusiones del Launcher y SystemUI
        val launcherPackage = resolveLauncherPackage()
        val systemPackages = setOfNotNull(launcherPackage, "com.android.systemui")
        sessionTracker = SessionTracker(
            ownPackageName = packageName,
            systemPackages = systemPackages
        )

        // Registrar receptor de pantalla apagada
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)

        // Arrancar en primer plano de inmediato
        startForeground(
            NotificationHelper.NOTIFICATION_ID_FOREGROUND,
            notificationHelper.buildForegroundNotification()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == NotificationHelper.ACTION_PAUSE_ONE_HOUR) {
            Log.i(TAG, "Acción recibida: Pausar monitoreo 1 h")
            serviceScope.launch {
                settingsRepository.pauseMonitoringForOneHour(clock.now().toEpochMilli())
            }
            return START_STICKY
        }

        if (monitorJob == null || monitorJob?.isActive != true) {
            startMonitoringLoop()
        }

        return START_STICKY
    }

    private fun startMonitoringLoop() {
        monitorJob = serviceScope.launch {
            val initialNow = clock.now().toEpochMilli()

            // 1. Reconstruir estado: Restaurar sesión activa de Room
            val savedSession = sessionRepository.getActiveSession()
            if (savedSession != null) {
                val restored = sessionTracker.restoreSession(savedSession, initialNow)
                Log.d(TAG, "Restauración de sesión previa: $restored (${savedSession.packageName})")
            }

            // 2. Repasar eventos de los últimos 10 minutos
            val replayStart = initialNow - REPLAY_WINDOW_MS
            val replayEvents = usageEventSource.queryEvents(replayStart, initialNow)
            val triggerPackages = triggerAppRepository.getTriggerPackageNames().toSet()

            for (event in replayEvents) {
                if (event.eventType == UsageEventType.FOREGROUND) {
                    val isTrigger = triggerPackages.contains(event.packageName)
                    sessionTracker.onPackageForeground(event.packageName, event.timestampEpochMilli, isTrigger)
                }
            }
            lastProcessedTimestamp = initialNow

            // 3. Ciclo continuo de monitoreo cada 2 s
            while (isActive) {
                val currentNow = clock.now().toEpochMilli()

                // Comprobación de permisos obligatorios en cada tick
                if (!PermissionHelper.hasAllMandatoryPermissions(this@UsageMonitorService)) {
                    Log.w(TAG, "Permiso obligatorio revocado. Deteniendo servicio y notificando al usuario.")
                    notificationHelper.showPermissionRevokedNotification()
                    settingsRepository.updateMonitoringEnabled(false)
                    stopSelf()
                    break
                }

                val settings = settingsRepository.getSettings().first()
                if (!settings.monitoringEnabled) {
                    Log.i(TAG, "Monitoreo desactivado en configuración. Deteniendo servicio.")
                    stopSelf()
                    break
                }

                // Consultar eventos desde la última marca con 1s de solapamiento
                val queryFrom = (lastProcessedTimestamp - OVERLAP_MS).coerceAtLeast(0)
                val newEvents = usageEventSource.queryEvents(queryFrom, currentNow)
                lastProcessedTimestamp = currentNow

                val currentTriggers = triggerAppRepository.getTriggerPackageNames().toSet()

                for (event in newEvents) {
                    if (event.eventType == UsageEventType.FOREGROUND) {
                        val isTrigger = currentTriggers.contains(event.packageName)
                        sessionTracker.onPackageForeground(event.packageName, event.timestampEpochMilli, isTrigger)
                    }
                }

                // Tick del SessionTracker
                val currentSnapshot = sessionTracker.onTick(currentNow)

                if (currentSnapshot != null) {
                    // Persistir sesión periódicamente
                    sessionRepository.saveActiveSession(currentSnapshot)

                    val isTriggerApp = currentTriggers.contains(currentSnapshot.packageName)
                    val recentForApp = interventionRepository.getInterventionsForApp(currentSnapshot.packageName)
                    val oneHourAgo = currentNow - (60 * 60 * 1000L)
                    val recentGlobal = interventionRepository.getInterventionsGlobalSince(oneHourAgo)
                    val isVisible = interventionLauncher.isInterventionVisible()

                    // Evaluar reglas con el RuleEngine
                    val decision = ruleEngine.evaluateCurrent(
                        currentPackage = currentSnapshot.packageName,
                        isTriggerApp = isTriggerApp,
                        sessionDurationSeconds = currentSnapshot.accumulatedDurationSeconds,
                        isInterventionVisible = isVisible,
                        settings = settings,
                        recentForApp = recentForApp,
                        recentGlobal = recentGlobal
                    )

                    Log.d(TAG, "RuleEngine evaluó: $decision para app ${currentSnapshot.packageName} (duración: ${currentSnapshot.accumulatedDurationSeconds}s)")

                    if (decision is RuleDecision.Intervene) {
                        Log.i(TAG, "¡DISPARANDO INTERVENCIÓN! Razón: ${decision.reason} en app: ${currentSnapshot.packageName}")
                        interventionLauncher.launch(
                            packageName = currentSnapshot.packageName,
                            reason = decision.reason,
                            sessionDurationSeconds = currentSnapshot.accumulatedDurationSeconds
                        )
                    }
                }

                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun resolveLauncherPackage(): String? {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(
                homeIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolveInfo?.activityInfo?.packageName
    }

    override fun onDestroy() {
        Log.i(TAG, "Destruyendo UsageMonitorService...")
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {}

        // Persistir sesión activa al detenerse
        sessionTracker.currentSnapshot()?.let { snapshot ->
            serviceScope.launch {
                sessionRepository.saveActiveSession(snapshot)
            }
        }

        serviceScope.cancel()
        super.onDestroy()
    }
}
