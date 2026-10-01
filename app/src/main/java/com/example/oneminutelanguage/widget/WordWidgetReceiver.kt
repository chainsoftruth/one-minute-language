package com.example.oneminutelanguage.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordWidgetReceiver : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // The service doesn't survive a reboot or an app update, so restart it if a widget is placed.
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, WordWidgetReceiver::class.java)
            )
            if (appWidgetIds.isNotEmpty()) {
                startService(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        renderEach(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        renderEach(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startService(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ScreenOnForegroundService.stop(context)
    }

    private fun startService(context: Context) {
        // Android 12+ can refuse a foreground service start from the background.
        // The next app open or reboot starts it anyway.
        try {
            ScreenOnForegroundService.start(context)
        } catch (e: Exception) {
            Log.w("WordWidgetReceiver", "Could not start screen-on service", e)
        }
    }

    private fun renderEach(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    try {
                        val views = WidgetRenderer.buildRemoteViews(context, appWidgetManager, appWidgetId, advance = false)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    } catch (e: Exception) {
                        Log.e("WordWidgetReceiver", "Failed to render widget $appWidgetId", e)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
