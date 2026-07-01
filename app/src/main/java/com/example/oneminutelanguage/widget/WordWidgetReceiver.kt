package com.example.oneminutelanguage.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordWidgetReceiver : AppWidgetProvider() {

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
        // Widget was resized (e.g. dropped to 1-row height) — rebuild immediately
        // so compact text sizing applies without waiting for the next screen-on.
        renderEach(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ScreenOnForegroundService.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ScreenOnForegroundService.stop(context)
    }

    private fun renderEach(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            for (appWidgetId in appWidgetIds) {
                val views = WidgetRenderer.buildRemoteViews(context, appWidgetManager, appWidgetId)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}