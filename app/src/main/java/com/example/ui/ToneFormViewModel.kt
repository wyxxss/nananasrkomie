package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesRepository
import com.example.media.ActiveSessionInfo
import com.example.media.MediaRepository
import com.example.model.ColorAdaptationMode
import com.example.model.ExpressiveShapeStyle
import com.example.model.NowPlayingState
import com.example.model.StudioPreset
import com.example.model.SurfaceContrastMode
import com.example.model.WidgetCustomizationState
import com.example.model.WidgetKind
import com.example.ui.theme.FallbackDarkScheme
import com.example.ui.theme.WallpaperSeedSwatches
import com.example.ui.theme.readSystemWallpaperSwatches
import com.example.ui.theme.resolveDynamicOrFallbackScheme
import com.example.widgets.WidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ToneFormUiState(
    val nowPlaying: NowPlayingState = NowPlayingState(),
    val albumBitmap: Bitmap? = null,
    val customization: WidgetCustomizationState = WidgetCustomizationState(),
    val notificationAccessGranted: Boolean = false,
    val activeSessions: List<ActiveSessionInfo> = emptyList(),
    val wallpaperSwatches: WallpaperSeedSwatches? = null,
    val pinSupported: Boolean = true,
    val previewCompactMode: Boolean = false,
    val previewWallpaperPreset: Int = 0 // 0 = Live System/Active, 1 = Coral Sunset, 2 = Forest Mint, 3 = Deep Indigo
)

class ToneFormViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val mediaRepo = MediaRepository.getInstance(appContext)
    private val prefsRepo = PreferencesRepository(appContext)

    private val previewCompactMode = MutableStateFlow(false)
    private val previewWallpaperPreset = MutableStateFlow(0)
    private val wallpaperSwatches = MutableStateFlow(
        readSystemWallpaperSwatches(appContext, resolveDynamicOrFallbackScheme(appContext, true))
    )

    val uiState: StateFlow<ToneFormUiState> = combine(
        mediaRepo.nowPlaying,
        mediaRepo.albumArtBitmap,
        prefsRepo.customizationFlow,
        mediaRepo.notificationAccessGranted,
        mediaRepo.activeSessions,
        previewCompactMode,
        previewWallpaperPreset,
        wallpaperSwatches
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        ToneFormUiState(
            nowPlaying = values[0] as NowPlayingState,
            albumBitmap = values[1] as Bitmap?,
            customization = values[2] as WidgetCustomizationState,
            notificationAccessGranted = values[3] as Boolean,
            activeSessions = values[4] as List<ActiveSessionInfo>,
            previewCompactMode = values[5] as Boolean,
            previewWallpaperPreset = values[6] as Int,
            wallpaperSwatches = values[7] as WallpaperSeedSwatches? ?: WallpaperSeedSwatches(
                FallbackDarkScheme.primary,
                FallbackDarkScheme.secondary,
                FallbackDarkScheme.tertiary,
                false
            ),
            pinSupported = WidgetUpdater.isPinSupported(appContext)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ToneFormUiState()
    )

    fun refreshSystemStatus() {
        mediaRepo.refreshNotificationAccess()
        wallpaperSwatches.value = readSystemWallpaperSwatches(
            appContext,
            resolveDynamicOrFallbackScheme(appContext, true)
        )
    }

    fun togglePlayPause() = mediaRepo.togglePlayPause()

    fun skipNext() = mediaRepo.skipNext()

    fun skipPrevious() = mediaRepo.skipPrevious()

    fun selectDemoTrack(index: Int) = mediaRepo.selectDemoTrack(index, autoPlay = true)

    fun setColorMode(mode: ColorAdaptationMode) {
        viewModelScope.launch {
            prefsRepo.setColorMode(mode)
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    fun cycleColorMode() {
        viewModelScope.launch {
            prefsRepo.cycleColorMode()
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    fun setShapeStyle(shape: ExpressiveShapeStyle) {
        viewModelScope.launch {
            prefsRepo.setShapeStyle(shape)
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    fun setContrastMode(contrast: SurfaceContrastMode) {
        viewModelScope.launch {
            prefsRepo.setContrastMode(contrast)
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    fun setStudioPreset(preset: StudioPreset) {
        viewModelScope.launch {
            prefsRepo.setStudioPreset(preset)
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    fun setShowProgress(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setShowProgress(enabled)
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    fun setShowSourceBadge(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setShowSourceBadge(enabled)
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    fun setShowWaveform(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setShowWaveform(enabled)
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    fun togglePreviewCompactMode() {
        previewCompactMode.value = !previewCompactMode.value
    }

    fun setPreviewWallpaperPreset(idx: Int) {
        previewWallpaperPreset.value = idx
    }

    fun pinWidget(kind: WidgetKind): Boolean {
        return WidgetUpdater.requestPinWidget(appContext, kind)
    }

    fun openNotificationAccessSettings() {
        mediaRepo.openNotificationListenerSettings(appContext)
    }

    fun openActivePlayer() {
        val intent = mediaRepo.openActivePlayerIntent(appContext)
        if (intent != null) {
            runCatching { appContext.startActivity(intent) }
        }
    }
}
