package org.platica.demo.ui.intervention

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import org.platica.demo.service.InterventionLauncher
import org.platica.demo.ui.theme.LaVidaNoEsUnBananoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InterventionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_REASON = "extra_reason"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
    }

    private val viewModel: InterventionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InterventionLauncher.isInterventionCurrentlyVisible.set(true)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val reason = intent.getStringExtra(EXTRA_REASON)
        val durationSeconds = intent.getLongExtra(EXTRA_DURATION_SECONDS, 0L)

        viewModel.initIntervention(packageName, reason, durationSeconds)

        setContent {
            LaVidaNoEsUnBananoTheme {
                InterventionScreen(
                    viewModel = viewModel,
                    onExitApp = {
                        goToHome()
                        finish()
                    },
                    onContinueApp = {
                        finish()
                    },
                    onSnoozeApp = {
                        goToHome()
                        finish()
                    }
                )
            }
        }
    }

    private fun goToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
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
