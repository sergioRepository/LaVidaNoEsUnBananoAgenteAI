package org.platica.demo.ui.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.platica.demo.domain.repository.InterventionRepository
import org.platica.demo.domain.repository.SessionRepository
import org.platica.demo.domain.repository.SettingsRepository
import org.platica.demo.domain.repository.TriggerAppRepository
import org.platica.demo.service.InterventionLauncher
import org.platica.demo.service.UsageMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val aiResponsesEnabled: Boolean = false,
    val showAiDisclaimerDialog: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val userId: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val triggerAppRepository: TriggerAppRepository,
    private val sessionRepository: SessionRepository,
    private val interventionRepository: InterventionRepository,
    private val interventionLauncher: InterventionLauncher
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        settingsRepository.getSettings().onEach { settings ->
            _uiState.update {
                it.copy(
                    aiResponsesEnabled = settings.aiResponsesEnabled,
                    userId = settings.userId
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onToggleAi(requestedEnabled: Boolean) {
        if (requestedEnabled) {
            // Mostrar aviso sobre privacidad y envío de texto libre
            _uiState.update { it.copy(showAiDisclaimerDialog = true) }
        } else {
            viewModelScope.launch {
                settingsRepository.updateAiResponsesEnabled(false)
            }
        }
    }

    fun confirmEnableAi() {
        viewModelScope.launch {
            settingsRepository.updateAiResponsesEnabled(true)
            _uiState.update { it.copy(showAiDisclaimerDialog = false) }
        }
    }

    fun dismissAiDisclaimer() {
        _uiState.update { it.copy(showAiDisclaimerDialog = false) }
    }

    fun onRequestClearData() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    fun dismissClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    fun confirmClearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Detener el servicio en primer plano
            val serviceIntent = Intent(context, UsageMonitorService::class.java)
            context.stopService(serviceIntent)

            // Borrar Room y DataStore y regenerar user_id
            triggerAppRepository.clearAll()
            sessionRepository.clearAll()
            interventionRepository.clearAll()
            settingsRepository.resetAllData()

            _uiState.update { it.copy(showClearDataDialog = false) }
            onComplete()
        }
    }

    fun simulateIntervention() {
        interventionLauncher.launchSimulation()
    }
}
