package com.lavidanoesunbanano.ui.intervention

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.lavidanoesunbanano.service.InterventionLauncher
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InterventionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_REASON = "extra_reason"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InterventionLauncher.isInterventionCurrentlyVisible.set(true)
    }

    override fun onResume() {
        super.onResume()
        InterventionLauncher.isInterventionCurrentlyVisible.set(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        InterventionLauncher.isInterventionCurrentlyVisible.set(false)
    }
}
