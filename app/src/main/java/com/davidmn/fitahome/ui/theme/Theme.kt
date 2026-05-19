package com.davidmn.fitahome.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores modo claro
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF),          // Azul Apple
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F1FF),
    background = Color(0xFFFFFFFF),       // Blanco puro
    onBackground = Color(0xFF000000),     // Negro puro
    surface = Color(0xFFF5F5F5),          // Gris suave tarjetas
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF6E6E73), // Gris Apple subtítulos
    outline = Color(0xFFD1D1D6),
    secondary = Color(0xFF34C759),        // Verde Apple
    tertiary = Color(0xFFFF9500)          // Naranja Apple
)

// Colores modo oscuro
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),          // Azul Apple dark
    onPrimary = Color.White,
    primaryContainer = Color(0xFF003366),
    background = Color(0xFF000000),       // Negro puro
    onBackground = Color(0xFFFFFFFF),     // Blanco puro
    surface = Color(0xFF1C1C1E),          // Gris oscuro Apple tarjetas
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2E),   // Gris más claro Apple
    onSurfaceVariant = Color(0xFF8E8E93), // Gris Apple subtítulos dark
    outline = Color(0xFF38383A),
    secondary = Color(0xFF30D158),        // Verde Apple dark
    tertiary = Color(0xFFFF9F0A)          // Naranja Apple dark
)

@Composable
fun FitaHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}