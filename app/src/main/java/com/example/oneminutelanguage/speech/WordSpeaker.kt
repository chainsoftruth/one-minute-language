package com.example.oneminutelanguage.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object WordSpeaker {
    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var isReady = false

    private var pendingText: String? = null
    private var pendingLocale: Locale? = null

    private val parenthetical = Regex("""\s*\([^)]*\)""")

    @Synchronized
    fun speak(context: Context, text: String, languageCode: String) {
        val clean = parenthetical.replace(text, "").trim()
        if (clean.isEmpty()) return
        val locale = Locale.forLanguageTag(languageCode)

        val engine = tts
        if (engine != null && isReady) {
            doSpeak(engine, clean, locale)
            return
        }

        pendingText = clean
        pendingLocale = locale

        if (engine == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isReady = true
                    flushPending()
                } else {
                    synchronized(this) {
                        tts = null
                        pendingText = null
                        pendingLocale = null
                    }
                }
            }
        }
    }

    @Synchronized
    private fun flushPending() {
        val engine = tts ?: return
        val text = pendingText
        val locale = pendingLocale
        pendingText = null
        pendingLocale = null
        if (text != null && locale != null) {
            doSpeak(engine, text, locale)
        }
    }

    private fun doSpeak(engine: TextToSpeech, text: String, locale: Locale) {
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            return
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "widget_word")
    }
}
