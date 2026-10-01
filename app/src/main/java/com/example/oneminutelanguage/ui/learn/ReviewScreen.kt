package com.example.oneminutelanguage.ui.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.course.Item
import com.example.oneminutelanguage.ui.components.FeedbackPanel

/** The daily review: up to 20 due cards (words and earlier mistakes), graded into the spaced-repetition schedule. */
@Composable
fun ReviewScreen(onClose: () -> Unit, viewModel: ReviewViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        when (val phase = viewModel.phase) {
            ReviewPhase.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            ReviewPhase.NoCourse -> Message("Review", "Pick a course in the Learn tab first. Your words and mistakes will show up here.", "Close", onClose)
            is ReviewPhase.Empty -> Message(
                "All caught up",
                phase.next?.let { "Nothing to review right now. The next review is in $it." }
                    ?: "Nothing to review yet. Finish a vocabulary lesson and its words will show up here tomorrow.",
                "Close", onClose
            )
            ReviewPhase.Question -> QuestionPhase(viewModel, onClose)
            is ReviewPhase.Done -> Message(
                "Reviewed ${phase.reviewed}",
                if (phase.next == null) "Well done!" else "Well done! Next review in ${phase.next}.",
                "Done", onClose
            )
        }
    }
}

@Composable
private fun Message(title: String, text: String, button: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text(button) }
    }
}

@Composable
private fun ColumnScope.QuestionPhase(vm: ReviewViewModel, onClose: () -> Unit) {
    val result = vm.result
    ResultHaptics(result)

    CloseRow(
        title = "",
        progress = if (vm.total == 0) 0f else vm.done.toFloat() / vm.total,
        trailing = "${vm.done} / ${vm.total}",
        onClose = onClose
    )
    AnimatedContent(
        targetState = vm.step to vm.currentItem,
        modifier = Modifier.weight(1f),
        transitionSpec = {
            (slideInHorizontally(tween(250)) { it / 4 } + fadeIn(tween(250))) togetherWith
                (slideOutHorizontally(tween(250)) { -it / 4 } + fadeOut(tween(250)))
        },
        label = "review item"
    ) { (step, item) ->
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (item != null) ReviewExercise(item, step, vm)
        }
    }

    val item = vm.currentItem ?: return
    if (item is Item.Match) {
        if (result != null) FeedbackPanel(result, item.explain, onContinue = vm::continueNext)
        return
    }
    ReviewBottomBar(vm, item)
}

/** "I don't know" + "Check" until the answer is graded, then the feedback panel. */
@Composable
private fun ReviewBottomBar(vm: ReviewViewModel, item: Item) {
    val result = vm.result
    Box {
        if (result == null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = vm::giveUp) { Text("I don't know") }
                Button(onClick = vm::check, enabled = vm.pending != null, modifier = Modifier.weight(1f)) { Text("Check") }
            }
        }
        AnimatedVisibility(visible = result != null, enter = slideInVertically { it }) {
            if (result != null) FeedbackPanel(result, item.explain, onContinue = vm::continueNext)
        }
    }
}

@Composable
private fun ReviewExercise(item: Item, step: Int, vm: ReviewViewModel) {
    val checked = vm.result != null
    val onInput: (String?) -> Unit = { vm.pending = it }
    when (item) {
        is Item.Choice -> ChoiceExercise(item, step, vm.pending?.toIntOrNull(), vm.result) { vm.pending = it.toString() }
        is Item.Gap -> GapExercise(item, step, !checked, onInput, onDone = vm::check)
        is Item.Transform -> TransformExercise(item, step, !checked, onInput, onDone = vm::check)
        is Item.Translate -> TranslateExercise(item, step, !checked, onInput, onDone = vm::check)
        is Item.Order -> OrderExercise(item, step, !checked, onInput)
        is Item.Match -> MatchExercise(item, step, onDone = vm::submitMatch)
        else -> Unit // never queued: the view model leaves those cards alone
    }
}
