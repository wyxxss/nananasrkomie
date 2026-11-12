package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpressiveShapeStyle
import com.example.model.NowPlayingState
import com.example.model.SurfaceContrastMode
import com.example.model.WidgetCustomizationState
import com.example.model.WidgetKind
import com.example.ui.theme.ExpressivePolygons
import com.example.ui.theme.RoundedPolygonShape

@Composable
fun PixelWallpaperStage(
    wallpaperPreset: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val stageGradient = remember(wallpaperPreset, colors) {
        when (wallpaperPreset) {
            1 -> Brush.linearGradient(
                listOf(Color(0xFF3E1F2A), Color(0xFF6B2D38), Color(0xFF281722))
            )
            2 -> Brush.linearGradient(
                listOf(Color(0xFF102A24), Color(0xFF1D4D42), Color(0xFF0E1D24))
            )
            3 -> Brush.linearGradient(
                listOf(Color(0xFF171A38), Color(0xFF2D2B63), Color(0xFF141226))
            )
            else -> Brush.linearGradient(
                listOf(
                    colors.surfaceContainerHighest,
                    colors.primaryContainer.copy(alpha = 0.45f),
                    colors.tertiaryContainer.copy(alpha = 0.45f)
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(stageGradient)
            .padding(horizontal = 14.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Pixel wallpaper ambient light orbs
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = colors.primary.copy(alpha = 0.14f),
                radius = size.minDimension * 0.45f,
                center = Offset(size.width * 0.15f, size.height * 0.25f)
            )
            drawCircle(
                color = colors.tertiary.copy(alpha = 0.14f),
                radius = size.minDimension * 0.40f,
                center = Offset(size.width * 0.85f, size.height * 0.78f)
            )
        }
        content()
    }
}

@Composable
fun InteractiveWidgetPreview(
    kind: WidgetKind,
    nowPlaying: NowPlayingState,
    albumBitmap: Bitmap?,
    customization: WidgetCustomizationState,
    compact: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onCycleColorMode: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    when (kind) {
        WidgetKind.VINYL_BLOOM -> VinylBloomPreview(
            nowPlaying = nowPlaying,
            albumBitmap = albumBitmap,
            customization = customization,
            compact = compact,
            onTogglePlayPause = onTogglePlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onOpenPlayer = onOpenPlayer
        )

        WidgetKind.PILL_DECK -> PillDeckPreview(
            nowPlaying = nowPlaying,
            albumBitmap = albumBitmap,
            customization = customization,
            onTogglePlayPause = onTogglePlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onOpenPlayer = onOpenPlayer
        )

        WidgetKind.CLOVER_CONTROL -> CloverControlPreview(
            nowPlaying = nowPlaying,
            albumBitmap = albumBitmap,
            compact = compact,
            onTogglePlayPause = onTogglePlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onCycleColorMode = onCycleColorMode,
            onOpenPlayer = onOpenPlayer
        )

        WidgetKind.WAVE_BAR -> WaveBarPreview(
            nowPlaying = nowPlaying,
            albumBitmap = albumBitmap,
            customization = customization,
            onTogglePlayPause = onTogglePlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onOpenPlayer = onOpenPlayer
        )
    }
}

@Composable
private fun AnimatedVinylRotation(isPlaying: Boolean): Float {
    val infinite = rememberInfiniteTransition(label = "vinyl_spin")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )
    return if (isPlaying) angle else 0f
}

@Composable
fun AnimatedWaveformView(
    isPlaying: Boolean,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "wave")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    Canvas(modifier = modifier) {
        val barCount = 8
        val totalW = size.width
        val slotW = totalW / barCount
        val barW = (slotW * 0.56f).coerceAtLeast(2.dp.toPx())
        for (i in 0 until barCount) {
            val hRatio = if (isPlaying) {
                0.30f + 0.68f * kotlin.math.abs(kotlin.math.sin(phase + i * 0.75f))
            } else {
                listOf(0.35f, 0.65f, 0.45f, 0.85f, 0.55f, 0.78f, 0.40f, 0.60f)[i]
            }
            val barH = size.height * hRatio
            val x = i * slotW + (slotW - barW) / 2f
            val y = (size.height - barH) / 2f
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}

@Composable
private fun ShapedAlbumArt(
    bitmap: Bitmap?,
    shapeStyle: ExpressiveShapeStyle,
    modifier: Modifier = Modifier,
    vinylCenterHole: Boolean = false
) {
    val shape = remember(shapeStyle) {
        RoundedPolygonShape(ExpressivePolygons.polygonFor(shapeStyle))
    }
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Обложка трека",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        if (vinylCenterHole) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(colors.tertiaryContainer)
            )
        }
    }
}

@Composable
private fun ExpressivePlaySquircle(
    isPlaying: Boolean,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corner by animateDpAsState(
        targetValue = if (isPlaying) 16.dp else 26.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "play_morph"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(corner),
        color = bgColor,
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun TonalCircleControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    bgColor: Color,
    tintColor: Color,
    onClick: () -> Unit,
    sizeDp: Int = 44
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier
            .size(sizeDp.dp)
            .minimumInteractiveComponentSize()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun VinylBloomPreview(
    nowPlaying: NowPlayingState,
    albumBitmap: Bitmap?,
    customization: WidgetCustomizationState,
    compact: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val spin = AnimatedVinylRotation(nowPlaying.isPlaying)
    val containerBg = when (customization.contrastMode) {
        SurfaceContrastMode.TONAL_SOFT -> colors.surfaceVariant
        SurfaceContrastMode.EXPRESSIVE_VIBRANT -> colors.primaryContainer
        SurfaceContrastMode.HIGH_CONTRAST -> colors.surface
    }
    val backShape = remember { RoundedPolygonShape(ExpressivePolygons.scallopedFlower()) }

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = containerBg,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = if (compact) Modifier.width(184.dp) else Modifier.fillMaxWidth()
    ) {
        if (compact) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clickable(onClick = onOpenPlayer),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(spin * 0.4f)
                            .clip(backShape)
                            .background(colors.tertiaryContainer)
                    )
                    ShapedAlbumArt(
                        bitmap = albumBitmap,
                        shapeStyle = customization.shapeStyle,
                        vinylCenterHole = true,
                        modifier = Modifier
                            .size(72.dp)
                            .rotate(spin)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = nowPlaying.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TonalCircleControl(
                        icon = Icons.Rounded.SkipPrevious,
                        contentDescription = "Предыдущий",
                        bgColor = colors.surface,
                        tintColor = colors.onSurface,
                        onClick = onSkipPrevious,
                        sizeDp = 40
                    )
                    ExpressivePlaySquircle(
                        isPlaying = nowPlaying.isPlaying,
                        bgColor = colors.tertiary,
                        iconColor = colors.onTertiary,
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("preview_play_vinyl_compact")
                    )
                    TonalCircleControl(
                        icon = Icons.Rounded.SkipNext,
                        contentDescription = "Следующий",
                        bgColor = colors.surface,
                        tintColor = colors.onSurface,
                        onClick = onSkipNext,
                        sizeDp = 40
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clickable(onClick = onOpenPlayer),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(spin * 0.4f)
                            .clip(backShape)
                            .background(colors.tertiaryContainer)
                    )
                    ShapedAlbumArt(
                        bitmap = albumBitmap,
                        shapeStyle = customization.shapeStyle,
                        vinylCenterHole = true,
                        modifier = Modifier
                            .size(84.dp)
                            .rotate(spin)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (customization.showSourceBadge) {
                        Surface(
                            shape = CircleShape,
                            color = colors.secondaryContainer
                        ) {
                            Text(
                                text = nowPlaying.sourceAppName,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = nowPlaying.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = nowPlaying.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (customization.showProgress) {
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(colors.surface.copy(alpha = 0.55f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(nowPlaying.progressFraction.coerceAtLeast(0.08f))
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(colors.primary)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TonalCircleControl(
                            icon = Icons.Rounded.SkipPrevious,
                            contentDescription = "Предыдущий",
                            bgColor = colors.surface,
                            tintColor = colors.onSurface,
                            onClick = onSkipPrevious
                        )
                        ExpressivePlaySquircle(
                            isPlaying = nowPlaying.isPlaying,
                            bgColor = colors.tertiary,
                            iconColor = colors.onTertiary,
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(width = 60.dp, height = 46.dp)
                                .testTag("preview_play_vinyl")
                        )
                        TonalCircleControl(
                            icon = Icons.Rounded.SkipNext,
                            contentDescription = "Следующий",
                            bgColor = colors.surface,
                            tintColor = colors.onSurface,
                            onClick = onSkipNext
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillDeckPreview(
    nowPlaying: NowPlayingState,
    albumBitmap: Bitmap?,
    customization: WidgetCustomizationState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val rootBg = when (customization.contrastMode) {
        SurfaceContrastMode.TONAL_SOFT -> colors.surface
        SurfaceContrastMode.EXPRESSIVE_VIBRANT -> colors.surfaceVariant
        SurfaceContrastMode.HIGH_CONTRAST -> colors.surface
    }

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = rootBg,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onOpenPlayer,
                shape = RoundedCornerShape(24.dp),
                color = colors.primaryContainer,
                modifier = Modifier.size(114.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(7.dp)) {
                    ShapedAlbumArt(
                        bitmap = albumBitmap,
                        shapeStyle = customization.shapeStyle,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = nowPlaying.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (customization.showWaveform) {
                                Spacer(Modifier.width(6.dp))
                                AnimatedWaveformView(
                                    isPlaying = nowPlaying.isPlaying,
                                    barColor = colors.primary,
                                    modifier = Modifier.size(width = 28.dp, height = 14.dp)
                                )
                            }
                        }
                        Text(
                            text = nowPlaying.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (customization.showProgress) {
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(nowPlaying.progressFraction.coerceAtLeast(0.08f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TonalCircleControl(
                        icon = Icons.Rounded.SkipPrevious,
                        contentDescription = "Предыдущий",
                        bgColor = colors.primaryContainer,
                        tintColor = colors.onPrimaryContainer,
                        onClick = onSkipPrevious
                    )
                    ExpressivePlaySquircle(
                        isPlaying = nowPlaying.isPlaying,
                        bgColor = colors.primary,
                        iconColor = colors.onPrimary,
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("preview_play_pill")
                    )
                    TonalCircleControl(
                        icon = Icons.Rounded.SkipNext,
                        contentDescription = "Следующий",
                        bgColor = colors.primaryContainer,
                        tintColor = colors.onPrimaryContainer,
                        onClick = onSkipNext
                    )
                }
            }
        }
    }
}

@Composable
private fun CloverControlPreview(
    nowPlaying: NowPlayingState,
    albumBitmap: Bitmap?,
    compact: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onCycleColorMode: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val cookieShape = remember { RoundedPolygonShape(ExpressivePolygons.fourSidedCookie()) }
    val sizeDp = if (compact) 168.dp else 208.dp

    Box(
        modifier = Modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = cookieShape,
            color = colors.primaryContainer,
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        TonalCircleControl(
                            icon = Icons.Rounded.SkipPrevious,
                            contentDescription = "Предыдущий",
                            bgColor = Color.Transparent,
                            tintColor = colors.onPrimaryContainer,
                            onClick = onSkipPrevious
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        TonalCircleControl(
                            icon = Icons.Rounded.SkipNext,
                            contentDescription = "Следующий",
                            bgColor = Color.Transparent,
                            tintColor = colors.onPrimaryContainer,
                            onClick = onSkipNext
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        TonalCircleControl(
                            icon = Icons.Rounded.Palette,
                            contentDescription = "Сменить стиль цвета",
                            bgColor = Color.Transparent,
                            tintColor = colors.onPrimaryContainer,
                            onClick = onCycleColorMode
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        TonalCircleControl(
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = "Открыть плеер",
                            bgColor = Color.Transparent,
                            tintColor = colors.onPrimaryContainer,
                            onClick = onOpenPlayer
                        )
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!compact) {
                Surface(
                    shape = CircleShape,
                    color = colors.secondaryContainer
                ) {
                    Text(
                        text = nowPlaying.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = colors.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            Box(contentAlignment = Alignment.Center) {
                ExpressivePlaySquircle(
                    isPlaying = nowPlaying.isPlaying,
                    bgColor = colors.tertiary,
                    iconColor = colors.onTertiary,
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("preview_play_clover")
                )
                if (albumBitmap != null && !nowPlaying.isPlaying && !compact) {
                    Image(
                        bitmap = albumBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onTogglePlayPause)
                    )
                    Surface(
                        onClick = onTogglePlayPause,
                        shape = CircleShape,
                        color = colors.tertiary.copy(alpha = 0.90f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Воспроизвести",
                                tint = colors.onTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaveBarPreview(
    nowPlaying: NowPlayingState,
    albumBitmap: Bitmap?,
    customization: WidgetCustomizationState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = colors.secondaryContainer,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShapedAlbumArt(
                bitmap = albumBitmap,
                shapeStyle = customization.shapeStyle,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onOpenPlayer)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nowPlaying.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (customization.showWaveform) {
                        AnimatedWaveformView(
                            isPlaying = nowPlaying.isPlaying,
                            barColor = colors.primary,
                            modifier = Modifier.size(width = 24.dp, height = 11.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = nowPlaying.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TonalCircleControl(
                    icon = Icons.Rounded.SkipPrevious,
                    contentDescription = "Предыдущий",
                    bgColor = colors.surface,
                    tintColor = colors.onSurface,
                    onClick = onSkipPrevious,
                    sizeDp = 42
                )
                ExpressivePlaySquircle(
                    isPlaying = nowPlaying.isPlaying,
                    bgColor = colors.tertiary,
                    iconColor = colors.onTertiary,
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("preview_play_wave")
                )
                TonalCircleControl(
                    icon = Icons.Rounded.SkipNext,
                    contentDescription = "Следующий",
                    bgColor = colors.surface,
                    tintColor = colors.onSurface,
                    onClick = onSkipNext,
                    sizeDp = 42
                )
            }
        }
    }
}
