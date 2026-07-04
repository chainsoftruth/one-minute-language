package com.example.oneminutelanguage.speech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.oneminutelanguage.data.DatabaseProvider
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.widget.WidgetPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpeakWordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                val wordId = WidgetPrefs.getLastWordId(applicationContext)
                if (wordId >= 0) {
                    val word = withContext(Dispatchers.IO) {
                        DatabaseProvider.getDatabase(applicationContext).wordDao().getWordById(wordId)
                    }
                    if (word != null) {
                        WordSpeaker.speak(
                            applicationContext,
                            word.language2Word,
                            LanguageSettingsStore.getTargetLanguage(applicationContext)
                        )
                    }
                }
            } finally {
                finish()
            }
        }
    }
}
