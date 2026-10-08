package com.lavidanoesunbanano.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = DarkGreenBackground,
    primaryContainer = DarkGreenSurfaceVariant,
    onPrimaryContainer = DarkGreenOnSurface,
    secondary = DarkGreenAccent,
    onSecondary = DarkGreenBackground,
    background = DarkGreenBackground,
    onBackground = DarkGreenOnSurface,
    surface = DarkGreenSurface,
    onSurface = DarkGreenOnSurface,
    surfaceVariant = DarkGreenSurfaceVariant,
    onSurfaceVariant = DarkGreenOnSurface,
    error = SoftRed,
    onError = DarkGreenBackground
)

@Composable
fun LaVidaNoEsUnBananoTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkGreenBackground.toArgb()
                window.navigationBarColor = DarkGreenBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
