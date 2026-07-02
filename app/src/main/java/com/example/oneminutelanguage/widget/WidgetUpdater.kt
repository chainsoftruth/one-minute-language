package com.example.oneminutelanguage.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log

object WidgetUpdater {
    suspend fun refreshWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WordWidgetReceiver::class.java)
        )

        for (appWidgetId in appWidgetIds) {
            try {
                val views = WidgetRenderer.buildRemoteViews(context, appWidgetManager, appWidgetId)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e("WidgetUpdater", "Failed to refresh widget $appWidgetId", e)
            }
        }
    }
}
