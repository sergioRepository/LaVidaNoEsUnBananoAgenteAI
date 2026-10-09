package org.platica.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.platica.demo.domain.repository.SettingsRepository
import org.platica.demo.ui.navigation.AppNavigation
import org.platica.demo.ui.navigation.Screen
import org.platica.demo.ui.theme.LaVidaNoEsUnBananoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LaVidaNoEsUnBananoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val settingsState by settingsRepository.getSettings().collectAsState(initial = null)

                    val startDestination = if (settingsState?.onboardingCompleted == true) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Onboarding.route
                    }

                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
