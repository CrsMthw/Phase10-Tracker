package com.crsmthw.phase10tracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ── Theme Mode ─────────────────────────────────────────────────────────────────
enum class ThemeMode { SYSTEM, LIGHT, DARK }

// One DataStore instance per process — scoped to the application context
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_prefs"
)

// ── Preference Manager ─────────────────────────────────────────────────────────
class ThemePreferenceManager(private val context: Context) {

    companion object {
        private val KEY_THEME_MODE  = intPreferencesKey("theme_mode")
        private val KEY_AMOLED      = booleanPreferencesKey("amoled_black")
        private val KEY_HAPTICS     = booleanPreferencesKey("haptics_enabled")
    }

    /** Emits the saved ThemeMode; defaults to SYSTEM on first run. */
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        when (prefs[KEY_THEME_MODE] ?: 0) {
            1    -> ThemeMode.LIGHT
            2    -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    /** Emits whether AMOLED Pure Black mode is enabled; defaults to false. */
    val amoledBlack: Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_AMOLED] ?: false
    }

    /** Emits whether haptic feedback is enabled; defaults to true. */
    val haptics: Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_HAPTICS] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = when (mode) {
                ThemeMode.LIGHT  -> 1
                ThemeMode.DARK   -> 2
                ThemeMode.SYSTEM -> 0
            }
        }
    }

    suspend fun setAmoledBlack(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_AMOLED] = enabled
        }
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_HAPTICS] = enabled
        }
    }
}
