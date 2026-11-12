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
import androidx.glance.appwidget.LinearProgressIndicator
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
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
import com.example.model.SurfaceContrastMode
import com.example.model.WidgetCustomizationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PillDeckWidget : GlanceAppWidget() {

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
                    outputSizePx = 200,
                    vinylSpindleCutout = false
                )
            }

            AdaptiveGlanceTheme(customization = customization, albumBitmap = rawArt) {
                PillDeckContent(
                    nowPlaying = nowPlaying,
                    customization = customization,
                    shapedArt = shapedArt
                )
            }
        }
    }
}

@Composable
private fun PillDeckContent(
    nowPlaying: NowPlayingState,
    customization: WidgetCustomizationState,
    shapedArt: Bitmap?
) {
    val rootBg = when (customization.contrastMode) {
        SurfaceContrastMode.TONAL_SOFT -> GlanceTheme.colors.surface
        SurfaceContrastMode.EXPRESSIVE_VIBRANT -> GlanceTheme.colors.surfaceVariant
        SurfaceContrastMode.HIGH_CONTRAST -> GlanceTheme.colors.surface
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(32.dp)
            .background(rootBg)
            .padding(10.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Bento Card: Shaped Album Art + Floating Source Pill
            Box(
                modifier = GlanceModifier
                    .size(116.dp)
                    .cornerRadius(26.dp)
                    .background(GlanceTheme.colors.primaryContainer)
                    .clickable(actionRunCallback<OpenMusicAppAction>()),
                contentAlignment = Alignment.Center
            ) {
                if (shapedArt != null) {
                    Image(
                        provider = ImageProvider(shapedArt),
                        contentDescription = nowPlaying.title,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.size(104.dp)
                    )
                } else {
                    Image(
                        provider = ImageProvider(R.drawable.ic_music_note_expressive),
                        contentDescription = nowPlaying.title,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                        modifier = GlanceModifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // Right Asymmetric Stacked Deck
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
            ) {
                // Upper Metadata & Progress Card
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .cornerRadius(22.dp)
                        .background(GlanceTheme.colors.secondaryContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nowPlaying.title,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSecondaryContainer,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            modifier = GlanceModifier.defaultWeight()
                        )
                        if (customization.showWaveform) {
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Image(
                                provider = ImageProvider(R.drawable.ic_waveform_bars),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                                modifier = GlanceModifier.size(width = 28.dp, height = 14.dp)
                            )
                        }
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

                    if (customization.showProgress) {
                        Spacer(modifier = GlanceModifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = nowPlaying.progressFraction,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .cornerRadius(100.dp),
                            color = GlanceTheme.colors.primary,
                            backgroundColor = GlanceTheme.colors.surface
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Lower Expressive Control Dock
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TonalCircleIconButton(
                        iconRes = R.drawable.ic_skip_prev_expressive,
                        contentDescription = "Предыдущий трек",
                        bgColor = GlanceTheme.colors.primaryContainer,
                        tintColor = GlanceTheme.colors.onPrimaryContainer,
                        onClick = actionRunCallback<SkipPreviousAction>()
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .height(48.dp)
                            .cornerRadius(if (nowPlaying.isPlaying) 16.dp else 100.dp)
                            .background(GlanceTheme.colors.primary)
                            .clickable(actionRunCallback<TogglePlayPauseAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(
                                if (nowPlaying.isPlaying) R.drawable.ic_pause_expressive else R.drawable.ic_play_expressive
                            ),
                            contentDescription = if (nowPlaying.isPlaying) "Пауза" else "Воспроизвести",
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimary),
                            modifier = GlanceModifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    TonalCircleIconButton(
                        iconRes = R.drawable.ic_skip_next_expressive,
                        contentDescription = "Следующий трек",
                        bgColor = GlanceTheme.colors.primaryContainer,
                        tintColor = GlanceTheme.colors.onPrimaryContainer,
                        onClick = actionRunCallback<SkipNextAction>()
                    )
                }
            }
        }
    }
}

class PillDeckWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PillDeckWidget()
}
