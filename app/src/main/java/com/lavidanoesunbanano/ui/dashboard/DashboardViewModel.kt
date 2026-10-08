package com.lavidanoesunbanano.ui.dashboard

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lavidanoesunbanano.domain.model.InterventionDecision
import com.lavidanoesunbanano.domain.model.InterventionRecord
import com.lavidanoesunbanano.domain.repository.InterventionRepository
import com.lavidanoesunbanano.domain.repository.SessionRepository
import com.lavidanoesunbanano.domain.time.Clock
import com.lavidanoesunbanano.domain.time.LogicalDateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import javax.inject.Inject

data class DashboardItemUi(
    val id: Long,
    val timeFormatted: String,
    val appName: String,
    val reasonText: String,
    val decision: InterventionDecision,
    val durationFormatted: String
)

data class DashboardUiState(
    val totalTriggerTimeFormatted: String = "0 min",
    val interventionCount: Int = 0,
    val successRateText: String = "—",
    val showSelfCompassionMessage: Boolean = false,
    val hourlyInterventions: Map<Int, Int> = emptyMap(),
    val recentInterventions: List<DashboardItemUi> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interventionRepository: InterventionRepository,
    private val sessionRepository: SessionRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val now = clock.now()
        val zone = clock.zoneId()
        val logicalDate = LogicalDateUtils.getLogicalDate(now, zone)
        val startInstant = LogicalDateUtils.getLogicalDayStartInstant(logicalDate, zone)
        val endInstant = LogicalDateUtils.getLogicalDayEndInstant(logicalDate, zone)

        // Observar intervenciones del día lógico
        interventionRepository.getInterventionsBetween(
            startInstant.toEpochMilli(),
            endInstant.toEpochMilli()
        ).onEach { eventsToday ->
            val totalInterventions = eventsToday.size
            val exitCount = eventsToday.count { it.decision == InterventionDecision.EXIT }
            val continueCount = eventsToday.count { it.decision == InterventionDecision.CONTINUE }

            val successRate = if (totalInterventions > 0) {
                val percentage = ((exitCount.toDouble() / totalInterventions) * 100).toInt()
                "$percentage%"
            } else {
                "—"
            }

            // Total de tiempo acumulado en segundos de sesiones hoy
            val totalSeconds = eventsToday.sumOf { it.sessionDurationSeconds }
            val timeFormatted = formatDuration(totalSeconds)

            // Distribución de intervenciones por hora (0..23)
            val hourlyMap = (0..23).associateWith { 0 }.toMutableMap()
            eventsToday.forEach { event ->
                val hour = LocalDateTime.ofInstant(event.timestamp, zone).hour
                hourlyMap[hour] = (hourlyMap[hour] ?: 0) + 1
            }

            // Últimas intervenciones con nombres legibles
            val pm = context.packageManager
            val recentItems = eventsToday.take(10).map { event ->
                val appLabel = try {
                    val appInfo = pm.getApplicationInfo(event.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    event.packageName
                }
                val localTime = LocalDateTime.ofInstant(event.timestamp, zone)
                val timeStr = String.format("%02d:%02d", localTime.hour, localTime.minute)

                DashboardItemUi(
                    id = event.id,
                    timeFormatted = timeStr,
                    appName = appLabel,
                    reasonText = event.reason.value,
                    decision = event.decision,
                    durationFormatted = formatDuration(event.sessionDurationSeconds)
                )
            }

            _uiState.update {
                it.copy(
                    totalTriggerTimeFormatted = timeFormatted,
                    interventionCount = totalInterventions,
                    successRateText = successRate,
                    showSelfCompassionMessage = continueCount >= 2,
                    hourlyInterventions = hourlyMap,
                    recentInterventions = recentItems,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun formatDuration(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return if (hours > 0) {
            "${hours} h ${remainingMinutes} min"
        } else {
            "${minutes} min"
        }
    }
}
