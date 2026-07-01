package com.example.oneminutelanguage.widget

import android.content.Context

/**
 * Tracks the word currently shown on the widget so the next random pick can
 * exclude it — otherwise a repeat looks like the widget failed to update.
 */
object WidgetPrefs {
    private const val PREFS_NAME = "widget_prefs"
    private const val KEY_LAST_WORD_ID = "last_word_id"
    private const val NO_LAST_WORD = -1L

    fun getLastWordId(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_WORD_ID, NO_LAST_WORD)
    }

    fun setLastWordId(context: Context, id: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_WORD_ID, id)
            .apply()
    }
}
