package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ColorAdaptationMode
import com.example.model.ExpressiveShapeStyle
import com.example.model.StudioPreset
import com.example.model.SurfaceContrastMode
import com.example.model.WidgetCustomizationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.toneFormDataStore: DataStore<Preferences> by preferencesDataStore(name = "toneform_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val COLOR_MODE = stringPreferencesKey("color_mode")
        val SHAPE_STYLE = stringPreferencesKey("shape_style")
        val CONTRAST_MODE = stringPreferencesKey("contrast_mode")
        val STUDIO_PRESET = stringPreferencesKey("studio_preset")
        val SHOW_PROGRESS = booleanPreferencesKey("show_progress")
        val SHOW_SOURCE_BADGE = booleanPreferencesKey("show_source_badge")
        val SHOW_WAVEFORM = booleanPreferencesKey("show_waveform")
        val REVISION = intPreferencesKey("revision")
    }

    val customizationFlow: Flow<WidgetCustomizationState> = context.toneFormDataStore.data.map { prefs ->
        parsePrefs(prefs)
    }

    suspend fun getCurrentCustomization(): WidgetCustomizationState {
        return parsePrefs(context.toneFormDataStore.data.first())
    }

    private fun parsePrefs(prefs: Preferences): WidgetCustomizationState {
        val colorMode = prefs[Keys.COLOR_MODE]?.let { name ->
            ColorAdaptationMode.entries.firstOrNull { it.name == name }
        } ?: ColorAdaptationMode.WALLPAPER_DYNAMIC

        val shapeStyle = prefs[Keys.SHAPE_STYLE]?.let { name ->
            ExpressiveShapeStyle.entries.firstOrNull { it.name == name }
        } ?: ExpressiveShapeStyle.SCALLOPED_FLOWER

        val contrastMode = prefs[Keys.CONTRAST_MODE]?.let { name ->
            SurfaceContrastMode.entries.firstOrNull { it.name == name }
        } ?: SurfaceContrastMode.EXPRESSIVE_VIBRANT

        val preset = prefs[Keys.STUDIO_PRESET]?.let { id ->
            StudioPreset.entries.firstOrNull { it.id == id }
        } ?: StudioPreset.PIXEL_CORAL

        return WidgetCustomizationState(
            colorMode = colorMode,
            shapeStyle = shapeStyle,
            contrastMode = contrastMode,
            selectedPreset = preset,
            showProgress = prefs[Keys.SHOW_PROGRESS] ?: true,
            showSourceBadge = prefs[Keys.SHOW_SOURCE_BADGE] ?: true,
            showWaveform = prefs[Keys.SHOW_WAVEFORM] ?: true,
            revision = prefs[Keys.REVISION] ?: 0
        )
    }

    suspend fun setColorMode(mode: ColorAdaptationMode) {
        update { prefs -> prefs[Keys.COLOR_MODE] = mode.name }
    }

    suspend fun cycleColorMode() {
        update { prefs ->
            val current = prefs[Keys.COLOR_MODE]?.let { n ->
                ColorAdaptationMode.entries.firstOrNull { it.name == n }
            } ?: ColorAdaptationMode.WALLPAPER_DYNAMIC
            val next = ColorAdaptationMode.entries[(current.ordinal + 1) % ColorAdaptationMode.entries.size]
            prefs[Keys.COLOR_MODE] = next.name
        }
    }

    suspend fun setShapeStyle(shape: ExpressiveShapeStyle) {
        update { prefs -> prefs[Keys.SHAPE_STYLE] = shape.name }
    }

    suspend fun setContrastMode(contrast: SurfaceContrastMode) {
        update { prefs -> prefs[Keys.CONTRAST_MODE] = contrast.name }
    }

    suspend fun setStudioPreset(preset: StudioPreset) {
        update { prefs ->
            prefs[Keys.STUDIO_PRESET] = preset.id
            prefs[Keys.COLOR_MODE] = ColorAdaptationMode.CURATED_PRESET.name
        }
    }

    suspend fun setShowProgress(enabled: Boolean) {
        update { prefs -> prefs[Keys.SHOW_PROGRESS] = enabled }
    }

    suspend fun setShowSourceBadge(enabled: Boolean) {
        update { prefs -> prefs[Keys.SHOW_SOURCE_BADGE] = enabled }
    }

    suspend fun setShowWaveform(enabled: Boolean) {
        update { prefs -> prefs[Keys.SHOW_WAVEFORM] = enabled }
    }

    private suspend fun update(transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.toneFormDataStore.edit { prefs ->
            transform(prefs)
            prefs[Keys.REVISION] = (prefs[Keys.REVISION] ?: 0) + 1
        }
    }
}
