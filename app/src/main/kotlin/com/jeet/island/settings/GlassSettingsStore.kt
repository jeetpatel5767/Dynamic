package com.jeet.island.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "island_settings")

object GlassSettingsStore {

    private val KEY_ENABLED = booleanPreferencesKey("glass_enabled")
    private val KEY_OPACITY = floatPreferencesKey("glass_opacity")
    private val KEY_BORDER = floatPreferencesKey("border_alpha")
    private val KEY_HIGHLIGHT = floatPreferencesKey("highlight_strength")
    private val KEY_NOISE = floatPreferencesKey("noise_amount")
    private val KEY_TINT = floatPreferencesKey("tint_hue")
    private val KEY_CORNER = floatPreferencesKey("corner_smoothing")
    private val KEY_BATT_DIALOG_DISMISSED = booleanPreferencesKey("batt_opt_dialog_dismissed")

    fun flow(context: Context): Flow<GlassSettings> = context.dataStore.data.map { prefs ->
        val defaults = GlassSettings()
        GlassSettings(
            glassEnabled = prefs[KEY_ENABLED] ?: defaults.glassEnabled,
            glassOpacity = prefs[KEY_OPACITY] ?: defaults.glassOpacity,
            borderAlpha = prefs[KEY_BORDER] ?: defaults.borderAlpha,
            highlightStrength = prefs[KEY_HIGHLIGHT] ?: defaults.highlightStrength,
            noiseAmount = prefs[KEY_NOISE] ?: defaults.noiseAmount,
            tintHue = prefs[KEY_TINT] ?: defaults.tintHue,
            cornerSmoothing = prefs[KEY_CORNER] ?: defaults.cornerSmoothing
        )
    }

    suspend fun write(context: Context, s: GlassSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ENABLED] = s.glassEnabled
            prefs[KEY_OPACITY] = s.glassOpacity
            prefs[KEY_BORDER] = s.borderAlpha
            prefs[KEY_HIGHLIGHT] = s.highlightStrength
            prefs[KEY_NOISE] = s.noiseAmount
            prefs[KEY_TINT] = s.tintHue
            prefs[KEY_CORNER] = s.cornerSmoothing
        }
    }

    fun battDialogDismissedFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[KEY_BATT_DIALOG_DISMISSED] ?: false }

    suspend fun setBattDialogDismissed(context: Context) {
        context.dataStore.edit { it[KEY_BATT_DIALOG_DISMISSED] = true }
    }
}
