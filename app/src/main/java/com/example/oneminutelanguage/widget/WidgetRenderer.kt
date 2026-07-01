package com.example.oneminutelanguage.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.oneminutelanguage.MainActivity
import com.example.oneminutelanguage.R
import com.example.oneminutelanguage.data.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Builds the widget's RemoteViews: fetches the next word, sizes the text, flips the
 * ViewFlipper to the freshly-populated hidden child (crossfade), and wires the "+"
 * button. Replaces the old Glance-based WordGlanceWidget with the same visual result.
 */
object WidgetRenderer {

    suspend fun buildRemoteViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ): RemoteViews {
        val data = withContext(Dispatchers.IO) {
            val database = DatabaseProvider.getDatabase(context)
            val wordDao = database.wordDao()
            val statsDao = database.dailyStatsDao()

            val today = java.time.LocalDate.now().toString()
            statsDao.incrementViewCount(today)

            // Exclude whatever word is currently on screen so a repeat doesn't
            // look like the widget failed to refresh. Falls back to any random
            // word if there's only one word in the database.
            val lastWordId = WidgetPrefs.getLastWordId(context)
            val randomWord = wordDao.getRandomWordExcluding(lastWordId) ?: wordDao.getRandomWord()

            if (randomWord != null) {
                WidgetPrefs.setLastWordId(context, randomWord.id)
                WordWidgetData(
                    language1Word = randomWord.language1Word,
                    language2Word = randomWord.language2Word
                )
            } else {
                WordWidgetData(
                    language1Word = "Tap + to add",
                    language2Word = "No words yet"
                )
            }
        }

        val isCompact = isCompactWidget(appWidgetManager, appWidgetId)
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Write the new word into whichever ViewFlipper child is currently hidden,
        // then flip to it so the ViewFlipper's fade animations play.
        val showingChildA = WidgetPrefs.isChildAVisible(context)
        val nextChildIndex = if (showingChildA) 1 else 0
        val primaryId = if (nextChildIndex == 0) R.id.primary_text_a else R.id.primary_text_b
        val secondaryId = if (nextChildIndex == 0) R.id.secondary_text_a else R.id.secondary_text_b

        views.setTextViewText(primaryId, data.language2Word)
        views.setTextViewText(secondaryId, data.language1Word)

        views.setTextViewTextSize(
            primaryId,
            TypedValue.COMPLEX_UNIT_SP,
            scaledFontSize(data.language2Word, isPrimary = true, compact = isCompact)
        )
        views.setTextViewTextSize(
            secondaryId,
            TypedValue.COMPLEX_UNIT_SP,
            scaledFontSize(data.language1Word, isPrimary = false, compact = isCompact)
        )

        // Text is single-line with ellipsize="marquee" in the layout; a TextView only
        // actually scrolls its marquee while "selected", which RemoteViews has no
        // dedicated action for, so it's set via the generic reflection setter.
        views.setBoolean(primaryId, "setSelected", true)
        views.setBoolean(secondaryId, "setSelected", true)

        val secondaryTopPaddingPx = dpToPx(context, if (isCompact) 0 else 1)
        views.setViewPadding(secondaryId, 0, secondaryTopPaddingPx, 0, 0)

        views.setDisplayedChild(R.id.word_flipper, nextChildIndex)
        WidgetPrefs.setChildAVisible(context, nextChildIndex == 0)

        views.setOnClickPendingIntent(
            R.id.btn_add_word,
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    putExtra("navigate_to_add_word", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        return views
    }

    /** True when the widget is currently sized to roughly a 1-row home screen slot. */
    fun isCompactWidget(appWidgetManager: AppWidgetManager, appWidgetId: Int): Boolean {
        return try {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, Int.MAX_VALUE)
            minHeight in 1..70
        } catch (e: Exception) {
            false
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}

/**
 * Large by default, shrinking in steps as the word gets longer so it never overflows
 * the widget. `isPrimary` sets the base tier — the learned-language word reads larger
 * than the English reference below it. `compact` drops the base tier further for a
 * 1-row-height widget, which has much less vertical room to work with.
 */
private fun scaledFontSize(text: String, isPrimary: Boolean, compact: Boolean): Float {
    // Another 10% down from the previous 18/31/11/20 set.
    val base = when {
        isPrimary && compact -> 16
        isPrimary -> 28
        compact -> 10
        else -> 18
    }

    val lengthPenalty = when {
        text.length <= 8 -> 0
        text.length <= 14 -> 4
        text.length <= 20 -> 10
        else -> 14
    }

    val minSize = when {
        isPrimary && compact -> 9
        isPrimary -> 12
        compact -> 7
        else -> 10
    }
    return (base - lengthPenalty).coerceAtLeast(minSize).toFloat()
}
