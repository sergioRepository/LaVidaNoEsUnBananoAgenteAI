package org.platica.demo.ui.intervention

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.platica.demo.data.local.AppCategoryMapper
import org.platica.demo.domain.agent.AgentClient
import org.platica.demo.domain.crisis.CrisisFilter
import org.platica.demo.domain.model.AgentRequest
import org.platica.demo.domain.model.AgentResponse
import org.platica.demo.domain.model.AgentStrategy
import org.platica.demo.domain.model.InterventionDecision
import org.platica.demo.domain.model.InterventionReason
import org.platica.demo.domain.model.InterventionRecord
import org.platica.demo.domain.repository.InterventionRepository
import org.platica.demo.domain.repository.SessionRepository
import org.platica.demo.domain.repository.SettingsRepository
import org.platica.demo.domain.repository.TriggerAppRepository
import org.platica.demo.domain.time.Clock
import org.platica.demo.domain.time.LogicalDateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class InterventionUiState(
    val packageName: String = "",
    val appName: String = "",
    val reason: InterventionReason = InterventionReason.TRIGGER_APP_IN_RISK_WINDOW,
    val sessionDurationSeconds: Long = 0,
    val secondsRemaining: Int = 30,
    val breathingFinished: Boolean = false,
    val selectedEmotion: String? = null,
    val userText: String = "",
    val agentResponse: AgentResponse? = null,
    val isCrisisDetected: Boolean = false,
    val decisionMade: Boolean = false
) {
    val showAgentCard: Boolean
        get() = breathingFinished && agentResponse != null
}

@HiltViewModel
class InterventionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interventionRepository: InterventionRepository,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val triggerAppRepository: TriggerAppRepository,
    private val agentClient: AgentClient,
    private val crisisFilter: CrisisFilter,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterventionUiState())
    val uiState: StateFlow<InterventionUiState> = _uiState.asStateFlow()

    private val hasRecordedDecision = AtomicBoolean(false)
    private var isInitialized = false

    fun initIntervention(
        packageName: String,
        reasonString: String?,
        sessionDurationSeconds: Long
    ) {
        if (isInitialized) return
        isInitialized = true

        val reason = try {
            InterventionReason.valueOf(reasonString ?: "")
        } catch (_: Exception) {
            InterventionReason.TRIGGER_APP_IN_RISK_WINDOW
        }

        // Obtener nombre local de la aplicación
        val appName = try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }

        _uiState.update {
            it.copy(
                packageName = packageName,
                appName = appName,
                reason = reason,
                sessionDurationSeconds = sessionDurationSeconds,
                secondsRemaining = 30,
                breathingFinished = false
            )
        }

        // 1. Iniciar cuenta regresiva de respiración de 30 s
        startBreathingTimer()

        // 2. Disparar llamada al agente en paralelo
        triggerAgentCall()
    }

    private fun startBreathingTimer() {
        viewModelScope.launch {
            for (sec in 29 downTo 0) {
                delay(1000)
                _uiState.update { it.copy(secondsRemaining = sec) }
            }
            _uiState.update { it.copy(breathingFinished = true) }
        }
    }

    private fun triggerAgentCall() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val now = clock.now()
            val zone = clock.zoneId()
            val localTime = LocalDateTime.ofInstant(now, zone)
            val settings = settingsRepository.getSettings().first()

            // Comprobar filtro de crisis localmente
            val hasLocalCrisis = crisisFilter.containsCrisisKeywords(state.userText)
            if (hasLocalCrisis) {
                _uiState.update { it.copy(isCrisisDetected = true) }
            }

            // Calcular relapsed_today en base al día lógico
            val logicalDate = LogicalDateUtils.getLogicalDate(now, zone)
            val logicalDayStart = LogicalDateUtils.getLogicalDayStartInstant(logicalDate, zone)
            val eventsToday = interventionRepository.getInterventionsBetweenDirect(
                logicalDayStart.toEpochMilli(),
                now.toEpochMilli()
            )
            val relapsedToday = eventsToday.any { it.decision == InterventionDecision.CONTINUE }

            // Mapear categoría sin enviar nunca el nombre del paquete al agente
            val appCategory = try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(state.packageName, 0)
                AppCategoryMapper.mapCategory(appInfo, state.packageName).value
            } catch (_: Exception) {
                "otra"
            }

            // Si hay crisis local, no enviamos el texto de usuario al backend
            val textToSend = if (hasLocalCrisis) null else state.userText.ifBlank { null }

            val request = AgentRequest(
                userId = settings.userId,
                appCategory = appCategory,
                sessionMinutes = (state.sessionDurationSeconds / 60).toInt().coerceAtLeast(1),
                localHour = localTime.hour,
                reason = state.reason.value,
                emotion = state.selectedEmotion,
                userText = textToSend,
                relapsedToday = relapsedToday
            )

            try {
                val response = agentClient.respond(request)
                _uiState.update {
                    it.copy(
                        agentResponse = response,
                        isCrisisDetected = it.isCrisisDetected || response.isCrisis
                    )
                }
            } catch (e: Exception) {
                // Fallback automático en caso de excepción
                val fallbackResponse = AgentResponse(
                    message = "Respira con calma. Tomarte unos instantes aquí te ayuda a decidir conscientemente qué hacer hoy.",
                    strategy = AgentStrategy.REFRAME,
                    isCrisis = false
                )
                _uiState.update { it.copy(agentResponse = fallbackResponse) }
            }
        }
    }

    fun onEmotionSelected(emotion: String) {
        val current = _uiState.value.selectedEmotion
        val newEmotion = if (current == emotion) null else emotion
        _uiState.update { it.copy(selectedEmotion = newEmotion) }
    }

    fun onUserTextChanged(text: String) {
        _uiState.update { it.copy(userText = text) }
        if (crisisFilter.containsCrisisKeywords(text)) {
            _uiState.update { it.copy(isCrisisDetected = true) }
        }
    }

    /**
     * Registra exactamente una vez la decisión tomada.
     */
    fun recordDecision(decision: InterventionDecision, onFinished: () -> Unit) {
        if (!hasRecordedDecision.compareAndSet(false, true)) {
            onFinished()
            return
        }

        viewModelScope.launch {
            val state = _uiState.value
            val record = InterventionRecord(
                timestamp = clock.now(),
                packageName = state.packageName,
                reason = state.reason,
                decision = decision,
                sessionDurationSeconds = state.sessionDurationSeconds
            )

            interventionRepository.recordIntervention(
                record = record,
                emotion = state.selectedEmotion,
                userText = state.userText.ifBlank { null }
            )

            if (decision == InterventionDecision.EXIT) {
                sessionRepository.clearActiveSession(state.packageName)
            }

            _uiState.update { it.copy(decisionMade = true) }
            onFinished()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Si el sistema destruye la Activity antes de pulsar un botón, registrar SNOOZE por defecto
        if (hasRecordedDecision.compareAndSet(false, true)) {
            val state = _uiState.value
            if (state.packageName.isNotBlank()) {
                val record = InterventionRecord(
                    timestamp = clock.now(),
                    packageName = state.packageName,
                    reason = state.reason,
                    decision = InterventionDecision.SNOOZE,
                    sessionDurationSeconds = state.sessionDurationSeconds
                )
                runBlocking {
                    interventionRepository.recordIntervention(
                        record = record,
                        emotion = state.selectedEmotion,
                        userText = state.userText.ifBlank { null }
                    )
                }
            }
        }
    }
}
