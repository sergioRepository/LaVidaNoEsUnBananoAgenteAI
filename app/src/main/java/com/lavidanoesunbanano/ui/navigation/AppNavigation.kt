package com.lavidanoesunbanano.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lavidanoesunbanano.ui.apps.AppSelectionScreen
import com.lavidanoesunbanano.ui.apps.AppSelectionViewModel
import com.lavidanoesunbanano.ui.config.RiskConfigScreen
import com.lavidanoesunbanano.ui.config.RiskConfigViewModel
import com.lavidanoesunbanano.ui.dashboard.DashboardPlaceholderScreen
import com.lavidanoesunbanano.ui.onboarding.OnboardingScreen
import com.lavidanoesunbanano.ui.onboarding.OnboardingViewModel
import com.lavidanoesunbanano.ui.settings.SettingsPlaceholderScreen

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
            DashboardPlaceholderScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsPlaceholderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
