package com.example.oneminutelanguage.course

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object CoursePrefs {
    private const val PREFS_NAME = "course_prefs"
    private const val KEY_COURSE = "selected_course"
    private const val KEY_DAILY_GOAL = "daily_goal"
    private const val KEY_TTS_RATE = "tts_rate"
    private const val KEY_LT_ENABLED = "language_tool_enabled"
    private const val KEY_FLAGS = "flags"
    private const val KEY_CAN_DO = "can_do"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun selectedCourse(context: Context): String? = prefs(context).getString(KEY_COURSE, null)
    fun setSelectedCourse(context: Context, id: String) = prefs(context).edit().putString(KEY_COURSE, id).apply()

    /** Emits the current course id, then every change (Learn and Today react when the selector saves). */
    fun selectedCourseFlow(context: Context): Flow<String?> = callbackFlow {
        val prefs = prefs(context)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_COURSE) trySend(prefs.getString(KEY_COURSE, null))
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(KEY_COURSE, null))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun dailyGoal(context: Context): Int = prefs(context).getInt(KEY_DAILY_GOAL, 20)
    fun setDailyGoal(context: Context, n: Int) = prefs(context).edit().putInt(KEY_DAILY_GOAL, n).apply()

    /** Used from Stage 4. */
    fun ttsRate(context: Context): Float = prefs(context).getFloat(KEY_TTS_RATE, 1.0f)
    fun setTtsRate(context: Context, rate: Float) = prefs(context).edit().putFloat(KEY_TTS_RATE, rate).apply()

    /** Opt-in: the writing check sends the text to languagetool.org. */
    fun languageToolEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_LT_ENABLED, false)
    fun setLanguageToolEnabled(context: Context, on: Boolean) = prefs(context).edit().putBoolean(KEY_LT_ENABLED, on).apply()

    /** Problems the learner reported: "<lessonId>|<itemIndex>|<type>|<text>". */
    fun flags(context: Context): Set<String> = prefs(context).getStringSet(KEY_FLAGS, emptySet()).orEmpty()
    fun addFlag(context: Context, line: String) =
        prefs(context).edit().putStringSet(KEY_FLAGS, flags(context) + line).apply() // a new set: the stored one must not be changed
    fun clearFlags(context: Context) = prefs(context).edit().remove(KEY_FLAGS).apply()

    /** The placement test is offered once per course. */
    fun placementOffered(context: Context, courseId: String): Boolean = prefs(context).getBoolean("placement_offered_$courseId", false)
    fun setPlacementOffered(context: Context, courseId: String) = prefs(context).edit().putBoolean("placement_offered_$courseId", true).apply()

    /** Ids of the B1 "I can…" statements the learner ticked. */
    fun canDo(context: Context): Set<String> = prefs(context).getStringSet(KEY_CAN_DO, emptySet()).orEmpty()
    fun setCanDo(context: Context, id: String, on: Boolean) =
        prefs(context).edit().putStringSet(KEY_CAN_DO, if (on) canDo(context) + id else canDo(context) - id).apply()

    /** The unfinished text of a writing task, so leaving the lesson doesn't lose it. */
    fun writingDraft(context: Context, lessonId: String): String = prefs(context).getString("writing_draft_$lessonId", "").orEmpty()
    fun setWritingDraft(context: Context, lessonId: String, text: String) =
        prefs(context).edit().putString("writing_draft_$lessonId", text).apply()
}
