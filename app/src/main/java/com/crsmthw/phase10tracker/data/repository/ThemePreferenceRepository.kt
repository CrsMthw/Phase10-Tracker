package com.crsmthw.phase10tracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.crsmthw.phase10tracker.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Top-level delegate — DataStore must be a singleton per file/process.
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_prefs"
)

/**
 * Persists user-facing app preferences (currently just theme mode).
 * Backed by DataStore Preferences — safe to extend with more keys later.
 */
class ThemePreferenceRepository(context: Context) {

    // Hold app context only (NOT activity context) to avoid leaks.
    private val appContext = context.applicationContext

    private val themeKey = stringPreferencesKey("theme_preference")

    val themePreference: Flow<ThemePreference> = appContext.themeDataStore.data.map { prefs ->
        val stored = prefs[themeKey] ?: return@map ThemePreference.SYSTEM
        runCatching { ThemePreference.valueOf(stored) }.getOrDefault(ThemePreference.SYSTEM)
    }

    suspend fun setThemePreference(preference: ThemePreference) {
        appContext.themeDataStore.edit { prefs ->
            prefs[themeKey] = preference.name
        }
    }
}
