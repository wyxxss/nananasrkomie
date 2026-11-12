package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.updateAll
import com.example.MainActivity
import com.example.model.WidgetKind

object WidgetUpdater {

    suspend fun updateAllWidgets(context: Context) {
        runCatching { VinylBloomWidget().updateAll(context) }
        runCatching { PillDeckWidget().updateAll(context) }
        runCatching { CloverControlWidget().updateAll(context) }
        runCatching { WaveBarWidget().updateAll(context) }
    }

    fun receiverClassFor(kind: WidgetKind): Class<*> = when (kind) {
        WidgetKind.VINYL_BLOOM -> VinylBloomWidgetReceiver::class.java
        WidgetKind.PILL_DECK -> PillDeckWidgetReceiver::class.java
        WidgetKind.CLOVER_CONTROL -> CloverControlWidgetReceiver::class.java
        WidgetKind.WAVE_BAR -> WaveBarWidgetReceiver::class.java
    }

    fun isPinSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val awm = AppWidgetManager.getInstance(context)
        return awm.isRequestPinAppWidgetSupported
    }

    fun requestPinWidget(context: Context, kind: WidgetKind): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val awm = AppWidgetManager.getInstance(context)
        if (!awm.isRequestPinAppWidgetSupported) return false

        val provider = ComponentName(context, receiverClassFor(kind))
        val callbackIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("pinned_widget", kind.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val successCallback = PendingIntent.getActivity(
            context,
            kind.ordinal + 100,
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return awm.requestPinAppWidget(provider, null, successCallback)
    }
}
