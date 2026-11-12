package com.example.ui.theme

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.example.model.ColorAdaptationMode
import com.example.model.StudioPreset

val FallbackLightScheme = lightColorScheme(
    primary = Color(0xFF8C3B2B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD2),
    onPrimaryContainer = Color(0xFF3A0902),
    secondary = Color(0xFF6B538C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDDCFF),
    onSecondaryContainer = Color(0xFF250E44),
    tertiary = Color(0xFF006A60),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF9AF2E5),
    onTertiaryContainer = Color(0xFF00201C),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF231917),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231917),
    surfaceVariant = Color(0xFFF5DDD8),
    onSurfaceVariant = Color(0xFF534340)
)

val FallbackDarkScheme = darkColorScheme(
    primary = Color(0xFFFFB4A3),
    onPrimary = Color(0xFF561E13),
    primaryContainer = Color(0xFF733427),
    onPrimaryContainer = Color(0xFFFFDAD2),
    secondary = Color(0xFFD8BAFF),
    onSecondary = Color(0xFF3B255B),
    secondaryContainer = Color(0xFF533B73),
    onSecondaryContainer = Color(0xFFEDDCFF),
    tertiary = Color(0xFF7ED6C9),
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF005048),
    onTertiaryContainer = Color(0xFF9AF2E5),
    background = Color(0xFF1A110F),
    onBackground = Color(0xFFF1DFDC),
    surface = Color(0xFF1A110F),
    onSurface = Color(0xFFF1DFDC),
    surfaceVariant = Color(0xFF534340),
    onSurfaceVariant = Color(0xFFD8C2BD)
)

data class WallpaperSeedSwatches(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val hasLiveWallpaperColors: Boolean
)

fun readSystemWallpaperSwatches(context: Context, fallbackScheme: ColorScheme): WallpaperSeedSwatches {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        try {
            val wm = WallpaperManager.getInstance(context)
            val wpColors = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            if (wpColors != null) {
                val p = Color(wpColors.primaryColor.toArgb())
                val s = wpColors.secondaryColor?.let { Color(it.toArgb()) } ?: fallbackScheme.secondary
                val t = wpColors.tertiaryColor?.let { Color(it.toArgb()) } ?: fallbackScheme.tertiary
                return WallpaperSeedSwatches(
                    primary = p,
                    secondary = s,
                    tertiary = t,
                    hasLiveWallpaperColors = true
                )
            }
        } catch (_: Throwable) {
            // Permission or headless device fallback
        }
    }
    return WallpaperSeedSwatches(
        primary = fallbackScheme.primary,
        secondary = fallbackScheme.secondary,
        tertiary = fallbackScheme.tertiary,
        hasLiveWallpaperColors = false
    )
}

fun buildPresetColorScheme(preset: StudioPreset, darkTheme: Boolean): ColorScheme {
    val primarySeed = Color(preset.primaryHex)
    val secondarySeed = Color(preset.secondaryHex)
    val tertiarySeed = Color(preset.tertiaryHex)
    return buildTonalScheme(primarySeed, secondarySeed, tertiarySeed, darkTheme)
}

fun buildAlbumArtColorScheme(bitmap: Bitmap?, darkTheme: Boolean, fallback: ColorScheme): ColorScheme {
    if (bitmap == null) return fallback
    val palette = Palette.from(bitmap).maximumColorCount(16).generate()
    val primaryInt = palette.getVibrantColor(
        palette.getLightVibrantColor(
            palette.getDominantColor(fallback.primary.toArgb())
        )
    )
    val secondaryInt = palette.getMutedColor(
        palette.getLightMutedColor(fallback.secondary.toArgb())
    )
    val tertiaryInt = palette.getDarkVibrantColor(
        palette.getLightVibrantColor(fallback.tertiary.toArgb())
    )
    return buildTonalScheme(
        primarySeed = Color(primaryInt),
        secondarySeed = Color(secondaryInt),
        tertiarySeed = Color(tertiaryInt),
        darkTheme = darkTheme
    )
}

fun buildTonalScheme(
    primarySeed: Color,
    secondarySeed: Color,
    tertiarySeed: Color,
    darkTheme: Boolean
): ColorScheme {
    val pArgb = primarySeed.toArgb()
    val sArgb = secondarySeed.toArgb()
    val tArgb = tertiarySeed.toArgb()

    fun tone(argb: Int, lightness: Float, satScale: Float = 1.0f): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(argb, hsl)
        hsl[1] = (hsl[1] * satScale).coerceIn(0.18f, 0.92f)
        hsl[2] = lightness.coerceIn(0.05f, 0.96f)
        return Color(ColorUtils.HSLToColor(hsl))
    }

    fun contrastOn(bg: Color): Color =
        if (bg.luminance() > 0.42f) Color(0xFF141118) else Color(0xFFFFFBFE)

    return if (darkTheme) {
        val primary = tone(pArgb, 0.78f, 0.95f)
        val primaryContainer = tone(pArgb, 0.28f, 0.85f)
        val secondary = tone(sArgb, 0.76f, 0.72f)
        val secondaryContainer = tone(sArgb, 0.26f, 0.65f)
        val tertiary = tone(tArgb, 0.78f, 0.88f)
        val tertiaryContainer = tone(tArgb, 0.27f, 0.80f)
        val surface = tone(pArgb, 0.09f, 0.22f)
        val surfaceVariant = tone(pArgb, 0.20f, 0.30f)
        darkColorScheme(
            primary = primary,
            onPrimary = contrastOn(primary),
            primaryContainer = primaryContainer,
            onPrimaryContainer = tone(pArgb, 0.92f, 0.85f),
            secondary = secondary,
            onSecondary = contrastOn(secondary),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = tone(sArgb, 0.90f, 0.65f),
            tertiary = tertiary,
            onTertiary = contrastOn(tertiary),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = tone(tArgb, 0.92f, 0.80f),
            background = surface,
            onBackground = Color(0xFFF3EDF4),
            surface = surface,
            onSurface = Color(0xFFF3EDF4),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = Color(0xFFD0C4D4),
            surfaceContainerHigh = tone(pArgb, 0.15f, 0.28f),
            surfaceContainerHighest = tone(pArgb, 0.21f, 0.32f),
            surfaceContainerLow = tone(pArgb, 0.11f, 0.24f)
        )
    } else {
        val primary = tone(pArgb, 0.42f, 0.95f)
        val primaryContainer = tone(pArgb, 0.89f, 0.85f)
        val secondary = tone(sArgb, 0.44f, 0.68f)
        val secondaryContainer = tone(sArgb, 0.90f, 0.60f)
        val tertiary = tone(tArgb, 0.42f, 0.85f)
        val tertiaryContainer = tone(tArgb, 0.88f, 0.78f)
        val surface = tone(pArgb, 0.97f, 0.25f)
        val surfaceVariant = tone(pArgb, 0.90f, 0.35f)
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = tone(pArgb, 0.16f, 0.90f),
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = tone(sArgb, 0.18f, 0.70f),
            tertiary = tertiary,
            onTertiary = Color.White,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = tone(tArgb, 0.16f, 0.85f),
            background = surface,
            onBackground = Color(0xFF1C1820),
            surface = surface,
            onSurface = Color(0xFF1C1820),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = Color(0xFF4A4250),
            surfaceContainerHigh = tone(pArgb, 0.92f, 0.30f),
            surfaceContainerHighest = tone(pArgb, 0.88f, 0.35f),
            surfaceContainerLow = tone(pArgb, 0.95f, 0.25f)
        )
    }
}

fun resolveDynamicOrFallbackScheme(context: Context, darkTheme: Boolean): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) FallbackDarkScheme else FallbackLightScheme
    }
}

fun resolveWidgetPreviewScheme(
    context: Context,
    mode: ColorAdaptationMode,
    preset: StudioPreset,
    albumBitmap: Bitmap?,
    darkTheme: Boolean
): ColorScheme {
    val systemScheme = resolveDynamicOrFallbackScheme(context, darkTheme)
    return when (mode) {
        ColorAdaptationMode.WALLPAPER_DYNAMIC -> systemScheme
        ColorAdaptationMode.ALBUM_ART_CHAMELEON -> buildAlbumArtColorScheme(albumBitmap, darkTheme, systemScheme)
        ColorAdaptationMode.CURATED_PRESET -> buildPresetColorScheme(preset, darkTheme)
    }
}
