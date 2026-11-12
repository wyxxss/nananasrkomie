package com.example.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import com.example.model.DemoCatalog
import com.example.model.NowPlayingState
import com.example.widgets.WidgetBitmapHelper
import com.example.widgets.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveSessionInfo(
    val packageName: String,
    val appName: String,
    val trackTitle: String,
    val isPlaying: Boolean
)

class MediaRepository private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _nowPlaying = MutableStateFlow(NowPlayingState())
    val nowPlaying: StateFlow<NowPlayingState> = _nowPlaying.asStateFlow()

    private val _albumArtBitmap = MutableStateFlow<Bitmap?>(null)
    val albumArtBitmap: StateFlow<Bitmap?> = _albumArtBitmap.asStateFlow()

    private val _notificationAccessGranted = MutableStateFlow(false)
    val notificationAccessGranted: StateFlow<Boolean> = _notificationAccessGranted.asStateFlow()

    private val _activeSessions = MutableStateFlow<List<ActiveSessionInfo>>(emptyList())
    val activeSessions: StateFlow<List<ActiveSessionInfo>> = _activeSessions.asStateFlow()

    private var externalController: MediaController? = null
    private var lastWidgetPushTimeMs = 0L

    private val builtInPlayer = BuiltInMusicPlayer(appContext) { track, playing, posMs, rawBitmap ->
        if (externalController == null) {
            val safeArt = WidgetBitmapHelper.downsampleForWidget(rawBitmap, maxDimensionPx = 220)
            _albumArtBitmap.value = safeArt
            _nowPlaying.value = NowPlayingState(
                title = track.title,
                artist = track.artist,
                album = track.album,
                isPlaying = playing,
                positionMs = posMs,
                durationMs = track.durationMs,
                sourcePackage = appContext.packageName,
                sourceAppName = "ToneForm Synth",
                demoTrackIndex = track.id,
                hasExternalSession = false,
                artRevision = _nowPlaying.value.artRevision + 1
            )
            // Throttle widget updates during 1s playback ticks to every ~5s unless state changed
            val now = System.currentTimeMillis()
            if (now - lastWidgetPushTimeMs > 4500L) {
                lastWidgetPushTimeMs = now
                pushWidgetUpdate()
            }
        }
    }

    init {
        refreshNotificationAccess()
        // Seed initial album art from first demo track
        val initialTrack = DemoCatalog.tracks[0]
        _albumArtBitmap.value = WidgetBitmapHelper.downsampleForWidget(
            builtInPlayer.getArtBitmap(initialTrack),
            maxDimensionPx = 220
        )
    }

    fun refreshNotificationAccess(): Boolean {
        val enabled = isNotificationServiceEnabled(appContext)
        _notificationAccessGranted.value = enabled
        if (enabled) {
            syncActiveSessionsFromManager()
        }
        return enabled
    }

    fun syncActiveSessionsFromManager() {
        try {
            val msm = appContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
                ?: return
            val component = ComponentName(appContext, MusicMediaListenerService::class.java)
            val controllers = msm.getActiveSessions(component)
            updateFromControllers(controllers)
        } catch (_: SecurityException) {
            _notificationAccessGranted.value = false
        } catch (_: Throwable) {
        }
    }

    fun updateFromControllers(controllers: List<MediaController>?) {
        val list = controllers.orEmpty()
        val externalOnly = list.filter { it.packageName != appContext.packageName }

        val sessionInfos = list.map { ctrl ->
            val meta = ctrl.metadata
            val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Без названия"
            val playing = ctrl.playbackState?.state == PlaybackState.STATE_PLAYING
            ActiveSessionInfo(
                packageName = ctrl.packageName,
                appName = resolveAppLabel(ctrl.packageName),
                trackTitle = title,
                isPlaying = playing
            )
        }
        _activeSessions.value = sessionInfos

        // Prefer actively playing external controller, else most recent external controller with metadata
        val bestExternal = externalOnly.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: externalOnly.firstOrNull {
            it.metadata != null
        }

        if (bestExternal != null) {
            externalController = bestExternal
            applyExternalControllerState(bestExternal)
        } else {
            externalController = null
            val track = builtInPlayer.getCurrentTrack()
            _albumArtBitmap.value = WidgetBitmapHelper.downsampleForWidget(
                builtInPlayer.getArtBitmap(track),
                maxDimensionPx = 220
            )
            _nowPlaying.value = NowPlayingState(
                title = track.title,
                artist = track.artist,
                album = track.album,
                isPlaying = builtInPlayer.isPlaying(),
                positionMs = builtInPlayer.getPositionMs(),
                durationMs = track.durationMs,
                sourcePackage = appContext.packageName,
                sourceAppName = "ToneForm Synth",
                demoTrackIndex = builtInPlayer.getCurrentIndex(),
                hasExternalSession = false,
                artRevision = _nowPlaying.value.artRevision + 1
            )
            pushWidgetUpdate()
        }
    }

    fun applyExternalControllerState(controller: MediaController) {
        if (controller.packageName == appContext.packageName) return
        externalController = controller
        val metadata = controller.metadata
        val pbState = controller.playbackState

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Музыка играет"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: resolveAppLabel(controller.packageName)
        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = pbState?.position ?: 0L
        val isPlaying = pbState?.state == PlaybackState.STATE_PLAYING

        val rawArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        val safeArt = if (rawArt != null) {
            WidgetBitmapHelper.downsampleForWidget(rawArt, maxDimensionPx = 220)
        } else {
            WidgetBitmapHelper.downsampleForWidget(
                builtInPlayer.getArtBitmap(builtInPlayer.getCurrentTrack()),
                maxDimensionPx = 220
            )
        }
        _albumArtBitmap.value = safeArt

        _nowPlaying.value = NowPlayingState(
            title = title,
            artist = artist,
            album = album,
            isPlaying = isPlaying,
            positionMs = position.coerceAtLeast(0L),
            durationMs = duration.coerceAtLeast(1L),
            sourcePackage = controller.packageName,
            sourceAppName = resolveAppLabel(controller.packageName),
            demoTrackIndex = builtInPlayer.getCurrentIndex(),
            hasExternalSession = true,
            artRevision = _nowPlaying.value.artRevision + 1
        )
        pushWidgetUpdate()
    }

    fun togglePlayPause() {
        val ext = externalController
        if (ext != null) {
            val isPlaying = ext.playbackState?.state == PlaybackState.STATE_PLAYING
            if (isPlaying) ext.transportControls.pause() else ext.transportControls.play()
        } else {
            builtInPlayer.togglePlayPause()
            pushWidgetUpdateNow()
        }
    }

    fun skipNext() {
        val ext = externalController
        if (ext != null) {
            ext.transportControls.skipToNext()
        } else {
            builtInPlayer.skipNext()
            pushWidgetUpdateNow()
        }
    }

    fun skipPrevious() {
        val ext = externalController
        if (ext != null) {
            ext.transportControls.skipToPrevious()
        } else {
            builtInPlayer.skipPrevious()
            pushWidgetUpdateNow()
        }
    }

    fun seekTo(positionMs: Long) {
        val ext = externalController
        if (ext != null) {
            ext.transportControls.seekTo(positionMs)
        } else {
            builtInPlayer.seekTo(positionMs)
            pushWidgetUpdateNow()
        }
    }

    fun selectDemoTrack(index: Int, autoPlay: Boolean = true) {
        externalController = null
        builtInPlayer.selectTrack(index, autoPlay)
        pushWidgetUpdateNow()
    }

    fun openActivePlayerIntent(context: Context): Intent? {
        val pkg = _nowPlaying.value.sourcePackage
        if (pkg.isNotBlank() && pkg != appContext.packageName) {
            val launch = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) {
                return launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return context.packageManager.getLaunchIntentForPackage(appContext.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun pushWidgetUpdateNow() {
        lastWidgetPushTimeMs = System.currentTimeMillis()
        pushWidgetUpdate()
    }

    private fun pushWidgetUpdate() {
        scope.launch {
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }

    private fun resolveAppLabel(packageName: String): String {
        if (packageName == appContext.packageName) return "ToneForm Synth"
        return try {
            val pm = appContext.packageManager
            val info = pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            pm.getApplicationLabel(info).toString()
        } catch (_: Throwable) {
            try {
                @Suppress("DEPRECATION")
                val info = appContext.packageManager.getApplicationInfo(packageName, 0)
                appContext.packageManager.getApplicationLabel(info).toString()
            } catch (_: Throwable) {
                packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MediaRepository? = null

        fun getInstance(context: Context): MediaRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MediaRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun isNotificationServiceEnabled(context: Context): Boolean {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val componentName = ComponentName(context, MusicMediaListenerService::class.java)
            return flat.split(":").any {
                ComponentName.unflattenFromString(it) == componentName
            }
        }
    }
}
