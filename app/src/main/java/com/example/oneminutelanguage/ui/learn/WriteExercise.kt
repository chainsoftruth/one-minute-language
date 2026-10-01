package com.example.oneminutelanguage.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.Item
import com.example.oneminutelanguage.course.LtMatch
import com.example.oneminutelanguage.course.LtResult
import com.example.oneminutelanguage.course.applyFix
import com.example.oneminutelanguage.course.checkWithLanguageTool
import com.example.oneminutelanguage.course.wordCount
import com.example.oneminutelanguage.course.writingScore
import com.example.oneminutelanguage.speech.WordSpeaker
import com.example.oneminutelanguage.ui.components.appCardColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CHECK_COOLDOWN_MS = 5_000L // the free server allows 20 checks a minute
private const val DRAFT_DELAY_MS = 500L

/**
 * A writing task: the situation and required points, a text field with a live word counter (the draft is kept
 * between visits), an optional LanguageTool check, the model answer, and a self-assessment of the required points.
 * Writing is finished, not graded: the lesson counts it as done.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WriteExercise(item: Item.Write, step: Int, draftKey: String, locale: String, onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember(draftKey) { mutableStateOf(CoursePrefs.writingDraft(context, draftKey)) }
    val latest by rememberUpdatedState(text)
    var assessing by remember(step) { mutableStateOf(false) }
    var showModel by remember(step) { mutableStateOf(false) }
    var covered by remember(step) { mutableStateOf(emptySet<Int>()) }
    var asking by remember(step) { mutableStateOf(false) }
    var loading by remember(step) { mutableStateOf(false) }
    var cooling by remember(step) { mutableStateOf(false) }
    var checked by remember(step) { mutableStateOf(false) }
    var result by remember(step) { mutableStateOf<LtResult?>(null) }
    var matches by remember(step) { mutableStateOf(emptyList<LtMatch>()) }

    LaunchedEffect(text) {
        delay(DRAFT_DELAY_MS)
        CoursePrefs.setWritingDraft(context, draftKey, text)
    }
    DisposableEffect(draftKey) { onDispose { CoursePrefs.setWritingDraft(context, draftKey, latest) } }

    val words = wordCount(text)
    val inRange = words in item.minWords..item.maxWords

    fun runCheck() {
        scope.launch {
            loading = true
            cooling = true
            val r = checkWithLanguageTool(text)
            result = r
            matches = (r as? LtResult.Ok)?.matches.orEmpty()
            checked = true
            loading = false
            delay(CHECK_COOLDOWN_MS)
            cooling = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = appCardColors()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Writing task", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(item.task, style = MaterialTheme.typography.bodyLarge)
                if (item.points.isNotEmpty()) {
                    Text("Include:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
                    item.points.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
                }
                Text(
                    "Write ${item.minWords}–${item.maxWords} words.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!assessing) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it; result = null; matches = emptyList() },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                label = { Text("Your text") },
                supportingText = {
                    Text(
                        "$words ${if (words == 1) "word" else "words"}" + when {
                            words == 0 -> ""
                            words < item.minWords -> " · a bit short"
                            words > item.maxWords -> " · a bit long"
                            else -> " · ✓ good length"
                        },
                        color = if (words == 0 || inRange) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
            )

            OutlinedButton(
                onClick = { if (CoursePrefs.languageToolEnabled(context)) runCheck() else asking = true },
                enabled = text.isNotBlank() && !loading && !cooling,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Check with LanguageTool") }
            CheckResult(result, loading, text, matches) { fixed, replacement ->
                val (newText, rest) = applyFix(text, matches, fixed, replacement)
                text = newText
                matches = rest
            }

            if (checked || words >= item.minWords) {
                OutlinedButton(onClick = { showModel = !showModel }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (showModel) "Hide model answer" else "Show model answer")
                }
                if (showModel) ModelAnswer(item.model, locale)
            }
            Button(onClick = { assessing = true }, enabled = words > 0, modifier = Modifier.fillMaxWidth()) { Text("Done") }
        } else {
            Text("How did you do?", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tick every point your text covers. Compare with the model answer if you like.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            item.points.forEachIndexed { i, point ->
                val on = i in covered
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .toggleable(value = on, role = Role.Checkbox) { covered = if (on) covered - i else covered + i },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = on, onCheckedChange = null)
                    Text(point, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                }
            }
            Text(
                if (words < item.minWords) "Your text has $words words and the minimum is ${item.minWords}. Add a little more next time."
                else "You covered ${covered.size} of ${item.points.size} points (${(100 * writingScore(covered.size, item.points.size, words, item.minWords)).toInt()}%).",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = { showModel = !showModel }, modifier = Modifier.fillMaxWidth()) {
                Text(if (showModel) "Hide model answer" else "Show model answer")
            }
            if (showModel) ModelAnswer(item.model, locale)
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Finish") }
            TextButton(onClick = { assessing = false }, modifier = Modifier.fillMaxWidth()) { Text("Back to my text") }
        }
    }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text("Check with LanguageTool?") },
            text = {
                Text(
                    "Your text will be sent to languagetool.org, a free public service, to look for spelling and " +
                        "grammar mistakes. You can turn this off again in Settings."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    CoursePrefs.setLanguageToolEnabled(context, true)
                    asking = false
                    runCheck()
                }) { Text("Turn on and check") }
            },
            dismissButton = { TextButton(onClick = { asking = false }) { Text("Not now") } }
        )
    }
}

@Composable
private fun ModelAnswer(model: String, locale: String) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = appCardColors()) {
        Row(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Model answer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(model, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = { WordSpeaker.speak(context, model, locale) }) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play the model answer", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** What LanguageTool found: the text with the problems underlined, and each problem with its suggestions (tap = apply). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckResult(result: LtResult?, loading: Boolean, text: String, matches: List<LtMatch>, onFix: (LtMatch, String) -> Unit) {
    when {
        loading -> CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        result == null -> Unit
        result is LtResult.Offline -> Note("No internet connection. Try again when you are online.")
        result is LtResult.RateLimited -> Note("LanguageTool is busy. Wait a minute and try again.")
        result is LtResult.Failed -> Note(result.message)
        matches.isEmpty() -> Note("✓ LanguageTool found nothing to change. It does not catch every mistake, so also read your text once more.")
        else -> Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = appCardColors()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val error = MaterialTheme.colorScheme.error
                Text(
                    buildAnnotatedString {
                        append(text)
                        matches.forEach {
                            val start = it.offset.coerceIn(0, text.length)
                            val end = (it.offset + it.length).coerceIn(start, text.length)
                            addStyle(SpanStyle(color = error, textDecoration = TextDecoration.Underline), start, end)
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                matches.forEach { m ->
                    val wrong = text.substring(m.offset.coerceIn(0, text.length), (m.offset + m.length).coerceIn(0, text.length))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("“$wrong”: ${m.message}", style = MaterialTheme.typography.bodyMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            m.replacements.take(3).forEach { r ->
                                AssistChip(onClick = { onFix(m, r.value) }, label = { Text("→ ${r.value}") })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
