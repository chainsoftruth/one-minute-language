package com.example.oneminutelanguage.widget

import android.content.Context

object WidgetPrefs {
    private const val PREFS_NAME = "widget_prefs"
    private const val KEY_LAST_WORD_ID = "last_word_id"
    private const val KEY_SHOWING_CHILD_A = "showing_child_a"
    private const val KEY_FOCUS_MODE = "focus_mode"
    private const val NO_LAST_WORD = -1L

    fun getLastWordId(context: Context, appWidgetId: Int): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong("${KEY_LAST_WORD_ID}_$appWidgetId", NO_LAST_WORD)
    }

    fun setLastWordId(context: Context, appWidgetId: Int, id: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong("${KEY_LAST_WORD_ID}_$appWidgetId", id)
            .apply()
    }

    fun isFocusMode(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FOCUS_MODE, true)
    }

    fun setFocusMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FOCUS_MODE, enabled)
            .apply()
    }

    fun isChildAVisible(context: Context, appWidgetId: Int): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("${KEY_SHOWING_CHILD_A}_$appWidgetId", true)
    }

    fun setChildAVisible(context: Context, appWidgetId: Int, visible: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("${KEY_SHOWING_CHILD_A}_$appWidgetId", visible)
            .apply()
    }
}
