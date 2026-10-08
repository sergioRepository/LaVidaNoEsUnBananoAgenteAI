package com.lavidanoesunbanano.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lavidanoesunbanano.ui.theme.DarkGreenAccent
import com.lavidanoesunbanano.ui.theme.DarkGreenPrimary
import com.lavidanoesunbanano.ui.theme.DarkGreenSurfaceVariant

/**
 * Gráfico de barras por hora del día (0..23) implementado exclusivamente con Canvas de Compose sin librerías.
 */
@Composable
fun HourlyChartCanvas(
    hourlyInterventions: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    val maxCount = (hourlyInterventions.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkGreenSurfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Distribución de intervenciones por hora",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                val totalBars = 24
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (totalBars - 1)
                val barWidth = (size.width - totalSpacing) / totalBars
                val bottomY = size.height - 10.dp.toPx()
                val availableHeight = bottomY - 10.dp.toPx()

                // Línea base suave
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, bottomY),
                    end = Offset(size.width, bottomY),
                    strokeWidth = 2f
                )

                for (hour in 0..23) {
                    val count = hourlyInterventions[hour] ?: 0
                    val barHeight = if (count > 0) {
                        (count.toFloat() / maxCount.toFloat()) * availableHeight
                    } else {
                        4.dp.toPx() // Pequeña marca para horas sin intervenciones
                    }

                    val left = hour * (barWidth + barSpacing)
                    val top = bottomY - barHeight

                    val barColor = if (count > 0) DarkGreenAccent else DarkGreenPrimary.copy(alpha = 0.3f)

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Etiquetas de horas guía: 00h, 06h, 12h, 18h, 23h
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "00h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "06h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "12h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "18h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "23h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
