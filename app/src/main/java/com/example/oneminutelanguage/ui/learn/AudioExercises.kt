package com.example.oneminutelanguage.ui.learn

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.oneminutelanguage.course.Item
import com.example.oneminutelanguage.course.ItemResult
import com.example.oneminutelanguage.course.Match
import com.example.oneminutelanguage.course.SPEECH_TRIES
import com.example.oneminutelanguage.speech.WordSpeaker
import com.example.oneminutelanguage.ui.components.AnswerOption
import com.example.oneminutelanguage.ui.components.AnswerState
import com.example.oneminutelanguage.ui.components.appCardColors
import com.example.oneminutelanguage.ui.theme.appColors
import kotlinx.coroutines.delay

// Listening and speaking exercises. Audio is the device's Dutch TTS voice; speaking is graded by the system
// speech recogniser. Both can be missing, so the screens say so and speaking falls back to self-grading.

/** "Install Dutch voice" when the engine reported that the voice data is missing. */
@Composable
fun VoiceBanner() {
    val missing by WordSpeaker.voiceMissing.collectAsState()
    if (!missing) return
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("No Dutch voice found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            Text(
                "Audio needs the Dutch voice data of your text-to-speech engine.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = { WordSpeaker.installVoiceData(context) }) { Text("Install Dutch voice") }
        }
    }
}

/** A big play button, a 🐢 slow button and a play counter. Plays once by itself when the item appears. */
@Composable
fun ListenControls(text: String, step: Int, locale: String, autoPlay: Boolean = true) {
    val context = LocalContext.current
    var plays by remember(step) { mutableIntStateOf(0) }
    val play: (Float?) -> Unit = { rate ->
        plays++
        WordSpeaker.speak(context, text, locale, rate)
    }
    LaunchedEffect(step) { if (autoPlay) play(null) }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilledIconButton(onClick = { play(null) }, modifier = Modifier.size(96.dp)) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play", modifier = Modifier.size(48.dp))
            }
            FilledTonalIconButton(onClick = { play(WordSpeaker.SLOW_RATE) }, modifier = Modifier.size(56.dp)) {
                Text("🐢", style = MaterialTheme.typography.headlineSmall)
            }
        }
        Text(
            if (plays == 0) "Tap to listen" else "Played $plays×",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Hear a sentence, pick what it means (or answers the question). The Dutch text appears after answering. */
@Composable
fun ListenExercise(item: Item.Listen, step: Int, locale: String, selected: Int?, result: ItemResult?, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VoiceBanner()
        Prompt(item.q ?: "What do you hear?", null)
        ListenControls(item.nl, step, locale)
        item.options.forEachIndexed { i, option ->
            AnswerOption(
                text = option,
                state = when {
                    result == null -> if (i == selected) AnswerState.SELECTED else AnswerState.IDLE
                    i == item.answer -> AnswerState.CORRECT
                    i == selected -> AnswerState.WRONG
                    else -> AnswerState.DIMMED
                },
                onClick = { onSelect(i) }
            )
        }
        if (result != null) Text(item.nl, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

/** Hear a sentence, type it. */
@Composable
fun DictationExercise(item: Item.Listen, step: Int, locale: String, enabled: Boolean, onInput: (String?) -> Unit, onDone: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VoiceBanner()
        Prompt(item.q ?: "Type what you hear", null)
        ListenControls(item.nl, step, locale)
        AnswerField(step, enabled, "Type in Dutch", onInput, onDone, minLines = 2)
    }
}

class SpeechRecorder(val available: Boolean, val start: () -> Unit)

/**
 * Opens the system speech recogniser (Dutch) and hands the guesses to [onHeard]. No RECORD_AUDIO permission is
 * needed because the recogniser app does the recording. [SpeechRecorder.available] is false without one.
 */
@Composable
fun rememberSpeechRecorder(locale: String, onHeard: (List<String>) -> Unit): SpeechRecorder {
    val context = LocalContext.current
    var failed by remember { mutableStateOf(false) }
    val heard by rememberUpdatedState(onHeard)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            heard(r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty().filter { it.isNotBlank() })
        }
    }
    val installed = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    return SpeechRecorder(installed && !failed) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        try {
            launcher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            failed = true
        }
    }
}

/** The target's words: heard ones green, missed ones red, bold and underlined (not colour alone). */
@Composable
fun MatchedWords(words: List<Pair<String, Boolean>>, modifier: Modifier = Modifier) {
    val error = MaterialTheme.colorScheme.error
    val ok = MaterialTheme.appColors.success
    Text(
        text = buildAnnotatedString {
            words.forEachIndexed { i, (word, matched) ->
                if (i > 0) append(' ')
                if (matched) withStyle(SpanStyle(color = ok, fontWeight = FontWeight.Medium)) { append(word) }
                else withStyle(SpanStyle(color = error, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append(word) }
            }
        },
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
    )
}

@Composable
fun MicButton(recorder: SpeechRecorder, label: String = "Tap and say it", onClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledIconButton(onClick = { onClick(); recorder.start() }, modifier = Modifier.size(80.dp)) {
            Icon(Icons.Default.Mic, contentDescription = "Speak", modifier = Modifier.size(40.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Say the sentence. With a recogniser the guess is scored (up to [SPEECH_TRIES] tries while it is close); without
 * one the learner shadows: listen, say it out loud, grade themselves.
 */
@Composable
fun SpeakExercise(
    item: Item.Speak, step: Int, locale: String, tries: Int, lastTry: Match?, result: ItemResult?,
    onHeard: (List<String>) -> Unit, onSelfGrade: (Boolean) -> Unit, onSkip: () -> Unit
) {
    val recorder = rememberSpeechRecorder(locale, onHeard)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VoiceBanner()
        Prompt(item.nl, item.en)
        ListenControls(item.nl, step, locale, autoPlay = false)
        if (result == null) {
            if (recorder.available) {
                lastTry?.let {
                    Text("Try again ($tries / $SPEECH_TRIES). Missed words are underlined.", style = MaterialTheme.typography.bodyMedium)
                    MatchedWords(it.words)
                }
                MicButton(recorder)
            } else {
                Text("No speech recogniser found. Listen, say it out loud, then grade yourself.", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onSelfGrade(true) }, modifier = Modifier.weight(1f)) { Text("I said it well") }
                    OutlinedButton(onClick = { onSelfGrade(false) }, modifier = Modifier.weight(1f)) { Text("Not yet") }
                }
            }
            TextButton(onClick = onSkip) { Text("Skip") }
        }
    }
}

/** Open speaking: a task, a timer, the mic (several takes), a checklist of points and a model answer. Not scored. */
@Composable
fun PromptExercise(item: Item.OpenPrompt, step: Int, locale: String, onDone: (covered: Int) -> Unit) {
    val context = LocalContext.current
    val takes = remember(step) { mutableStateListOf<String>() }
    val covered = remember(step) { mutableStateListOf<Int>() }
    var remaining by remember(step) { mutableIntStateOf(item.seconds) }
    var running by remember(step) { mutableStateOf(false) }
    var showModel by remember(step) { mutableStateOf(false) }
    val recorder = rememberSpeechRecorder(locale) { guesses -> guesses.firstOrNull()?.let { takes += it } }

    // The timer is a guide: it counts down while the learner talks and does not cut anything off.
    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
        running = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VoiceBanner()
        Prompt(item.task, "Speak for about ${item.seconds} seconds")
        Text("$remaining s", style = MaterialTheme.typography.displaySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
        if (recorder.available) {
            MicButton(recorder, if (takes.isEmpty()) "Tap and start speaking" else "Another take", onClick = { running = true })
        } else if (!running && remaining > 0) {
            Text("No speech recogniser found. Say your answer out loud.", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { running = true }, modifier = Modifier.fillMaxWidth()) { Text("Start timer") }
        }
        takes.forEach { Text("“$it”", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (item.points.isNotEmpty()) {
            Text("Did you cover these points?", style = MaterialTheme.typography.titleSmall)
            item.points.forEachIndexed { i, point ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = i in covered, onCheckedChange = { if (it) covered += i else covered -= i })
                    Text(point, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        OutlinedButton(onClick = { showModel = !showModel }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showModel) "Hide model answer" else "Show model answer")
        }
        if (showModel) {
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = appCardColors()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.model, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    FilledTonalIconButton(onClick = { WordSpeaker.speak(context, item.model, locale) }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play the model answer")
                    }
                }
            }
        }
        Button(onClick = { onDone(covered.size) }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}
