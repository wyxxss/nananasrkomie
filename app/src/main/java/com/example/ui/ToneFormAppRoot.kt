package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SettingsInputComponent
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsInputComponent
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.PaletteLabScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.ShowcaseScreen
import kotlinx.coroutines.launch

enum class ToneFormTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SHOWCASE("Виджеты", Icons.Rounded.Widgets, Icons.Outlined.Widgets, "nav_showcase"),
    PALETTE("Палитра", Icons.Rounded.Palette, Icons.Outlined.Palette, "nav_palette"),
    SETUP("Подключение", Icons.Rounded.SettingsInputComponent, Icons.Outlined.SettingsInputComponent, "nav_setup")
}

@Composable
fun ToneFormAppRoot(
    uiState: ToneFormUiState,
    viewModel: ToneFormViewModel
) {
    var currentTab by rememberSaveable { mutableStateOf(ToneFormTab.SHOWCASE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val useRail = LocalConfiguration.current.screenWidthDp >= 700

    if (currentTab != ToneFormTab.SHOWCASE) {
        BackHandler { currentTab = ToneFormTab.SHOWCASE }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!useRail) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    tonalElevation = 3.dp
                ) {
                    ToneFormTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (useRail) {
                NavigationRail {
                    ToneFormTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }

            Crossfade(
                targetState = currentTab,
                animationSpec = tween(durationMillis = 220),
                label = "tab_crossfade",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    ToneFormTab.SHOWCASE -> ShowcaseScreen(
                        uiState = uiState,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSkipNext = viewModel::skipNext,
                        onSkipPrevious = viewModel::skipPrevious,
                        onSelectDemoTrack = viewModel::selectDemoTrack,
                        onColorModeSelected = viewModel::setColorMode,
                        onCycleColorMode = viewModel::cycleColorMode,
                        onToggleCompactPreview = viewModel::togglePreviewCompactMode,
                        onWallpaperPresetSelected = viewModel::setPreviewWallpaperPreset,
                        onPinWidget = { kind ->
                            val requested = viewModel.pinWidget(kind)
                            scope.launch {
                                val msg = if (requested) {
                                    "Запрос на добавление «${kind.title}» отправлен на главный экран"
                                } else {
                                    "Удерживайте рабочий стол Pixel → Виджеты → ToneForm"
                                }
                                snackbarHostState.showSnackbar(msg)
                            }
                        },
                        onOpenNotificationSettings = viewModel::openNotificationAccessSettings,
                        onOpenPlayer = viewModel::openActivePlayer,
                        onGoToPaletteLab = { currentTab = ToneFormTab.PALETTE }
                    )

                    ToneFormTab.PALETTE -> PaletteLabScreen(
                        uiState = uiState,
                        onColorModeSelected = viewModel::setColorMode,
                        onShapeSelected = viewModel::setShapeStyle,
                        onContrastSelected = viewModel::setContrastMode,
                        onPresetSelected = viewModel::setStudioPreset,
                        onToggleShowProgress = viewModel::setShowProgress,
                        onToggleShowWaveform = viewModel::setShowWaveform,
                        onToggleShowSourceBadge = viewModel::setShowSourceBadge
                    )

                    ToneFormTab.SETUP -> SetupScreen(
                        uiState = uiState,
                        onOpenNotificationSettings = viewModel::openNotificationAccessSettings,
                        onRefreshStatus = {
                            viewModel.refreshSystemStatus()
                            scope.launch {
                                snackbarHostState.showSnackbar("Статус медиа-сессий обновлён")
                            }
                        },
                        onSelectDemoTrack = viewModel::selectDemoTrack
                    )
                }
            }
        }
    }
}
