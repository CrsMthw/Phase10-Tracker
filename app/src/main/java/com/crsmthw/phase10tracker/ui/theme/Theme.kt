package com.crsmthw.phase10tracker.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** User-selectable theme mode. Persisted via [com.crsmthw.phase10tracker.data.repository.ThemePreferenceRepository]. */
enum class ThemePreference { LIGHT, DARK, AMOLED, SYSTEM }

/**
 * Resolves a [ThemePreference] to a concrete light/dark boolean given the current system state.
 * AMOLED is treated as dark (it IS dark, just blacker). Composable because [isSystemInDarkTheme]
 * is composable. Shared between [Phase10Theme] and the activity's system-bar styling code.
 */
@Composable
fun ThemePreference.isDark(): Boolean = when (this) {
    ThemePreference.LIGHT  -> false
    ThemePreference.DARK   -> true
    ThemePreference.AMOLED -> true
    ThemePreference.SYSTEM -> isSystemInDarkTheme()
}

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

// ── AMOLED variant ───────────────────────────────────────────────────────────
// Force surface tiers to pure (or near-pure) black while preserving the accent
// colors from whichever dark scheme is in use. Higher elevation tiers keep a
// faint dark tint so cards and dialogs remain visible against the black.

private fun ColorScheme.toAmoled(): ColorScheme = copy(
    background              = Color.Black,
    surface                 = Color.Black,
    surfaceDim              = Color.Black,
    surfaceContainerLowest  = Color.Black,
    surfaceContainerLow     = Color(0xFF050508),
    surfaceContainer        = Color(0xFF0A0A12),
    surfaceContainerHigh    = Color(0xFF11111C),
    surfaceContainerHighest = Color(0xFF181826),
    surfaceBright           = Color(0xFF1F1F30),
    surfaceVariant          = Color(0xFF0F0F1A),
)

private val AmoledColorScheme = DarkColorScheme.toAmoled()

// ── Animated color scheme ────────────────────────────────────────────────────
// Wrapping every slot in animateColorAsState turns the abrupt swap-on-recompose
// into a smooth interpolation. Each color animates independently — Compose
// remembers the previous value per color and tweens to the new target.

@Composable
private fun ColorScheme.animated(
    animationSpec: AnimationSpec<Color> = tween(durationMillis = 400, easing = FastOutSlowInEasing)
): ColorScheme = copy(
    primary                 = animateColorAsState(primary,                 animationSpec, label = "primary").value,
    onPrimary               = animateColorAsState(onPrimary,               animationSpec, label = "onPrimary").value,
    primaryContainer        = animateColorAsState(primaryContainer,        animationSpec, label = "primaryContainer").value,
    onPrimaryContainer      = animateColorAsState(onPrimaryContainer,      animationSpec, label = "onPrimaryContainer").value,
    inversePrimary          = animateColorAsState(inversePrimary,          animationSpec, label = "inversePrimary").value,
    secondary               = animateColorAsState(secondary,               animationSpec, label = "secondary").value,
    onSecondary             = animateColorAsState(onSecondary,             animationSpec, label = "onSecondary").value,
    secondaryContainer      = animateColorAsState(secondaryContainer,      animationSpec, label = "secondaryContainer").value,
    onSecondaryContainer    = animateColorAsState(onSecondaryContainer,    animationSpec, label = "onSecondaryContainer").value,
    tertiary                = animateColorAsState(tertiary,                animationSpec, label = "tertiary").value,
    onTertiary              = animateColorAsState(onTertiary,              animationSpec, label = "onTertiary").value,
    tertiaryContainer       = animateColorAsState(tertiaryContainer,       animationSpec, label = "tertiaryContainer").value,
    onTertiaryContainer     = animateColorAsState(onTertiaryContainer,     animationSpec, label = "onTertiaryContainer").value,
    background              = animateColorAsState(background,              animationSpec, label = "background").value,
    onBackground            = animateColorAsState(onBackground,            animationSpec, label = "onBackground").value,
    surface                 = animateColorAsState(surface,                 animationSpec, label = "surface").value,
    onSurface               = animateColorAsState(onSurface,               animationSpec, label = "onSurface").value,
    surfaceVariant          = animateColorAsState(surfaceVariant,          animationSpec, label = "surfaceVariant").value,
    onSurfaceVariant        = animateColorAsState(onSurfaceVariant,        animationSpec, label = "onSurfaceVariant").value,
    surfaceTint             = animateColorAsState(surfaceTint,             animationSpec, label = "surfaceTint").value,
    inverseSurface          = animateColorAsState(inverseSurface,          animationSpec, label = "inverseSurface").value,
    inverseOnSurface        = animateColorAsState(inverseOnSurface,        animationSpec, label = "inverseOnSurface").value,
    error                   = animateColorAsState(error,                   animationSpec, label = "error").value,
    onError                 = animateColorAsState(onError,                 animationSpec, label = "onError").value,
    errorContainer          = animateColorAsState(errorContainer,          animationSpec, label = "errorContainer").value,
    onErrorContainer        = animateColorAsState(onErrorContainer,        animationSpec, label = "onErrorContainer").value,
    outline                 = animateColorAsState(outline,                 animationSpec, label = "outline").value,
    outlineVariant          = animateColorAsState(outlineVariant,          animationSpec, label = "outlineVariant").value,
    scrim                   = animateColorAsState(scrim,                   animationSpec, label = "scrim").value,
    surfaceBright           = animateColorAsState(surfaceBright,           animationSpec, label = "surfaceBright").value,
    surfaceDim              = animateColorAsState(surfaceDim,              animationSpec, label = "surfaceDim").value,
    surfaceContainer        = animateColorAsState(surfaceContainer,        animationSpec, label = "surfaceContainer").value,
    surfaceContainerHigh    = animateColorAsState(surfaceContainerHigh,    animationSpec, label = "surfaceContainerHigh").value,
    surfaceContainerHighest = animateColorAsState(surfaceContainerHighest, animationSpec, label = "surfaceContainerHighest").value,
    surfaceContainerLow     = animateColorAsState(surfaceContainerLow,     animationSpec, label = "surfaceContainerLow").value,
    surfaceContainerLowest  = animateColorAsState(surfaceContainerLowest,  animationSpec, label = "surfaceContainerLowest").value,
)

@Composable
fun Phase10Theme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme: Boolean = themePreference.isDark()
    val isAmoled: Boolean = themePreference == ThemePreference.AMOLED

    val targetColorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val base = if (darkTheme) dynamicDarkColorScheme(context)
                       else           dynamicLightColorScheme(context)
            if (isAmoled) base.toAmoled() else base
        }
        isAmoled  -> AmoledColorScheme
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Animate every color slot for a smooth crossfade between schemes.
    val colorScheme = targetColorScheme.animated()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Phase10Typography,
        shapes = Phase10Shapes,
        content = content
    )
}
