package org.platica.demo.ui.intervention

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.platica.demo.R
import org.platica.demo.ui.theme.DarkGreenAccent
import org.platica.demo.ui.theme.DarkGreenPrimary

/**
 * Círculo animado de respiración guiada de 30 s:
 * Inhalar 4 s (expandir), Sostener 4 s (pausa), Exhalar 6 s (contraer). Ciclo de 14 s.
 */
@Composable
fun BreathingCanvas(
    secondsRemaining: Int,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_transition")

    // Ciclo de 14.000 ms: 0-4000ms inhalar (0.4f -> 1.0f), 4000-8000ms sostener (1.0f), 8000-14000ms exhalar (1.0f -> 0.4f)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 14000
                0.45f at 0 with LinearEasing
                1.0f at 4000 with LinearEasing // Fin inhalación (4s)
                1.0f at 8000 with LinearEasing // Fin retención (4s)
                0.45f at 14000 with LinearEasing // Fin exhalación (6s)
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "breathing_scale"
    )

    // Determinar la fase actual del ciclo para el texto
    val cycleProgressMs = (30 - secondsRemaining) * 1000 % 14000
    val phaseText = when {
        isCompleted -> stringResource(R.string.breathing_completed)
        cycleProgressMs < 4000 -> stringResource(R.string.breathing_inhale)
        cycleProgressMs < 8000 -> stringResource(R.string.breathing_hold)
        else -> stringResource(R.string.breathing_exhale)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(190.dp)) {
                val currentRadius = (size.minDimension / 2f) * if (isCompleted) 0.65f else scale
                val centerOffset = center

                // Halo difuso exterior
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DarkGreenAccent.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = currentRadius * 1.25f
                    ),
                    radius = currentRadius * 1.25f,
                    center = centerOffset
                )

                // Círculo principal relajante
                drawCircle(
                    color = DarkGreenPrimary.copy(alpha = 0.7f),
                    radius = currentRadius,
                    center = centerOffset
                )
            }

            if (!isCompleted) {
                Text(
                    text = "${secondsRemaining}s",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = phaseText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
