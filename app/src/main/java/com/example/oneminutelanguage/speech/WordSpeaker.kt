package com.example.oneminutelanguage.speech

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.translation.withoutHints
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/** One line of a dialogue for [WordSpeaker.speakQueue]; speaker B gets the second voice, or a lower pitch. */
class SpeakLine(val text: String, val speakerB: Boolean = false)

object WordSpeaker {
    /** The 🐢 button: slower than the speed chosen in Settings. */
    const val SLOW_RATE = 0.7f
    private const val PITCH_B = 0.85f

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var isReady = false

    /** The last request made before the engine was ready; it runs as soon as the engine is. */
    private var pending: ((TextToSpeech) -> Unit)? = null

    private val main = Handler(Looper.getMainLooper())

    private class Queue(val generation: Int, val last: Int, val onLineStart: (Int) -> Unit, val onDone: () -> Unit)

    @Volatile
    private var queue: Queue? = null
    private var generation = 0
    private var customVoice = false

    private val _voiceMissing = MutableStateFlow(false)

    /** True once the engine reported that the language has no voice data (audio screens show an install banner). */
    val voiceMissing: StateFlow<Boolean> = _voiceMissing

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = notify(utteranceId, done = false)
        override fun onDone(utteranceId: String?) = notify(utteranceId, done = true)

        @Deprecated("Deprecated in Java", ReplaceWith("onError(utteranceId, -1)"))
        override fun onError(utteranceId: String?) = notify(utteranceId, done = true)
    }

    /** Utterance ids are "line_<generation>_<index>"; callbacks of a stopped or replaced queue are dropped. */
    private fun notify(utteranceId: String?, done: Boolean) {
        val parts = utteranceId?.split('_') ?: return
        if (parts.size != 3 || parts[0] != "line") return
        val gen = parts[1].toIntOrNull() ?: return
        val index = parts[2].toIntOrNull() ?: return
        main.post {
            val q = queue?.takeIf { it.generation == gen } ?: return@post
            if (!done) q.onLineStart(index)
            else if (index == q.last) {
                queue = null
                q.onDone()
            }
        }
    }

    @Synchronized
    private fun withEngine(context: Context, action: (TextToSpeech) -> Unit) {
        val engine = tts
        if (engine != null && isReady) {
            action(engine)
            return
        }
        pending = action
        if (engine == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isReady = true
                    flushPending()
                } else {
                    synchronized(this) {
                        tts = null
                        pending = null
                    }
                }
            }
        }
    }

    @Synchronized
    private fun flushPending() {
        val engine = tts ?: return
        engine.setOnUtteranceProgressListener(listener)
        val action = pending
        pending = null
        action?.invoke(engine)
    }

    /** Language, speed and pitch for the next utterance. False (and [voiceMissing]) when the language has no voice. */
    private fun prepare(engine: TextToSpeech, locale: Locale, rate: Float, pitch: Float = 1f): Boolean {
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _voiceMissing.value = true
            return false
        }
        _voiceMissing.value = false
        engine.setSpeechRate(rate)
        engine.setPitch(pitch)
        return true
    }

    /** A dialogue left a voice behind; go back to the language's own voice for single words. */
    private fun resetVoice(engine: TextToSpeech) {
        if (!customVoice) return
        customVoice = false
        engine.defaultVoice?.let { engine.voice = it }
    }

    /** Speaks [text] now, replacing whatever is playing. [rateOverride] beats the speed from Settings. */
    fun speak(context: Context, text: String, languageCode: String, rateOverride: Float? = null) {
        val clean = text.withoutHints()
        if (clean.isEmpty()) return
        val locale = Locale.forLanguageTag(languageCode)
        val rate = rateOverride ?: CoursePrefs.ttsRate(context)
        withEngine(context) { engine ->
            queue = null
            resetVoice(engine)
            if (prepare(engine, locale, rate)) engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "widget_word")
        }
    }

    /**
     * Speaks the lines one after the other. [onLineStart] gets the index of each line as it begins, [onDone] is
     * called after the last one (not when [stop] or another speak call cuts it off). Callbacks run on the main thread.
     */
    fun speakQueue(
        context: Context,
        languageCode: String,
        lines: List<SpeakLine>,
        rateOverride: Float? = null,
        onLineStart: (Int) -> Unit,
        onDone: () -> Unit
    ) {
        if (lines.isEmpty()) return onDone()
        val locale = Locale.forLanguageTag(languageCode)
        val rate = rateOverride ?: CoursePrefs.ttsRate(context)
        withEngine(context) { engine ->
            engine.stop()
            val gen = ++generation
            queue = Queue(gen, lines.lastIndex, onLineStart, onDone)
            if (!prepare(engine, locale, rate)) {
                queue = null
                main.post(onDone)
                return@withEngine
            }
            val voices = dutchVoices()
            lines.forEachIndexed { i, line ->
                // Speaker A = first voice, speaker B = second voice, or the same voice a little lower.
                val voice = if (line.speakerB) voices.getOrNull(1) ?: voices.firstOrNull() else voices.firstOrNull()
                if (voice != null) {
                    engine.voice = voice
                    customVoice = true
                }
                engine.setPitch(if (line.speakerB && voices.size < 2) PITCH_B else 1f)
                engine.setSpeechRate(rate)
                engine.speak(line.text.withoutHints(), TextToSpeech.QUEUE_ADD, null, "line_${gen}_$i")
            }
        }
    }

    /** Dutch voices that work offline and are installed, nl-NL first. Empty until the engine is ready. */
    fun dutchVoices(): List<Voice> = try {
        tts?.takeIf { isReady }?.voices.orEmpty()
            .filter {
                it.locale.language == "nl" && !it.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
            }
            .sortedWith(compareBy({ it.locale.country != "NL" }, { it.name }))
    } catch (e: Exception) {
        emptyList()
    }

    /** Starts the engine and sets [voiceMissing] for the language (Settings shows the result). */
    fun checkVoice(context: Context, languageCode: String) {
        val locale = Locale.forLanguageTag(languageCode)
        withEngine(context) { engine -> prepare(engine, locale, 1f) }
    }

    fun stop() {
        queue = null
        generation++
        if (isReady) tts?.stop()
    }

    /** Opens the system screen that installs voice data; false when no app handles it. */
    fun installVoiceData(context: Context): Boolean = try {
        context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
