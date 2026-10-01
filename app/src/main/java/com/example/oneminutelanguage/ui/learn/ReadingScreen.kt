package com.example.oneminutelanguage.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.example.oneminutelanguage.course.sendToWidgetMessage
import com.example.oneminutelanguage.course.sentenceRanges
import com.example.oneminutelanguage.course.wordRanges
import com.example.oneminutelanguage.speech.SpeakLine
import com.example.oneminutelanguage.speech.WordSpeaker
import com.example.oneminutelanguage.ui.components.appCardColors
import kotlinx.coroutines.launch

/**
 * The reading text before its questions. Every word can be tapped for the lexicon entry behind it, and the whole
 * text can be read aloud sentence by sentence with the current sentence highlighted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColumnScope.ReadingPhase(viewModel: LessonViewModel, onClose: () -> Unit) {
    val lesson = viewModel.lesson ?: return
    val text = lesson.text ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sentences = remember(text) { sentenceRanges(text) }
    val words = remember(text) { wordRanges(text) }
    var current by remember { mutableIntStateOf(-1) }
    var playing by remember { mutableStateOf(false) }
    var tapped by remember { mutableStateOf<String?>(null) }
    val highlight = MaterialTheme.colorScheme.primaryContainer

    DisposableEffect(Unit) { onDispose { WordSpeaker.stop() } }

    val annotated = remember(text, current, highlight) {
        buildAnnotatedString {
            append(text)
            sentences.getOrNull(current)?.let { addStyle(SpanStyle(background = highlight), it.first, it.last + 1) }
            for (range in words) {
                val word = text.substring(range.first, range.last + 1)
                addLink(LinkAnnotation.Clickable(tag = word, linkInteractionListener = { tapped = word }), range.first, range.last + 1)
            }
        }
    }

    CloseRow(title = lesson.title, progress = null, onClose = onClose)
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VoiceBanner()
        Text(
            "Tap any word to see what it means.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = appCardColors()) {
            val style = MaterialTheme.typography.bodyLarge
            Text(annotated, style = style.copy(lineHeight = style.fontSize * 1.5f), modifier = Modifier.padding(20.dp))
        }
        OutlinedButton(
            onClick = {
                if (playing) {
                    WordSpeaker.stop()
                    playing = false
                    current = -1
                } else {
                    playing = true
                    WordSpeaker.speakQueue(
                        context, viewModel.ttsLocale, sentences.map { SpeakLine(text.substring(it.first, it.last + 1)) },
                        onLineStart = { current = it },
                        onDone = { playing = false; current = -1 }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(if (playing) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
            Text(if (playing) "Stop" else "Listen to the text", modifier = Modifier.padding(start = 8.dp))
        }
    }
    Button(
        onClick = { WordSpeaker.stop(); viewModel.startItems() },
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) { Text(if (lesson.items.isEmpty()) "Finish" else "Questions") }

    tapped?.let { word ->
        var note by remember(word) { mutableStateOf<String?>(null) }
        val results = viewModel.lookup(word)
        ModalBottomSheet(onDismissRequest = { tapped = null }) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (results.isEmpty()) {
                    Text(word, style = MaterialTheme.typography.displaySmall)
                    Text("Not in the course dictionary", style = MaterialTheme.typography.titleMedium)
                    WoordenlijstButton(word)
                } else {
                    results.forEachIndexed { i, entry ->
                        if (i > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        WordDetails(entry, viewModel.ttsLocale, viewModel.canSend) {
                            scope.launch {
                                val (added, already) = viewModel.addToWords(entry)
                                note = sendToWidgetMessage(added, already)
                            }
                        }
                    }
                }
                note?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}
