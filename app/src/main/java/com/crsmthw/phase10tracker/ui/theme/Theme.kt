package com.crsmthw.phase10tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.crsmthw.phase10tracker.data.ThemeMode

// ── Custom fallback palette (used on devices below Android 12) ─────────────────
// Deep indigo + amber accent — bold, game-night feel

private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF9B8DFF),   // soft violet
    onPrimary            = Color(0xFF1A0070),
    primaryContainer     = Color(0xFF2D1FA8),
    onPrimaryContainer   = Color(0xFFD8D0FF),

    secondary            = Color(0xFFFFBB33),   // warm amber
    onSecondary          = Color(0xFF3D2800),
    secondaryContainer   = Color(0xFF593D00),
    onSecondaryContainer = Color(0xFFFFDFA0),

    tertiary             = Color(0xFF5CE0C0),   // teal
    onTertiary           = Color(0xFF003730),
    tertiaryContainer    = Color(0xFF004E43),
    onTertiaryContainer  = Color(0xFF77FADA),

    background           = Color(0xFF0E0E18),
    onBackground         = Color(0xFFE8E5FF),
    surface              = Color(0xFF141425),
    onSurface            = Color(0xFFE8E5FF),
    surfaceVariant       = Color(0xFF1F1F38),
    onSurfaceVariant     = Color(0xFFBBB8D4),

    outline              = Color(0xFF6B6890),
    outlineVariant       = Color(0xFF3A3860),
    inverseSurface       = Color(0xFFE8E5FF),
    inverseOnSurface     = Color(0xFF1A1830),
    inversePrimary       = Color(0xFF3A2FBF),

    error                = Color(0xFFFF6B6B),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),

    scrim                = Color(0xFF000000),
)

private val LightColorScheme = lightColorScheme(
    primary              = Color(0xFF3A2FBF),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFD8D0FF),
    onPrimaryContainer   = Color(0xFF1A0070),

    secondary            = Color(0xFF8A5E00),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFFFDFA0),
    onSecondaryContainer = Color(0xFF3D2800),

    tertiary             = Color(0xFF006B5E),
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFB8F0E6),
    onTertiaryContainer  = Color(0xFF004E43),

    background           = Color(0xFFF5F3FF),
    onBackground         = Color(0xFF1A1830),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1A1830),
    surfaceVariant       = Color(0xFFEAE6FF),
    onSurfaceVariant     = Color(0xFF4A4670),

    outline              = Color(0xFF7B78A0),
    outlineVariant       = Color(0xFFCCC9E8),
    inverseSurface       = Color(0xFF302E47),
    inverseOnSurface     = Color(0xFFF5F3FF),
    inversePrimary       = Color(0xFF9B8DFF),

    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),

    scrim                = Color(0xFF000000),
)

// ── AMOLED Pure Black overlay ──────────────────────────────────────────────────
// Applied on top of whichever dark scheme is active (static or dynamic).
// Overrides only the surface / background tokens so accent colours are preserved.

private fun ColorScheme.withAmoledBlack(): ColorScheme = copy(
    background       = Color(0xFF000000),
    onBackground     = Color(0xFFE8E5FF),
    surface          = Color(0xFF000000),
    onSurface        = Color(0xFFE8E5FF),
    surfaceVariant   = Color(0xFF0D0D14),
    surfaceTint      = Color(0xFF9B8DFF),
    inverseSurface   = Color(0xFFE8E5FF),
    inverseOnSurface = Color(0xFF000000),
)

// ── Smooth animated ColorScheme ────────────────────────────────────────────────
// Animates every individual colour token so transitions between
// light <-> dark <-> AMOLED are smooth rather than abrupt.

private val colorAnimSpec: AnimationSpec<Color> =
    tween(durationMillis = 450, easing = FastOutSlowInEasing)

@Composable
private fun animatedColorScheme(target: ColorScheme): ColorScheme {

    @Composable
    fun ac(c: Color): Color = animateColorAsState(c, colorAnimSpec, label = "").value

    return target.copy(
        primary              = ac(target.primary),
        onPrimary            = ac(target.onPrimary),
        primaryContainer     = ac(target.primaryContainer),
        onPrimaryContainer   = ac(target.onPrimaryContainer),
        secondary            = ac(target.secondary),
        onSecondary          = ac(target.onSecondary),
        secondaryContainer   = ac(target.secondaryContainer),
        onSecondaryContainer = ac(target.onSecondaryContainer),
        tertiary             = ac(target.tertiary),
        onTertiary           = ac(target.onTertiary),
        tertiaryContainer    = ac(target.tertiaryContainer),
        onTertiaryContainer  = ac(target.onTertiaryContainer),
        background           = ac(target.background),
        onBackground         = ac(target.onBackground),
        surface              = ac(target.surface),
        onSurface            = ac(target.onSurface),
        surfaceVariant       = ac(target.surfaceVariant),
        onSurfaceVariant     = ac(target.onSurfaceVariant),
        surfaceTint          = ac(target.surfaceTint),
        inverseSurface       = ac(target.inverseSurface),
        inverseOnSurface     = ac(target.inverseOnSurface),
        inversePrimary       = ac(target.inversePrimary),
        error                = ac(target.error),
        onError              = ac(target.onError),
        errorContainer       = ac(target.errorContainer),
        onErrorContainer     = ac(target.onErrorContainer),
        outline              = ac(target.outline),
        outlineVariant       = ac(target.outlineVariant),
        scrim                = ac(target.scrim),
    )
}

// ── Public theme entry-point ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Phase10Theme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    amoledBlack: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> systemInDark
    }

    // Resolve base scheme (dynamic on API 31+, static fallback below)
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else   -> LightColorScheme
    }

    // Overlay AMOLED black on top of whichever dark scheme we resolved
    val targetScheme = if (amoledBlack && isDark) baseScheme.withAmoledBlack() else baseScheme

    // Every colour token animates individually -- buttery-smooth transitions
    val colorScheme = animatedColorScheme(targetScheme)

    // ── Keep system bar icon tint in sync with the active theme ───────────────
    // SideEffect fires after every successful composition, so it stays in sync
    // even after navigating away or dismissing overlays (sheets, dialogs, etc.)
    // that temporarily take over the window appearance.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                // Light bars = dark icons (readable on light backgrounds)
                // Dark bars  = light icons (readable on dark backgrounds)
                isAppearanceLightStatusBars     = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    // Expressive theme wires MotionScheme.expressive() so every spatial spec
    // (motionScheme.defaultSpatialSpec) carries the M3 Expressive spring/overshoot.
    MaterialExpressiveTheme(
        colorScheme  = colorScheme,
        typography   = Phase10Typography,
        shapes       = Phase10Shapes,
        motionScheme = MotionScheme.expressive(),
        content      = content
    )
}
