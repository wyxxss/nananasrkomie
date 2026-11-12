package com.example.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.ColorAdaptationMode
import com.example.model.StudioPreset

val ExpressiveM3Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun ToneFormTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    adaptationMode: ColorAdaptationMode = ColorAdaptationMode.WALLPAPER_DYNAMIC,
    studioPreset: StudioPreset = StudioPreset.PIXEL_CORAL,
    albumBitmap: Bitmap? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = resolveWidgetPreviewScheme(
        context = context,
        mode = adaptationMode,
        preset = studioPreset,
        albumBitmap = albumBitmap,
        darkTheme = darkTheme
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExpressiveTypography,
        shapes = ExpressiveM3Shapes,
        content = content
    )
}
