package com.lavidanoesunbanano.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object AppSelection : Screen("app_selection")
    data object RiskConfig : Screen("risk_config")
    data object Dashboard : Screen("dashboard")
    data object Settings : Screen("settings")
}
