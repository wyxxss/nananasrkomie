package com.example.widgets

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.data.PreferencesRepository
import com.example.media.MediaRepository

class TogglePlayPauseAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        MediaRepository.getInstance(context).togglePlayPause()
        WidgetUpdater.updateAllWidgets(context)
    }
}

class SkipNextAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        MediaRepository.getInstance(context).skipNext()
        WidgetUpdater.updateAllWidgets(context)
    }
}

class SkipPreviousAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        MediaRepository.getInstance(context).skipPrevious()
        WidgetUpdater.updateAllWidgets(context)
    }
}

class CycleColorModeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        PreferencesRepository(context).cycleColorMode()
        WidgetUpdater.updateAllWidgets(context)
    }
}

class OpenMusicAppAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = MediaRepository.getInstance(context).openActivePlayerIntent(context)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (_: Throwable) {
            }
        }
    }
}
