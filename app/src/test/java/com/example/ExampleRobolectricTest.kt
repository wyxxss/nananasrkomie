package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.model.ExpressiveShapeStyle
import com.example.model.StudioPreset
import com.example.ui.theme.buildAlbumArtColorScheme
import com.example.ui.theme.buildPresetColorScheme
import com.example.ui.theme.FallbackDarkScheme
import com.example.widgets.WidgetBitmapHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches ToneForm`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ToneForm", appName)
    }

    @Test
    fun `widget bitmap helper downsamples and clips to expressive shapes`() {
        val largeBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val downsampled = WidgetBitmapHelper.downsampleForWidget(largeBitmap, maxDimensionPx = 220)
        assertNotNull(downsampled)
        assertTrue(downsampled!!.width <= 220 && downsampled.height <= 220)

        ExpressiveShapeStyle.entries.forEach { shape ->
            val clipped = WidgetBitmapHelper.clipToExpressiveShape(
                source = downsampled,
                shapeStyle = shape,
                outputSizePx = 160,
                vinylSpindleCutout = shape == ExpressiveShapeStyle.SCALLOPED_FLOWER
            )
            assertNotNull(clipped)
            assertEquals(160, clipped!!.width)
            assertEquals(160, clipped.height)
        }
    }

    @Test
    fun `preset and album palette builders produce valid tonal schemes`() {
        val presetScheme = buildPresetColorScheme(StudioPreset.PIXEL_CORAL, darkTheme = true)
        assertNotNull(presetScheme.primary)
        assertNotNull(presetScheme.tertiaryContainer)

        val testArt = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            eraseColor(0xFFFF6B4A.toInt())
        }
        val artScheme = buildAlbumArtColorScheme(testArt, darkTheme = false, fallback = FallbackDarkScheme)
        assertNotNull(artScheme.primary)
    }
}
