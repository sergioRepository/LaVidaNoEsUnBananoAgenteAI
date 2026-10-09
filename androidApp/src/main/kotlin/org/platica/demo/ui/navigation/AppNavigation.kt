package org.platica.demo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.platica.demo.ui.apps.AppSelectionScreen
import org.platica.demo.ui.apps.AppSelectionViewModel
import org.platica.demo.ui.config.RiskConfigScreen
import org.platica.demo.ui.config.RiskConfigViewModel
import org.platica.demo.ui.dashboard.DashboardScreen
import org.platica.demo.ui.dashboard.DashboardViewModel
import org.platica.demo.ui.onboarding.OnboardingScreen
import org.platica.demo.ui.onboarding.OnboardingViewModel
import org.platica.demo.ui.settings.SettingsScreen
import org.platica.demo.ui.settings.SettingsViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onNavigateNext = {
                    navController.navigate(Screen.AppSelection.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AppSelection.route) {
            val viewModel: AppSelectionViewModel = hiltViewModel()
            AppSelectionScreen(
                viewModel = viewModel,
                onNavigateNext = {
                    navController.navigate(Screen.RiskConfig.route)
                }
            )
        }

        composable(Screen.RiskConfig.route) {
            val viewModel: RiskConfigViewModel = hiltViewModel()
            RiskConfigScreen(
                viewModel = viewModel,
                onMonitoringStarted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.RiskConfig.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToApps = {
                    navController.navigate(Screen.AppSelection.route)
                },
                onNavigateToSchedule = {
                    navController.navigate(Screen.RiskConfig.route)
                },
                onDataReset = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
