package com.example.oneminutelanguage.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.text.TextPaint
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.os.BundleCompat
import com.example.oneminutelanguage.MainActivity
import com.example.oneminutelanguage.R
import com.example.oneminutelanguage.speech.SpeakWordActivity
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.data.WordDao
import com.example.oneminutelanguage.data.WordEntity
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.translation.withoutHints
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.random.Random

object WidgetRenderer {
    private const val ROOT_PADDING_DP = 6f
    private const val SECONDARY_MARGIN_DP = 1f
    private const val ADD_BUTTON_SP = 26
    private const val ADD_BUTTON_PADDING_DP = 6f
    private const val PRIMARY_MAX_SP = 28
    private const val PRIMARY_MIN_SP = 14
    private const val SECONDARY_MAX_SP = 18
    private const val SECONDARY_MIN_SP = 12
    private const val MAX_LINES = 2
    private const val ARTICLE_TAG_SP = 12
    /** The tag's vertical padding (2 x 1dp) and its bottom margin (2dp), as in widget_layout.xml. */
    private const val ARTICLE_TAG_EXTRA_DP = 4f
    private const val DEFAULT_WIDTH_DP = 180f
    private const val DEFAULT_HEIGHT_DP = 110f

    /**
     * [advance] = true picks a new word (screen-on). Otherwise the current word is
     * re-rendered (resize, DB or settings change), or a random one if it is gone or disabled.
     */
    suspend fun buildRemoteViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        advance: Boolean
    ): RemoteViews {
        val data = withContext(Dispatchers.IO) {
            val wordDao = DatabaseProvider.getDatabase(context).wordDao()

            val lastWordId = WidgetPrefs.getLastWordId(context, appWidgetId)
            val word = if (advance) {
                pickWord(context, wordDao, lastWordId)
            } else {
                wordDao.getWordById(lastWordId)?.takeIf { it.isEnabled }
                    ?: pickWord(context, wordDao, lastWordId)
            }

            if (word != null) {
                WidgetPrefs.setLastWordId(context, appWidgetId, word.id)
                WordWidgetData(
                    language1Word = word.language1Word,
                    language2Word = word.language2Word.withoutHints()
                )
            } else {
                WordWidgetData(
                    language1Word = "Tap + to add",
                    language2Word = "No words yet"
                )
            }
        }

        val metrics = context.resources.displayMetrics
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val spToPx = { sp: Int -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), metrics) }
        val measure = { text: String, sp: Int ->
            paint.textSize = spToPx(sp)
            paint.measureText(text)
        }
        val lineHeight = { sp: Int ->
            paint.textSize = spToPx(sp)
            paint.fontMetrics.let { it.bottom - it.top }
        }

        // The + chip width is reserved on both sides so the text stays centred and never runs under it.
        val chipPx = ceil(measure("+", ADD_BUTTON_SP) + dpToPx(context, ADD_BUTTON_PADDING_DP)).toInt()
        val (widthDp, heightDp) = widgetSizeDp(appWidgetManager, appWidgetId)
        val rootPaddingPx = dpToPx(context, ROOT_PADDING_DP)
        val textWidth = dpToPx(context, widthDp) - 2 * rootPaddingPx - 2 * chipPx
        // Dutch nouns: the article becomes a small tag above the word, and the noun alone gets the width.
        val (article, primaryText) =
            if (LanguageSettingsStore.getTargetLanguage(context) == TranslateLanguage.DUTCH) splitArticle(data.language2Word)
            else null to data.language2Word
        val tagHeight = if (article == null) 0f else lineHeight(ARTICLE_TAG_SP) + dpToPx(context, ARTICLE_TAG_EXTRA_DP)
        val textHeight = dpToPx(context, heightDp) - 2 * rootPaddingPx - dpToPx(context, SECONDARY_MARGIN_DP) - tagHeight

        val primarySp = fitSp(
            primaryText, textWidth, textHeight - lineHeight(SECONDARY_MIN_SP),
            PRIMARY_MAX_SP, PRIMARY_MIN_SP, MAX_LINES, lineHeight, measure
        )
        val primaryLines = minOf(lineCount(primaryText, textWidth, primarySp, measure), MAX_LINES)
        val secondarySp = minOf(
            fitSp(
                data.language1Word, textWidth, textHeight - primaryLines * lineHeight(primarySp),
                SECONDARY_MAX_SP, SECONDARY_MIN_SP, MAX_LINES, lineHeight, measure
            ),
            maxOf(SECONDARY_MIN_SP, (primarySp * 0.7f).toInt())
        )

        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        val childA = Triple(R.id.primary_text_a, R.id.secondary_text_a, R.id.article_a)
        val childB = Triple(R.id.primary_text_b, R.id.secondary_text_b, R.id.article_b)
        val showingChildA = WidgetPrefs.isChildAVisible(context, appWidgetId)
        // A new word slides in on the hidden child. A re-render updates both children without flipping.
        val targets = when {
            !advance -> listOf(childA, childB)
            showingChildA -> listOf(childB)
            else -> listOf(childA)
        }

        for ((primaryId, secondaryId, articleId) in targets) {
            views.setTextViewText(primaryId, primaryText)
            views.setViewVisibility(articleId, if (article == null) View.GONE else View.VISIBLE)
            if (article != null) {
                views.setTextViewText(articleId, article)
                views.setInt(articleId, "setBackgroundResource", if (article == "de") R.drawable.widget_tag_de else R.drawable.widget_tag_het)
            }
            views.setTextViewText(secondaryId, data.language1Word)
            views.setTextViewTextSize(primaryId, TypedValue.COMPLEX_UNIT_SP, primarySp.toFloat())
            views.setTextViewTextSize(secondaryId, TypedValue.COMPLEX_UNIT_SP, secondarySp.toFloat())
            views.setInt(primaryId, "setMaxLines", MAX_LINES)
            views.setViewPadding(primaryId, chipPx, 0, chipPx, 0)
            views.setViewPadding(secondaryId, chipPx, 0, chipPx, 0)
        }

        if (advance) {
            views.setDisplayedChild(R.id.word_flipper, if (showingChildA) 1 else 0)
            WidgetPrefs.setChildAVisible(context, appWidgetId, !showingChildA)
        }

        views.setOnClickPendingIntent(
            R.id.word_flipper,
            PendingIntent.getActivity(
                context,
                appWidgetId,
                Intent(context, SpeakWordActivity::class.java).apply {
                    putExtra(SpeakWordActivity.EXTRA_APP_WIDGET_ID, appWidgetId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

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

    /** Portrait size in dp: width = min width, height = max height. */
    private fun widgetSizeDp(appWidgetManager: AppWidgetManager, appWidgetId: Int): Pair<Float, Float> {
        return try {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val size = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                BundleCompat.getParcelableArrayList(
                    options, AppWidgetManager.OPTION_APPWIDGET_SIZES, SizeF::class.java
                )?.firstOrNull()
            } else null
            if (size != null) {
                size.width to size.height
            } else {
                val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
                val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
                (if (width > 0) width.toFloat() else DEFAULT_WIDTH_DP) to
                    (if (height > 0) height.toFloat() else DEFAULT_HEIGHT_DP)
            }
        } catch (e: Exception) {
            DEFAULT_WIDTH_DP to DEFAULT_HEIGHT_DP
        }
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    /** Focus mode rotates through the focus set; off (or every word learned) = random pick. */
    private suspend fun pickWord(context: Context, wordDao: WordDao, lastWordId: Long): WordEntity? {
        val focusWord = if (WidgetPrefs.isFocusMode(context)) {
            nextFocusWord(wordDao.getEnabledWordsOnce(), lastWordId)
        } else null
        return focusWord ?: pickRandomWord(wordDao, lastWordId)
    }

    private suspend fun pickRandomWord(wordDao: WordDao, excludeId: Long): WordEntity? {
        val count = wordDao.getWordCountExcluding(excludeId)
        if (count <= 0) {
            return wordDao.getWordAtOffset(0)
        }
        val offset = Random.nextInt(count)
        return wordDao.getWordAtOffsetExcluding(excludeId, offset)
    }
}

/**
 * "De fiets" / "het huis" -> ("de", "fiets") / ("het", "huis"); anything else -> (null, word).
 * The noun loses its capital when the article had one (words added by the user are stored title-cased).
 */
internal fun splitArticle(word: String): Pair<String?, String> {
    val match = Regex("""^(de|het)\s+(\S.*)$""",RegexOption.IGNORE_CASE).find(word.trim()) ?: return null to word
    val noun = match.groupValues[2]
    return match.groupValues[1].lowercase() to noun.replaceFirstChar { it.lowercase() }
}

internal const val FOCUS_SET_SIZE = 20

/** The user's own words first, then the starter list in import order. */
private val focusOrder = compareBy<WordEntity>({ it.isDefault }, { it.id })

/** The first [size] enabled words not yet answered correctly in a quiz. */
internal fun focusSet(words: List<WordEntity>, size: Int = FOCUS_SET_SIZE): List<WordEntity> =
    words.filter { it.isEnabled && it.learnedAt == null }.sortedWith(focusOrder).take(size)

/**
 * The focus-set word after [lastWordId], wrapping around; null when every enabled word is learned.
 * A learned word leaves the set and the next unlearned word fills its slot.
 * ponytail: fixed set + learnedAt, no intervals. Upgrade path: SM-2 (ease, interval, dueAt columns)
 * so learned words come back for review, once the focus set proves itself.
 */
internal fun nextFocusWord(words: List<WordEntity>, lastWordId: Long, size: Int = FOCUS_SET_SIZE): WordEntity? {
    val set = focusSet(words, size)
    val last = words.find { it.id == lastWordId } ?: return set.firstOrNull()
    return set.firstOrNull { focusOrder.compare(it, last) > 0 } ?: set.firstOrNull()
}

/**
 * Largest size in [minSp]..[maxSp] at which no word has to break, the text word-wraps
 * into at most [maxLines] lines, and those lines fit in [availHeightPx].
 * [measure] returns the width in px of a string at a given sp.
 */
internal fun fitSp(
    text: String,
    availWidthPx: Float,
    availHeightPx: Float,
    maxSp: Int = 28,
    minSp: Int = 14,
    maxLines: Int = 2,
    lineHeightPx: (sp: Int) -> Float,
    measure: (text: String, sp: Int) -> Float
): Int {
    val words = text.split(' ').filter { it.isNotEmpty() }
    for (sp in maxSp downTo minSp) {
        if (words.any { measure(it, sp) > availWidthPx }) continue
        val lines = lineCount(text, availWidthPx, sp, measure)
        if (lines > maxLines) continue
        if (lines * lineHeightPx(sp) > availHeightPx) continue
        return sp
    }
    // Last resort: let the TextView wrap inside the word.
    return minSp
}

/** Greedy word-wrap, the same as the widget TextView's breakStrategy="simple". */
internal fun lineCount(text: String, availWidthPx: Float, sp: Int, measure: (text: String, sp: Int) -> Float): Int {
    var lines = 0
    var current = ""
    for (word in text.split(' ').filter { it.isNotEmpty() }) {
        val candidate = if (current.isEmpty()) word else "$current $word"
        if (current.isNotEmpty() && measure(candidate, sp) > availWidthPx) {
            lines++
            current = word
        } else {
            current = candidate
        }
    }
    return if (current.isEmpty()) lines else lines + 1
}
