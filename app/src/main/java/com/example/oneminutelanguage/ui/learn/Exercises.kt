package com.example.oneminutelanguage.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.oneminutelanguage.course.Item
import com.example.oneminutelanguage.course.ItemResult
import com.example.oneminutelanguage.course.orderTiles
import com.example.oneminutelanguage.ui.components.AnswerOption
import com.example.oneminutelanguage.ui.components.AnswerState
import com.example.oneminutelanguage.ui.components.appCardColors
import kotlinx.coroutines.delay

// One composable per item type. Typed and tile answers are reported upward with onInput (null = nothing yet);
// the lesson screen's "Check" button then grades them. Local state is keyed by `step` so each item starts fresh.

@Composable
fun ChoiceExercise(item: Item.Choice, step: Int, selected: Int?, result: ItemResult?, onSelect: (Int) -> Unit) {
    // Two options (de / het) keep their authored order; longer lists are shuffled so the answer isn't always first.
    val order = remember(step) { if (item.options.size <= 2) item.options.indices.toList() else item.options.indices.shuffled() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Prompt(item.q, item.en)
        order.forEach { i ->
            AnswerOption(
                text = item.options[i],
                state = when {
                    result == null -> if (i == selected) AnswerState.SELECTED else AnswerState.IDLE
                    i == item.answer -> AnswerState.CORRECT
                    i == selected -> AnswerState.WRONG
                    else -> AnswerState.DIMMED
                },
                onClick = { onSelect(i) }
            )
        }
    }
}

@Composable
fun GapExercise(item: Item.Gap, step: Int, enabled: Boolean, onInput: (String?) -> Unit, onDone: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Prompt(item.text, item.en)
        AnswerField(step, enabled, item.hint ?: "Fill the gap", onInput, onDone)
    }
}

@Composable
fun TransformExercise(item: Item.Transform, step: Int, enabled: Boolean, onInput: (String?) -> Unit, onDone: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Prompt(item.q, item.instruction)
        AnswerField(step, enabled, "Your answer", onInput, onDone)
    }
}

@Composable
fun TranslateExercise(item: Item.Translate, step: Int, enabled: Boolean, onInput: (String?) -> Unit, onDone: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Prompt(item.en, "Translate into Dutch")
        AnswerField(step, enabled, "Type in Dutch", onInput, onDone)
    }
}

@Composable
private fun Prompt(text: String, sub: String?) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = appCardColors()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text, style = MaterialTheme.typography.headlineSmall)
            if (sub != null) {
                Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AnswerField(step: Int, enabled: Boolean, placeholder: String, onInput: (String?) -> Unit, onDone: () -> Unit) {
    var text by remember(step) { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onInput(it.ifBlank { null })
        },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderExercise(item: Item.Order, step: Int, enabled: Boolean, onInput: (String?) -> Unit) {
    val tiles = remember(step) { orderTiles(item) }
    val chosen = remember(step) { mutableStateListOf<Int>() }
    fun report() = onInput(chosen.takeIf { it.isNotEmpty() }?.joinToString(" ") { tiles[it] })

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Prompt(item.en ?: "Put the words in order", "Tap the words in the right order")
        FlowRow(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chosen.forEach { i ->
                AssistChip(
                    onClick = { if (enabled) { chosen.remove(i); report() } },
                    label = { Text(tiles[i], style = MaterialTheme.typography.bodyLarge) }
                )
            }
        }
        HorizontalDivider()
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.indices.filter { it !in chosen }.forEach { i ->
                AssistChip(
                    onClick = { if (enabled) { chosen.add(i); report() } },
                    label = { Text(tiles[i], style = MaterialTheme.typography.bodyLarge) }
                )
            }
        }
    }
}

/** Tap a word on the left, then its partner on the right. Reports once, when every pair is matched. */
@Composable
fun MatchExercise(item: Item.Match, step: Int, onDone: (mistakes: Int) -> Unit) {
    val pairs = item.pairs
    val left = remember(step) { pairs.indices.shuffled() }
    val right = remember(step) { pairs.indices.shuffled() }
    var selectedLeft by remember(step) { mutableStateOf<Int?>(null) }
    val matched = remember(step) { mutableStateListOf<Int>() }
    var mistakes by remember(step) { mutableIntStateOf(0) }
    var flash by remember(step) { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(flash) {
        if (flash != null) {
            delay(600)
            flash = null
        }
    }
    LaunchedEffect(matched.size) {
        if (matched.size == pairs.size) {
            delay(500)
            onDone(mistakes)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Prompt("Match the pairs", "Tap a word, then its partner")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                left.forEach { i ->
                    AnswerOption(
                        text = pairs[i][0],
                        state = when {
                            i in matched -> AnswerState.CORRECT
                            flash?.first == i -> AnswerState.WRONG
                            i == selectedLeft -> AnswerState.SELECTED
                            else -> AnswerState.IDLE
                        },
                        enabled = i !in matched && flash == null,
                        onClick = { selectedLeft = i }
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                right.forEach { j ->
                    AnswerOption(
                        text = pairs[j][1],
                        state = when {
                            j in matched -> AnswerState.CORRECT
                            flash?.second == j -> AnswerState.WRONG
                            else -> AnswerState.IDLE
                        },
                        enabled = j !in matched && flash == null && selectedLeft != null,
                        onClick = {
                            val l = selectedLeft ?: return@AnswerOption
                            if (l == j) {
                                matched.add(j)
                            } else {
                                mistakes++
                                flash = l to j
                            }
                            selectedLeft = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ComingSoonExercise(onSkip: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = appCardColors()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Coming in a later update", style = MaterialTheme.typography.titleMedium)
            Text(
                "Listening, speaking and writing exercises are on their way.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onSkip) { Text("Skip") }
        }
    }
}
