package org.platica.demo.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.platica.demo.domain.model.RiskSchedule
import org.platica.demo.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RiskConfigUiState(
    val startHour: Int = 22,
    val startMinute: Int = 0,
    val endHour: Int = 2,
    val endMinute: Int = 0,
    val sessionThresholdMinutes: Int = 15,
    val isSameTimeError: Boolean = false,
    val isSaved: Boolean = false
) {
    val crossesMidnight: Boolean
        get() = (startHour * 60 + startMinute) > (endHour * 60 + endMinute)

    val isValid: Boolean
        get() = (startHour * 60 + startMinute) != (endHour * 60 + endMinute)
}

@HiltViewModel
class RiskConfigViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiskConfigUiState())
    val uiState: StateFlow<RiskConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings().first()
            _uiState.update {
                it.copy(
                    startHour = settings.riskSchedule.startHour,
                    startMinute = settings.riskSchedule.startMinute,
                    endHour = settings.riskSchedule.endHour,
                    endMinute = settings.riskSchedule.endMinute,
                    sessionThresholdMinutes = settings.sessionThresholdMinutes
                )
            }
        }
    }

    fun updateStartTime(hour: Int, minute: Int) {
        _uiState.update {
            val isSame = (hour * 60 + minute) == (it.endHour * 60 + it.endMinute)
            it.copy(
                startHour = hour,
                startMinute = minute,
                isSameTimeError = isSame
            )
        }
    }

    fun updateEndTime(hour: Int, minute: Int) {
        _uiState.update {
            val isSame = (it.startHour * 60 + it.startMinute) == (hour * 60 + minute)
            it.copy(
                endHour = hour,
                endMinute = minute,
                isSameTimeError = isSame
            )
        }
    }

    fun updateThreshold(minutes: Int) {
        _uiState.update { it.copy(sessionThresholdMinutes = minutes) }
    }

    fun saveAndStartMonitoring(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update { it.copy(isSameTimeError = true) }
            return
        }

        viewModelScope.launch {
            val schedule = RiskSchedule(
                startHour = state.startHour,
                startMinute = state.startMinute,
                endHour = state.endHour,
                endMinute = state.endMinute
            )
            settingsRepository.updateRiskSchedule(schedule)
            settingsRepository.updateSessionThresholdMinutes(state.sessionThresholdMinutes)
            settingsRepository.updateMonitoringEnabled(true)
            _uiState.update { it.copy(isSaved = true) }
            onSuccess()
        }
    }
}
