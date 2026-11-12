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
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.R
import com.example.data.PreferencesRepository
import com.example.media.MediaRepository
import com.example.model.ExpressiveShapeStyle
import com.example.model.NowPlayingState
import com.example.model.WidgetCustomizationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloverControlWidget : GlanceAppWidget() {

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

            val shapedArt = remember(nowPlaying.artRevision) {
                WidgetBitmapHelper.clipToExpressiveShape(
                    source = rawArt,
                    shapeStyle = ExpressiveShapeStyle.SQUIRCLE,
                    outputSizePx = 120,
                    vinylSpindleCutout = false
                )
            }

            AdaptiveGlanceTheme(customization = customization, albumBitmap = rawArt) {
                CloverControlContent(
                    nowPlaying = nowPlaying,
                    shapedArt = shapedArt
                )
            }
        }
    }
}

@Composable
private fun CloverControlContent(
    nowPlaying: NowPlayingState,
    shapedArt: Bitmap?
) {
    val size = LocalSize.current
    val boxSize = size.width.coerceAtMost(size.height)

    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Sculpted 4-Sided Cookie Material 3 Expressive Background
        Box(
            modifier = GlanceModifier.size(boxSize),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.shape_four_sided_cookie),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer),
                modifier = GlanceModifier.fillMaxSize()
            )

            // 4 Expressive Corner Controls (2x2 Grid)
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        CornerActionButton(
                            iconRes = R.drawable.ic_skip_prev_expressive,
                            contentDescription = "Предыдущий трек",
                            onClick = actionRunCallback<SkipPreviousAction>()
                        )
                    }
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        CornerActionButton(
                            iconRes = R.drawable.ic_skip_next_expressive,
                            contentDescription = "Следующий трек",
                            onClick = actionRunCallback<SkipNextAction>()
                        )
                    }
                }

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        CornerActionButton(
                            iconRes = R.drawable.ic_palette_expressive,
                            contentDescription = "Переключить стиль цвета",
                            onClick = actionRunCallback<CycleColorModeAction>()
                        )
                    }
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        CornerActionButton(
                            iconRes = R.drawable.ic_open_player,
                            contentDescription = "Открыть плеер",
                            onClick = actionRunCallback<OpenMusicAppAction>()
                        )
                    }
                }
            }

            // Hero Center Expressive Squircle Play/Pause + Mini Art Ring
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (boxSize >= 185.dp && shapedArt != null) {
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(100.dp)
                            .background(GlanceTheme.colors.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = nowPlaying.title,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSecondaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }

                Box(
                    modifier = GlanceModifier
                        .size(64.dp)
                        .cornerRadius(if (nowPlaying.isPlaying) 18.dp else 32.dp)
                        .background(GlanceTheme.colors.tertiary)
                        .clickable(actionRunCallback<TogglePlayPauseAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    if (shapedArt != null && !nowPlaying.isPlaying && boxSize >= 170.dp) {
                        Image(
                            provider = ImageProvider(shapedArt),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = GlanceModifier
                                .size(56.dp)
                                .cornerRadius(28.dp)
                        )
                        Box(
                            modifier = GlanceModifier
                                .size(34.dp)
                                .cornerRadius(17.dp)
                                .background(GlanceTheme.colors.tertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_play_expressive),
                                contentDescription = "Воспроизвести",
                                colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiary),
                                modifier = GlanceModifier.size(22.dp)
                            )
                        }
                    } else {
                        Image(
                            provider = ImageProvider(
                                if (nowPlaying.isPlaying) R.drawable.ic_pause_expressive
                                else R.drawable.ic_play_expressive
                            ),
                            contentDescription = if (nowPlaying.isPlaying) "Пауза" else "Воспроизвести",
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiary),
                            modifier = GlanceModifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CornerActionButton(
    iconRes: Int,
    contentDescription: String,
    onClick: androidx.glance.action.Action
) {
    Box(
        modifier = GlanceModifier
            .size(48.dp)
            .cornerRadius(100.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
            modifier = GlanceModifier.size(22.dp)
        )
    }
}

class CloverControlWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CloverControlWidget()
}
