package com.example.oneminutelanguage.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.example.oneminutelanguage.data.DatabaseProvider

object WidgetUpdater {
    /** [advance] = true only on screen-on: shows a new word and counts one view. */
    suspend fun refreshWidget(context: Context, advance: Boolean = false) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WordWidgetReceiver::class.java)
        )

        if (advance && appWidgetIds.isNotEmpty()) {
            try {
                DatabaseProvider.getDatabase(context).dailyStatsDao()
                    .incrementViewCount(java.time.LocalDate.now().toString())
            } catch (e: Exception) {
                Log.e("WidgetUpdater", "Failed to count widget view", e)
            }
        }

        for (appWidgetId in appWidgetIds) {
            try {
                val views = WidgetRenderer.buildRemoteViews(context, appWidgetManager, appWidgetId, advance)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e("WidgetUpdater", "Failed to refresh widget $appWidgetId", e)
            }
        }
    }
}
