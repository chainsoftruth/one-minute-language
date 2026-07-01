package com.example.oneminutelanguage.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WordWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WordGlanceWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ScreenOnForegroundService.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ScreenOnForegroundService.stop(context)
    }
}