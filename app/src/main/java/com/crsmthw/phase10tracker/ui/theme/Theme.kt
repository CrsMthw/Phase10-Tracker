package com.crsmthw.phase10tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── Custom fallback palette (used on devices below Android 12) ───────────────
// Deep indigo + amber accent — bold, game-night feel

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF9B8DFF),   // soft violet
    onPrimary        = Color(0xFF1A0070),
    primaryContainer = Color(0xFF2D1FA8),
    onPrimaryContainer = Color(0xFFD8D0FF),

    secondary        = Color(0xFFFFBB33),   // warm amber
    onSecondary      = Color(0xFF3D2800),
    secondaryContainer = Color(0xFF593D00),
    onSecondaryContainer = Color(0xFFFFDFA0),

    tertiary         = Color(0xFF5CE0C0),   // teal
    onTertiary       = Color(0xFF003730),

    background       = Color(0xFF0E0E18),
    onBackground     = Color(0xFFE8E5FF),
    surface          = Color(0xFF141425),
    onSurface        = Color(0xFFE8E5FF),
    surfaceVariant   = Color(0xFF1F1F38),
    onSurfaceVariant = Color(0xFFBBB8D4),

    error            = Color(0xFFFF6B6B),
    onError          = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF3A2FBF),
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8D0FF),
    onPrimaryContainer = Color(0xFF1A0070),

    secondary        = Color(0xFF8A5E00),
    onSecondary      = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDFA0),
    onSecondaryContainer = Color(0xFF3D2800),

    tertiary         = Color(0xFF006B5E),
    onTertiary       = Color(0xFFFFFFFF),

    background       = Color(0xFFF5F3FF),
    onBackground     = Color(0xFF1A1830),
    surface          = Color(0xFFFFFFFF),
    onSurface        = Color(0xFF1A1830),
    surfaceVariant   = Color(0xFFEAE6FF),
    onSurfaceVariant = Color(0xFF4A4670),

    error            = Color(0xFFBA1A1A),
    onError          = Color(0xFFFFFFFF),
)

@Composable
fun Phase10Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Phase10Typography,
        shapes = Phase10Shapes,
        content = content
    )
}
