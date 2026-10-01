package com.example.oneminutelanguage.ui.learn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.oneminutelanguage.course.Line
import com.example.oneminutelanguage.course.Match
import com.example.oneminutelanguage.course.SPEECH_ALMOST
import com.example.oneminutelanguage.course.SPEECH_CORRECT
import com.example.oneminutelanguage.course.SPEECH_TRIES
import com.example.oneminutelanguage.course.best
import com.example.oneminutelanguage.speech.SpeakLine
import com.example.oneminutelanguage.speech.WordSpeaker

private const val NORMAL_RATE = 1.0f
private const val SLOW_DIALOGUE_RATE = 0.8f

/** One speech bubble: speaker A on the left, B on the right. */
@Composable
private fun Bubble(line: Line, text: String?, english: Boolean, active: Boolean) {
    val right = line.who == "B"
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (right) Arrangement.End else Arrangement.Start) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(line.who, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text ?: "···", style = MaterialTheme.typography.bodyLarge)
                if (english) Text(line.en, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * The dialogue played by two voices, line by line. The text is hidden at first (listen!) and switches on after the
 * first full play; "Show English" is separate. Pause resumes at the current line.
 */
@Composable
internal fun ColumnScope.DialoguePhase(viewModel: LessonViewModel, onClose: () -> Unit) {
    val lesson = viewModel.lesson ?: return
    val lines = lesson.dialogue
    val context = LocalContext.current
    var current by remember { mutableIntStateOf(-1) }
    var reached by remember { mutableIntStateOf(-1) }
    var playing by remember { mutableStateOf(false) }
    var slow by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showEnglish by remember { mutableStateOf(false) }
    var playedOnce by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { WordSpeaker.stop() } }

    fun play(from: Int) {
        playing = true
        if (from == 0) reached = -1
        WordSpeaker.speakQueue(
            context, viewModel.ttsLocale, lines.drop(from).map { SpeakLine(it.nl, it.who == "B") },
            rateOverride = if (slow) SLOW_DIALOGUE_RATE else NORMAL_RATE,
            onLineStart = { i ->
                current = from + i
                reached = maxOf(reached, current)
            },
            onDone = {
                playing = false
                current = -1
                if (!playedOnce) {
                    playedOnce = true
                    showText = true
                }
            }
        )
    }
    fun pause() {
        WordSpeaker.stop()
        playing = false
    }

    CloseRow(title = lesson.title, progress = null, onClose = onClose)
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VoiceBanner()
        Text(
            if (playedOnce) "Listen again, or turn the English on." else "Listen first. The text stays hidden until the end.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = showText, onClick = { showText = !showText }, label = { Text("Show text") })
            FilterChip(selected = showEnglish, onClick = { showEnglish = !showEnglish }, label = { Text("Show English") })
        }
        lines.forEachIndexed { i, line ->
            // Lines appear as they are spoken; after a full play they are all there.
            AnimatedVisibility(visible = playedOnce || i <= reached) {
                Bubble(line, text = line.nl.takeIf { showText }, english = showEnglish, active = i == current)
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { WordSpeaker.stop(); play(0) }) { Icon(Icons.Default.Replay, contentDescription = "Play from the start") }
        FilledIconButton(
            onClick = { if (playing) pause() else play(current.coerceAtLeast(0)) },
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                modifier = Modifier.size(32.dp)
            )
        }
        FilterChip(selected = slow, onClick = { slow = !slow }, label = { Text("0.8×") })
    }
    Button(
        onClick = { WordSpeaker.stop(); viewModel.startItems() },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp)
    ) { Text(if (lesson.items.isEmpty()) "Finish" else "Start questions") }
}

/**
 * Roleplay: the app speaks A, the learner says the B lines (scored with the speech recogniser, or self-graded
 * without one). A line is passed at 0.5 or after [SPEECH_TRIES] tries. "Try from memory" hides the B text.
 */
@Composable
internal fun ColumnScope.RoleplayPhase(viewModel: LessonViewModel, onClose: () -> Unit) {
    val lesson = viewModel.lesson ?: return
    val lines = lesson.dialogue
    val context = LocalContext.current
    var pos by remember { mutableIntStateOf(0) }
    var tries by remember { mutableIntStateOf(0) }
    var lastTry by remember { mutableStateOf<Match?>(null) }
    var fromMemory by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { WordSpeaker.stop() } }

    // The app plays every A line by itself; the learner's turn starts on a B line.
    LaunchedEffect(pos) {
        val line = lines.getOrNull(pos) ?: return@LaunchedEffect
        if (line.who != "B") {
            WordSpeaker.speakQueue(
                context, viewModel.ttsLocale, listOf(SpeakLine(line.nl)), onLineStart = {}, onDone = { pos++ }
            )
        }
    }
    fun next() {
        pos++
        tries = 0
        lastTry = null
    }
    val line = lines.getOrNull(pos)
    val recorder = rememberSpeechRecorder(viewModel.ttsLocale) { guesses ->
        val target = lines.getOrNull(pos)?.takeIf { it.who == "B" } ?: return@rememberSpeechRecorder
        if (guesses.isEmpty()) return@rememberSpeechRecorder
        val match = best(guesses, target.nl)
        tries++
        lastTry = match
        if (match.score >= SPEECH_ALMOST || tries >= SPEECH_TRIES) next()
    }

    CloseRow(title = lesson.title, progress = null, trailing = "${pos.coerceAtMost(lines.size)} / ${lines.size}", onClose = onClose)
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VoiceBanner()
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("You play speaker B", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text("Try from memory", style = MaterialTheme.typography.labelLarge)
            Switch(checked = fromMemory, onCheckedChange = { fromMemory = it })
        }
        lines.forEachIndexed { i, l ->
            if (i < pos) Bubble(l, text = l.nl, english = false, active = false)
        }
        if (line != null && line.who != "B") {
            Bubble(line, text = line.nl, english = false, active = true)
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speaker A is talking", tint = MaterialTheme.colorScheme.primary)
        }
        if (line != null && line.who == "B") {
            val hidden = fromMemory && tries == 0
            Bubble(line, text = line.nl.takeIf { !hidden }, english = true, active = true)
            lastTry?.let {
                Text(
                    if (it.score >= SPEECH_CORRECT) "Great!" else "Try again (${tries} / $SPEECH_TRIES). Missed words are underlined.",
                    style = MaterialTheme.typography.bodyMedium
                )
                MatchedWords(it.words)
            }
            if (recorder.available) {
                MicButton(recorder, "Tap and say your line")
            } else {
                Text("No speech recogniser found. Say your line out loud.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = ::next, modifier = Modifier.fillMaxWidth()) { Text("I said it") }
            }
            TextButton(onClick = ::next) { Text("Skip this line") }
        }
        if (line == null) {
            Text("Dialogue complete", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = { pos = 0; tries = 0; lastTry = null }, modifier = Modifier.fillMaxWidth()) { Text("Play it again") }
        }
    }
    if (line == null) {
        Button(
            onClick = { WordSpeaker.stop(); viewModel.startItems() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp)
        ) { Text(if (lesson.items.isEmpty()) "Finish" else "Continue") }
    }
}
