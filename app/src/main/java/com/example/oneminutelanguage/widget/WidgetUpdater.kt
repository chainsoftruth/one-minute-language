package com.example.oneminutelanguage.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdater {
    suspend fun refreshWidget(context: Context) {
        WordGlanceWidget().updateAll(context)
    }
}