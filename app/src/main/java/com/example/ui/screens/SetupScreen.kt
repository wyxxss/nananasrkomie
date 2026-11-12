package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.model.DemoCatalog
import com.example.ui.ToneFormUiState

@Composable
fun SetupScreen(
    uiState: ToneFormUiState,
    onOpenNotificationSettings: () -> Unit,
    onRefreshStatus: () -> Unit,
    onSelectDemoTrack: (Int) -> Unit,
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
            item {
                Column {
                    Text(
                        text = "Подключение Pixel",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.onBackground
                    )
                    Text(
                        text = "Синхронизация с музыкальными плеерами и добавление виджетов на экран",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // Step 1: Notification Listener Access
            item {
                val granted = uiState.notificationAccessGranted
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = if (granted) colors.primaryContainer else colors.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = if (granted) colors.onPrimaryContainer else colors.onTertiaryContainer,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (granted) "Доступ к медиа активен" else "1. Доступ к медиа-сессиям",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (granted) colors.onPrimaryContainer else colors.onTertiaryContainer
                            )
                        }

                        Text(
                            text = if (granted) {
                                "ToneForm автоматически считывает треки, обложки и статус воспроизведения из Spotify, YouTube Music, Apple Music и других плееров."
                            } else {
                                "Разрешите ToneForm чтение медиа-уведомлений в настройках Pixel, чтобы виджеты мгновенно управляли любым плеером и подбирали цвета из обложек."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (granted) colors.onPrimaryContainer else colors.onTertiaryContainer
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onOpenNotificationSettings,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("setup_notification_access_button")
                            ) {
                                Text(if (granted) "Настройки доступа" else "Разрешить доступ")
                            }
                            FilledTonalButton(
                                onClick = onRefreshStatus,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("setup_refresh_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Проверить")
                            }
                        }
                    }
                }
            }

            // Active Media Sessions
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Активные плееры",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface
                            )
                        }

                        if (uiState.activeSessions.isEmpty()) {
                            Text(
                                text = "Внешние медиа-сессии пока не запущены. Откройте любой музыкальный плеер или запустите встроенный синт ToneForm ниже.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        } else {
                            uiState.activeSessions.forEach { session ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = colors.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.GraphicEq,
                                            contentDescription = null,
                                            tint = if (session.isPlaying) colors.primary else colors.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = session.appName,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = colors.onSurface
                                            )
                                            Text(
                                                text = session.trackTitle,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = if (session.isPlaying) colors.primaryContainer else colors.surfaceVariant
                                        ) {
                                            Text(
                                                text = if (session.isPlaying) "Играет" else "Пауза",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (session.isPlaying) colors.onPrimaryContainer else colors.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Built-in Playable Demo Synth Deck
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Встроенный ToneForm Synth",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface
                        )
                        Text(
                            text = "Запустите любой трек, чтобы сразу проверить работу виджетов, анимаций и адаптации цветов под обложку:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        DemoCatalog.tracks.forEachIndexed { idx, track ->
                            val active = !uiState.nowPlaying.hasExternalSession &&
                                uiState.nowPlaying.demoTrackIndex == idx &&
                                uiState.nowPlaying.isPlaying
                            Surface(
                                onClick = { onSelectDemoTrack(idx) },
                                shape = RoundedCornerShape(18.dp),
                                color = if (active) colors.secondaryContainer else colors.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("setup_demo_track_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = track.artRes),
                                        contentDescription = track.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.onSurface
                                        )
                                        Text(
                                            text = "${track.artist} • ${track.tempoBpm} BPM",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.PlayCircle,
                                        contentDescription = "Запустить",
                                        tint = if (active) colors.primary else colors.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pixel Launcher Tips
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Как разместить на Pixel Launcher",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Нажмите «Добавить на экран» на карточке любого виджета во вкладке «Виджеты», либо удерживайте пустое место на рабочем столе Pixel → Виджеты → ToneForm.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
