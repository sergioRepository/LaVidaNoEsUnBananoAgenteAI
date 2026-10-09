package org.platica.demo.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.platica.demo.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val hasUsageStats: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotifications: Boolean = false,
    val isIgnoringBattery: Boolean = false
) {
    val canProceed: Boolean
        get() = hasUsageStats && hasOverlay && hasNotifications
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun refreshPermissions(context: Context) {
        _uiState.update {
            it.copy(
                hasUsageStats = PermissionHelper.hasUsageStatsPermission(context),
                hasOverlay = PermissionHelper.hasOverlayPermission(context),
                hasNotifications = PermissionHelper.hasNotificationPermission(context),
                isIgnoringBattery = PermissionHelper.isIgnoringBatteryOptimizations(context)
            )
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        if (_uiState.value.canProceed) {
            viewModelScope.launch {
                settingsRepository.setOnboardingCompleted(true)
                onSuccess()
            }
        }
    }
}
