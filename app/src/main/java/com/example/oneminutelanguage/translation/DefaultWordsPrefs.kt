package com.example.oneminutelanguage.translation

import android.content.Context

object DefaultWordsPrefs {
    private const val PREFS_NAME = "default_words_prefs"
    private const val KEY_ENABLED = "default_words_enabled"

    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }
}
