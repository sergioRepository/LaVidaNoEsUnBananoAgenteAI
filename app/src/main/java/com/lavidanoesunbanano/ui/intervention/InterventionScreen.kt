package com.lavidanoesunbanano.ui.intervention

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lavidanoesunbanano.R
import com.lavidanoesunbanano.domain.model.AgentStrategy
import com.lavidanoesunbanano.domain.model.InterventionDecision
import com.lavidanoesunbanano.ui.theme.DarkGreenBackground
import com.lavidanoesunbanano.ui.theme.DarkGreenSurface
import com.lavidanoesunbanano.ui.theme.DarkGreenSurfaceVariant
import com.lavidanoesunbanano.ui.theme.SoftRed
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterventionScreen(
    viewModel: InterventionViewModel,
    onExitApp: () -> Unit,
    onContinueApp: () -> Unit,
    onSnoozeApp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Regla de producto: Botón Atrás en la intervención equivale a "Posponer 15 min"
    BackHandler {
        viewModel.recordDecision(InterventionDecision.SNOOZE) {
            onSnoozeApp()
        }
    }

    val minutesInApp = (uiState.sessionDurationSeconds / 60).coerceAtLeast(1)

    Scaffold(
        containerColor = DarkGreenBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGreenBackground)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Salir de la app
                Button(
                    onClick = {
                        viewModel.recordDecision(InterventionDecision.EXIT) {
                            onExitApp()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_exit_app),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón Continuar 10 min
                    OutlinedButton(
                        onClick = {
                            viewModel.recordDecision(InterventionDecision.CONTINUE) {
                                onContinueApp()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_continue_10m),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Botón Posponer 15 min
                    OutlinedButton(
                        onClick = {
                            viewModel.recordDecision(InterventionDecision.SNOOZE) {
                                onSnoozeApp()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_snooze_15m),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mensaje breve sin juicio
            Text(
                text = String.format(
                    Locale.getDefault(),
                    stringResource(R.string.intervention_default_message),
                    minutesInApp,
                    uiState.appName.ifBlank { "esta app" }
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Respiración guiada de 30 s
            BreathingCanvas(
                secondsRemaining = uiState.secondsRemaining,
                isCompleted = uiState.breathingFinished
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de Líneas de Ayuda si se detecta crisis
            if (uiState.isCrisisDetected) {
                CrisisHelplineCard(
                    onCall = { number ->
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$number")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(dialIntent)
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Tarjeta del Agente de IA (solo cuando terminó la respiración y llegó la respuesta)
            if (uiState.showAgentCard) {
                uiState.agentResponse?.let { response ->
                    AgentResponseCard(response = response)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Chips emocionales opcionales
            Text(
                text = stringResource(R.string.emotion_question),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val emotions = listOf(
                "aburrimiento" to stringResource(R.string.emotion_boredom),
                "ansiedad" to stringResource(R.string.emotion_anxiety),
                "cansancio" to stringResource(R.string.emotion_tiredness),
                "estrés" to stringResource(R.string.emotion_stress),
                "costumbre" to stringResource(R.string.emotion_habit)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                emotions.forEach { (key, label) ->
                    FilterChip(
                        selected = uiState.selectedEmotion == key,
                        onClick = { viewModel.onEmotionSelected(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campo de texto libre opcional
            OutlinedTextField(
                value = uiState.userText,
                onValueChange = { viewModel.onUserTextChanged(it) },
                placeholder = { Text(stringResource(R.string.user_text_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CrisisHelplineCard(
    onCall: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SoftRed.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = SoftRed
                )
                Text(
                    text = stringResource(R.string.crisis_alert_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SoftRed,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.crisis_alert_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            val lines = listOf(
                Pair(stringResource(R.string.linea_106_name), stringResource(R.string.linea_106_number)),
                Pair(stringResource(R.string.linea_192_name), stringResource(R.string.linea_192_number)),
                Pair(stringResource(R.string.linea_123_name), stringResource(R.string.linea_123_number))
            )

            lines.forEach { (name, number) ->
                Button(
                    onClick = { onCall(number) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftRed.copy(alpha = 0.85f),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "$name ($number)")
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentResponseCard(
    response: com.lavidanoesunbanano.domain.model.AgentResponse
) {
    val (icon: ImageVector, actionTitle: String) = when (response.strategy) {
        AgentStrategy.BREATHING -> Pair(Icons.Default.Spa, "Tomar 3 respiraciones profundas más")
        AgentStrategy.REFRAME -> Pair(Icons.Default.Psychology, "¿Qué necesidad intentas llenar?")
        AgentStrategy.ALTERNATIVE -> Pair(Icons.Default.DirectionsWalk, "Dar una caminata de 5 min")
        AgentStrategy.RELAPSE_SUPPORT -> Pair(Icons.Default.Favorite, "Cada intento cuenta, continúa con calma")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = actionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = response.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
