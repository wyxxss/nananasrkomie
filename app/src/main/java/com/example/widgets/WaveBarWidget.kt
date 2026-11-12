package com.example.widgets

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.R
import com.example.data.PreferencesRepository
import com.example.media.MediaRepository
import com.example.model.NowPlayingState
import com.example.model.WidgetCustomizationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WaveBarWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val mediaRepo = MediaRepository.getInstance(context)
        val prefsRepo = PreferencesRepository(context)
        val initialCustomization = withContext(Dispatchers.IO) {
            prefsRepo.getCurrentCustomization()
        }

        provideContent {
            val nowPlaying by mediaRepo.nowPlaying.collectAsState()
            val rawArt by mediaRepo.albumArtBitmap.collectAsState()
            val customization by prefsRepo.customizationFlow.collectAsState(initial = initialCustomization)

            val shapedArt = remember(nowPlaying.artRevision, customization.shapeStyle) {
                WidgetBitmapHelper.clipToExpressiveShape(
                    source = rawArt,
                    shapeStyle = customization.shapeStyle,
                    outputSizePx = 140,
                    vinylSpindleCutout = false
                )
            }

            AdaptiveGlanceTheme(customization = customization, albumBitmap = rawArt) {
                WaveBarContent(
                    nowPlaying = nowPlaying,
                    customization = customization,
                    shapedArt = shapedArt
                )
            }
        }
    }
}

@Composable
private fun WaveBarContent(
    nowPlaying: NowPlayingState,
    customization: WidgetCustomizationState,
    shapedArt: Bitmap?
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(100.dp)
            .background(GlanceTheme.colors.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shaped Art Thumbnail
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .cornerRadius(16.dp)
                    .background(GlanceTheme.colors.primaryContainer)
                    .clickable(actionRunCallback<OpenMusicAppAction>()),
                contentAlignment = Alignment.Center
            ) {
                if (shapedArt != null) {
                    Image(
                        provider = ImageProvider(shapedArt),
                        contentDescription = nowPlaying.title,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.size(44.dp)
                    )
                } else {
                    Image(
                        provider = ImageProvider(R.drawable.ic_music_note_expressive),
                        contentDescription = nowPlaying.title,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                        modifier = GlanceModifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // Track Info + Waveform
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nowPlaying.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (customization.showWaveform) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_waveform_bars),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                            modifier = GlanceModifier.size(width = 24.dp, height = 11.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(5.dp))
                    }
                    Text(
                        text = nowPlaying.artist,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(6.dp))

            // Controls Cluster
            Row(verticalAlignment = Alignment.CenterVertically) {
                TonalCircleIconButton(
                    iconRes = R.drawable.ic_skip_prev_expressive,
                    contentDescription = "Предыдущий трек",
                    bgColor = GlanceTheme.colors.surface,
                    tintColor = GlanceTheme.colors.onSurface,
                    buttonSize = 48.dp,
                    onClick = actionRunCallback<SkipPreviousAction>()
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                ExpressivePlaySquircleButton(
                    isPlaying = nowPlaying.isPlaying,
                    modifier = GlanceModifier.size(48.dp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                TonalCircleIconButton(
                    iconRes = R.drawable.ic_skip_next_expressive,
                    contentDescription = "Следующий трек",
                    bgColor = GlanceTheme.colors.surface,
                    tintColor = GlanceTheme.colors.onSurface,
                    buttonSize = 48.dp,
                    onClick = actionRunCallback<SkipNextAction>()
                )
            }
        }
    }
}

class WaveBarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WaveBarWidget()
}
