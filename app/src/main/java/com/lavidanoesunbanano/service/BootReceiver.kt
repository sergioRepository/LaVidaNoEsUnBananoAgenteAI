package com.lavidanoesunbanano.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.lavidanoesunbanano.domain.repository.SettingsRepository
import com.lavidanoesunbanano.ui.onboarding.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.i(TAG, "Reinicio del dispositivo detectado: ${intent.action}")

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = settingsRepository.getSettings().first()
                    val hasPermissions = PermissionHelper.hasAllMandatoryPermissions(context)

                    if (settings.monitoringEnabled && hasPermissions) {
                        Log.i(TAG, "Monitoreo activo y permisos concedidos. Reiniciando UsageMonitorService...")
                        val serviceIntent = Intent(context, UsageMonitorService::class.java)
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } else {
                        Log.i(TAG, "No se reinicia el servicio: monitoreo=${settings.monitoringEnabled}, permisos=$hasPermissions")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en BootReceiver", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
