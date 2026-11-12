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
import com.example.model.ExpressiveShapeStyle
import com.example.model.NowPlayingState
import com.example.model.WidgetCustomizationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VinylBloomWidget : GlanceAppWidget() {

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
                    outputSizePx = 190,
                    vinylSpindleCutout = true
                )
            }

            AdaptiveGlanceTheme(customization = customization, albumBitmap = rawArt) {
                VinylBloomContent(
                    nowPlaying = nowPlaying,
                    customization = customization,
                    shapedArt = shapedArt
                )
            }
        }
    }
}

@Composable
private fun VinylBloomContent(
    nowPlaying: NowPlayingState,
    customization: WidgetCustomizationState,
    shapedArt: Bitmap?
) {
    val size = LocalSize.current
    val isWide = size.width >= 220.dp

    val containerBg = when (customization.contrastMode) {
        com.example.model.SurfaceContrastMode.TONAL_SOFT -> GlanceTheme.colors.surfaceVariant
        com.example.model.SurfaceContrastMode.EXPRESSIVE_VIBRANT -> GlanceTheme.colors.primaryContainer
        com.example.model.SurfaceContrastMode.HIGH_CONTRAST -> GlanceTheme.colors.surface
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(28.dp)
            .background(containerBg)
            .padding(12.dp)
    ) {
        if (isWide) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sculpted Vinyl Flower Disc
                Box(
                    modifier = GlanceModifier
                        .size(104.dp)
                        .clickable(actionRunCallback<OpenMusicAppAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(
                            if (customization.shapeStyle == ExpressiveShapeStyle.FOUR_SIDED_COOKIE)
                                R.drawable.shape_four_sided_cookie
                            else
                                R.drawable.shape_scallop_flower
                        ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.tertiaryContainer),
                        modifier = GlanceModifier.fillMaxSize()
                    )
                    if (shapedArt != null) {
                        Image(
                            provider = ImageProvider(shapedArt),
                            contentDescription = nowPlaying.title,
                            contentScale = ContentScale.Crop,
                            modifier = GlanceModifier.size(86.dp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customization.showSourceBadge) {
                        Box(
                            modifier = GlanceModifier
                                .cornerRadius(100.dp)
                                .background(GlanceTheme.colors.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = nowPlaying.sourceAppName,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSecondaryContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }

                    Text(
                        text = nowPlaying.title,
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
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
                            backgroundColor = GlanceTheme.colors.surfaceVariant
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start
                    ) {
                        TonalCircleIconButton(
                            iconRes = R.drawable.ic_skip_prev_expressive,
                            contentDescription = "Предыдущий трек",
                            bgColor = GlanceTheme.colors.surface,
                            tintColor = GlanceTheme.colors.onSurface,
                            onClick = actionRunCallback<SkipPreviousAction>()
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        ExpressivePlaySquircleButton(
                            isPlaying = nowPlaying.isPlaying,
                            modifier = GlanceModifier.size(width = 58.dp, height = 48.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        TonalCircleIconButton(
                            iconRes = R.drawable.ic_skip_next_expressive,
                            contentDescription = "Следующий трек",
                            bgColor = GlanceTheme.colors.surface,
                            tintColor = GlanceTheme.colors.onSurface,
                            onClick = actionRunCallback<SkipNextAction>()
                        )
                    }
                }
            }
        } else {
            // Compact 2x2 Expressive Vinyl Card
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(76.dp)
                        .clickable(actionRunCallback<OpenMusicAppAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.shape_scallop_flower),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.tertiaryContainer),
                        modifier = GlanceModifier.fillMaxSize()
                    )
                    if (shapedArt != null) {
                        Image(
                            provider = ImageProvider(shapedArt),
                            contentDescription = nowPlaying.title,
                            contentScale = ContentScale.Crop,
                            modifier = GlanceModifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                Text(
                    text = nowPlaying.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                        modifier = GlanceModifier.size(52.dp)
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
}

@Composable
internal fun ExpressivePlaySquircleButton(
    isPlaying: Boolean,
    modifier: GlanceModifier = GlanceModifier.size(52.dp)
) {
    val corner = if (isPlaying) 16.dp else 26.dp
    Box(
        modifier = modifier
            .cornerRadius(corner)
            .background(GlanceTheme.colors.tertiary)
            .clickable(actionRunCallback<TogglePlayPauseAction>()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(
                if (isPlaying) R.drawable.ic_pause_expressive else R.drawable.ic_play_expressive
            ),
            contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiary),
            modifier = GlanceModifier.size(26.dp)
        )
    }
}

@Composable
internal fun TonalCircleIconButton(
    iconRes: Int,
    contentDescription: String,
    bgColor: androidx.glance.unit.ColorProvider,
    tintColor: androidx.glance.unit.ColorProvider,
    buttonSize: androidx.compose.ui.unit.Dp = 48.dp,
    onClick: androidx.glance.action.Action
) {
    Box(
        modifier = GlanceModifier
            .size(buttonSize)
            .cornerRadius(100.dp)
            .background(bgColor)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tintColor),
            modifier = GlanceModifier.size(22.dp)
        )
    }
}

class VinylBloomWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VinylBloomWidget()
}
