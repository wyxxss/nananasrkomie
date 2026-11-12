package com.example.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MusicMediaListenerService : NotificationListenerService() {

    private var sessionManager: MediaSessionManager? = null
    private val trackedCallbacks = mutableMapOf<MediaController, MediaController.Callback>()

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            attachCallbacks(controllers.orEmpty())
            MediaRepository.getInstance(applicationContext).updateFromControllers(controllers)
        }

    override fun onListenerConnected() {
        super.onListenerConnected()
        val repo = MediaRepository.getInstance(applicationContext)
        repo.refreshNotificationAccess()
        registerMediaSessionListener()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        unregisterAllCallbacks()
        try {
            sessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        } catch (_: Throwable) {
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Keep active session list fresh when media notifications post/update
        MediaRepository.getInstance(applicationContext).syncActiveSessionsFromManager()
    }

    private fun registerMediaSessionListener() {
        try {
            val msm = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
            sessionManager = msm
            val component = ComponentName(this, MusicMediaListenerService::class.java)
            msm.addOnActiveSessionsChangedListener(sessionsChangedListener, component)
            val initial = msm.getActiveSessions(component)
            attachCallbacks(initial)
            MediaRepository.getInstance(applicationContext).updateFromControllers(initial)
        } catch (_: SecurityException) {
        }
    }

    private fun attachCallbacks(controllers: List<MediaController>) {
        unregisterAllCallbacks()
        val repo = MediaRepository.getInstance(applicationContext)
        controllers.filter { it.packageName != packageName }.forEach { controller ->
            val cb = object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    repo.applyExternalControllerState(controller)
                }

                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    repo.applyExternalControllerState(controller)
                }

                override fun onSessionDestroyed() {
                    repo.syncActiveSessionsFromManager()
                }
            }
            controller.registerCallback(cb)
            trackedCallbacks[controller] = cb
        }
    }

    private fun unregisterAllCallbacks() {
        trackedCallbacks.forEach { (controller, callback) ->
            try {
                controller.unregisterCallback(callback)
            } catch (_: Throwable) {
            }
        }
        trackedCallbacks.clear()
    }
}
