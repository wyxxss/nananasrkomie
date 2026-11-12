package com.example.widgets

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import com.example.model.ColorAdaptationMode
import com.example.model.WidgetCustomizationState
import com.example.ui.theme.FallbackDarkScheme
import com.example.ui.theme.FallbackLightScheme
import com.example.ui.theme.buildAlbumArtColorScheme
import com.example.ui.theme.buildPresetColorScheme

@Composable
fun AdaptiveGlanceTheme(
    customization: WidgetCustomizationState,
    albumBitmap: Bitmap?,
    content: @Composable () -> Unit
) {
    when (customization.colorMode) {
        ColorAdaptationMode.WALLPAPER_DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceTheme(content = content)
            } else {
                val fallbackProviders = ColorProviders(
                    light = FallbackLightScheme,
                    dark = FallbackDarkScheme
                )
                GlanceTheme(colors = fallbackProviders, content = content)
            }
        }

        ColorAdaptationMode.ALBUM_ART_CHAMELEON -> {
            val light = buildAlbumArtColorScheme(albumBitmap, darkTheme = false, fallback = FallbackLightScheme)
            val dark = buildAlbumArtColorScheme(albumBitmap, darkTheme = true, fallback = FallbackDarkScheme)
            GlanceTheme(colors = ColorProviders(light = light, dark = dark), content = content)
        }

        ColorAdaptationMode.CURATED_PRESET -> {
            val light = buildPresetColorScheme(customization.selectedPreset, darkTheme = false)
            val dark = buildPresetColorScheme(customization.selectedPreset, darkTheme = true)
            GlanceTheme(colors = ColorProviders(light = light, dark = dark), content = content)
        }
    }
}
