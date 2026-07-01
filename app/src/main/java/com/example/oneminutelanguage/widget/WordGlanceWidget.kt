package com.example.oneminutelanguage.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.oneminutelanguage.MainActivity
import com.example.oneminutelanguage.data.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WordGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // All data work happens here, BEFORE composition, exactly once per update.
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

        val isCompact = isCompactWidget(context, id)

        provideContent {
            WidgetContent(data = data, isCompact = isCompact)
        }
    }

    /** True when the widget is currently sized to roughly a 1-row home screen slot. */
    private fun isCompactWidget(context: Context, glanceId: GlanceId): Boolean {
        return try {
            val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
            val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, Int.MAX_VALUE)
            minHeight in 1..70
        } catch (e: Exception) {
            false
        }
    }

    @Composable
    private fun WidgetContent(data: WordWidgetData, isCompact: Boolean) {
        val context = LocalContext.current
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f))
                .padding(6.dp),
            // Only the "+" button (below) is small enough for this alignment to matter —
            // the text Column fills the box regardless, so it stays centered on its own.
            contentAlignment = Alignment.TopEnd
        ) {
            // Word content fills the whole widget and centers vertically, so a
            // 1-row-height widget doesn't waste space on a dedicated button bar.
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Word being learned: primary, largest text.
                Text(
                    text = data.language2Word,
                    maxLines = if (isCompact) 1 else 2,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledFontSize(data.language2Word, isPrimary = true, compact = isCompact),
                        textAlign = TextAlign.Center
                    )
                )

                // English reference: secondary, smaller text.
                Text(
                    text = data.language1Word,
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = scaledFontSize(data.language1Word, isPrimary = false, compact = isCompact),
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier.padding(top = if (isCompact) 0.dp else 1.dp)
                )
            }

            // Add-word button floats over the top-right corner instead of
            // reserving its own row.
            Text(
                text = "+",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                modifier = GlanceModifier
                    .padding(start = 0.dp, top = 0.dp, end = 6.dp, bottom = 0.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                putExtra("navigate_to_add_word", true)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                        )
                    )
            )
        }
    }
}

/**
 * Large by default, shrinking in steps as the word gets longer so it never overflows
 * the widget. `isPrimary` sets the base tier — the learned-language word reads larger
 * than the English reference below it. `compact` drops the base tier further for a
 * 1-row-height widget, which has much less vertical room to work with.
 */
private fun scaledFontSize(text: String, isPrimary: Boolean, compact: Boolean): TextUnit {
    // Base tiers are 10% smaller than the original 20/34/12/22 set.
    val base = when {
        isPrimary && compact -> 18
        isPrimary -> 31
        compact -> 11
        else -> 20
    }

    val lengthPenalty = when {
        text.length <= 8 -> 0
        text.length <= 14 -> 5
        text.length <= 20 -> 11
        else -> 16
    }

    val minSize = when {
        isPrimary && compact -> 10
        isPrimary -> 14
        compact -> 8
        else -> 11
    }
    return (base - lengthPenalty).coerceAtLeast(minSize).sp
}