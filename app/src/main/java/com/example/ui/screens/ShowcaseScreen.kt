package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ColorAdaptationMode
import com.example.model.DemoCatalog
import com.example.model.WidgetKind
import com.example.ui.ToneFormUiState
import com.example.ui.components.AnimatedWaveformView
import com.example.ui.components.InteractiveWidgetPreview
import com.example.ui.components.PixelWallpaperStage

@Composable
fun ShowcaseScreen(
    uiState: ToneFormUiState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSelectDemoTrack: (Int) -> Unit,
    onColorModeSelected: (ColorAdaptationMode) -> Unit,
    onCycleColorMode: () -> Unit,
    onToggleCompactPreview: () -> Unit,
    onWallpaperPresetSelected: (Int) -> Unit,
    onPinWidget: (WidgetKind) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenPlayer: () -> Unit,
    onGoToPaletteLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expressive Studio Hero Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ToneForm",
                                style = MaterialTheme.typography.displaySmall,
                                color = colors.onBackground
                            )
                            Text(
                                text = "Material You 3 Expressive • Адаптация под обои Pixel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Surface(
                            onClick = onGoToPaletteLab,
                            shape = CircleShape,
                            color = colors.primaryContainer,
                            modifier = Modifier.testTag("header_palette_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = "Открыть палитру",
                                    tint = colors.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = uiState.customization.colorMode.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Notification Access Banner (only emphasized if not yet granted)
                    AnimatedVisibility(visible = !uiState.notificationAccessGranted) {
                        Surface(
                            onClick = onOpenNotificationSettings,
                            shape = RoundedCornerShape(22.dp),
                            color = colors.tertiaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grant_media_access_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = colors.onTertiaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Подключить Spotify / YouTube Music",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Разрешите доступ к медиа-уведомлениям для живой синхронизации",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onTertiaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live Now-Playing Interactive Synth & Sync Deck
            item {
                HeroNowPlayingCard(
                    uiState = uiState,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipNext = onSkipNext,
                    onSkipPrevious = onSkipPrevious,
                    onSelectDemoTrack = onSelectDemoTrack
                )
            }

            // Quick Color Mode & Simulated Pixel Wallpaper Strip
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Адаптация и фон рабочего стола",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorAdaptationMode.entries.forEach { mode ->
                            val selected = uiState.customization.colorMode == mode
                            Surface(
                                onClick = { onColorModeSelected(mode) },
                                shape = CircleShape,
                                color = if (selected) colors.primary else colors.surfaceContainerHigh,
                                modifier = Modifier.testTag("mode_chip_${mode.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = colors.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) colors.onPrimary else colors.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Simulated Pixel Wallpaper backdrop selector for testing widget contrast
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Wallpaper,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Фон превью:",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        val backdrops = listOf(
                            0 to listOf(colors.primaryContainer, colors.tertiaryContainer),
                            1 to listOf(Color(0xFF6B2D38), Color(0xFF281722)),
                            2 to listOf(Color(0xFF1D4D42), Color(0xFF0E1D24)),
                            3 to listOf(Color(0xFF2D2B63), Color(0xFF141226))
                        )
                        backdrops.forEach { (idx, gradColors) ->
                            val active = uiState.previewWallpaperPreset == idx
                            Box(
                                modifier = Modifier
                                    .size(if (active) 28.dp else 24.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(gradColors))
                                    .then(
                                        if (active) Modifier.border(2.dp, colors.primary, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { onWallpaperPresetSelected(idx) }
                            )
                        }
                    }
                }
            }

            // All 4 Expressive Music Widget Showcase Cards
            items(WidgetKind.entries, key = { it.id }) { kind ->
                WidgetShowcaseCard(
                    kind = kind,
                    uiState = uiState,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipNext = onSkipNext,
                    onSkipPrevious = onSkipPrevious,
                    onCycleColorMode = onCycleColorMode,
                    onToggleCompactPreview = onToggleCompactPreview,
                    onPinWidget = { onPinWidget(kind) },
                    onOpenPlayer = onOpenPlayer
                )
            }
        }
    }
}

@Composable
private fun HeroNowPlayingCard(
    uiState: ToneFormUiState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSelectDemoTrack: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val np = uiState.nowPlaying

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = colors.surfaceContainerHigh,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.primaryContainer)
                ) {
                    val bmp = uiState.albumBitmap
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = np.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (np.hasExternalSession) "Синхронизация: ${np.sourceAppName}"
                            else "Встроенный синт • ${np.sourceAppName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = np.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${np.artist} • ${np.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AnimatedWaveformView(
                    isPlaying = np.isPlaying,
                    barColor = colors.tertiary,
                    modifier = Modifier.size(width = 34.dp, height = 22.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Demo Track Switcher
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DemoCatalog.tracks.forEachIndexed { idx, track ->
                        val active = !np.hasExternalSession && np.demoTrackIndex == idx
                        Surface(
                            onClick = { onSelectDemoTrack(idx) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) colors.primaryContainer else colors.surface,
                            modifier = Modifier
                                .size(38.dp)
                                .then(
                                    if (active) Modifier.border(2.dp, colors.primary, RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                                .testTag("demo_track_$idx")
                        ) {
                            Image(
                                painter = painterResource(id = track.artRes),
                                contentDescription = track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Playback Transport Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.testTag("hero_prev_button")
                    ) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Предыдущий")
                    }
                    FilledTonalIconButton(
                        onClick = onTogglePlayPause,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        modifier = Modifier
                            .size(width = 58.dp, height = 44.dp)
                            .testTag("hero_play_button")
                    ) {
                        Icon(
                            imageVector = if (np.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (np.isPlaying) "Пауза" else "Воспроизвести"
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.testTag("hero_next_button")
                    ) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Следующий")
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetShowcaseCard(
    kind: WidgetKind,
    uiState: ToneFormUiState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onCycleColorMode: () -> Unit,
    onToggleCompactPreview: () -> Unit,
    onPinWidget: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val supportsCompactToggle = kind == WidgetKind.VINYL_BLOOM || kind == WidgetKind.CLOVER_CONTROL

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = colors.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = kind.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                    Text(
                        text = kind.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = colors.secondaryContainer
                ) {
                    Text(
                        text = kind.gridLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            PixelWallpaperStage(wallpaperPreset = uiState.previewWallpaperPreset) {
                InteractiveWidgetPreview(
                    kind = kind,
                    nowPlaying = uiState.nowPlaying,
                    albumBitmap = uiState.albumBitmap,
                    customization = uiState.customization,
                    compact = uiState.previewCompactMode,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipNext = onSkipNext,
                    onSkipPrevious = onSkipPrevious,
                    onCycleColorMode = onCycleColorMode,
                    onOpenPlayer = onOpenPlayer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (supportsCompactToggle) {
                    FilledTonalButton(
                        onClick = onToggleCompactPreview,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.testTag("toggle_size_${kind.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AspectRatio,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (uiState.previewCompactMode) "Широкий" else "Компакт")
                    }
                }

                Button(
                    onClick = onPinWidget,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pin_widget_${kind.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddToHomeScreen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Добавить на экран",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
