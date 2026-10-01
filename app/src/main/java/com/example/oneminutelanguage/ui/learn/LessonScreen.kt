package com.example.oneminutelanguage.ui.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.oneminutelanguage.course.Block
import com.example.oneminutelanguage.course.Item
import com.example.oneminutelanguage.course.LexEntry
import com.example.oneminutelanguage.course.display
import com.example.oneminutelanguage.course.formsLine
import com.example.oneminutelanguage.course.Source
import com.example.oneminutelanguage.speech.WordSpeaker
import com.example.oneminutelanguage.ui.components.ArticleTag
import com.example.oneminutelanguage.ui.components.FeedbackPanel
import com.example.oneminutelanguage.ui.components.ProgressRing
import com.example.oneminutelanguage.ui.components.appCardColors

@Composable
fun LessonScreen(
    onClose: () -> Unit,
    onNextLesson: (String) -> Unit,
    onBackToUnit: (String) -> Unit,
    viewModel: LessonViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        when (val phase = viewModel.phase) {
            LessonPhase.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is LessonPhase.Failed -> {
                CloseRow(title = "Lesson", progress = null, onClose = onClose)
                Text(phase.message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            }
            LessonPhase.Explain -> ExplainPhase(viewModel, onClose)
            LessonPhase.Intro -> IntroPhase(viewModel, onClose)
            LessonPhase.Dialogue -> DialoguePhase(viewModel, onClose)
            LessonPhase.Roleplay -> RoleplayPhase(viewModel, onClose)
            LessonPhase.Items -> ItemsPhase(viewModel, onClose)
            LessonPhase.Done -> DonePhase(viewModel, onClose, onNextLesson, onBackToUnit)
        }
    }
}

@Composable
internal fun CloseRow(title: String, progress: Float?, onClose: () -> Unit, trailing: String? = null) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close lesson") }
        if (progress != null) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f).height(8.dp))
        } else {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        }
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp))
        }
    }
}

// ---- Explain ----

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.ExplainPhase(viewModel: LessonViewModel, onClose: () -> Unit) {
    val lesson = viewModel.lesson ?: return
    val context = LocalContext.current
    val speak: (String) -> Unit = { WordSpeaker.speak(context, it, viewModel.ttsLocale) }

    CloseRow(title = lesson.title, progress = null, onClose = onClose)
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        lesson.explain.forEach { block -> BlockView(block, speak) }
        if (lesson.sources.isNotEmpty()) SourceChips(lesson.sources)
    }
    Button(
        onClick = viewModel::startItems,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) { Text(if (lesson.items.isEmpty()) "Finish" else "Start practice") }
}

@Composable
private fun BlockView(block: Block, speak: (String) -> Unit) {
    when (block) {
        is Block.Paragraph -> Text(markup(block.text), style = MaterialTheme.typography.bodyLarge)
        is Block.Tip -> Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Tip", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(markup(block.text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
        is Block.Example -> Row(
            modifier = Modifier.fillMaxWidth().clickable { speak(block.nl) }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(block.nl, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(block.en, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play: ${block.nl}", tint = MaterialTheme.colorScheme.primary)
        }
        is Block.Table -> Card(shape = MaterialTheme.shapes.medium, colors = appCardColors(), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row { block.head.forEach { Text(it, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f)) } }
                HorizontalDivider()
                block.rows.forEach { row ->
                    Row { row.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(end = 8.dp)) } }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SourceChips(sources: List<Source>) {
    val uri = LocalUriHandler.current
    Text("Learn more", style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        sources.forEach { source ->
            AssistChip(
                onClick = { uri.openUri(source.url) },
                label = { Text(source.title) },
                leadingIcon = { Icon(Icons.Default.OpenInBrowser, contentDescription = null) }
            )
        }
    }
}

// ---- Intro (vocab lessons) ----

/** Swipeable word cards. The word is spoken whenever a card comes into view. */
@Composable
private fun ColumnScope.IntroPhase(viewModel: LessonViewModel, onClose: () -> Unit) {
    val entries = viewModel.introEntries
    val context = LocalContext.current
    val pager = rememberPagerState(pageCount = { entries.size })
    val scope = rememberCoroutineScope()
    val speak: (LexEntry) -> Unit = { WordSpeaker.speak(context, it.display(), viewModel.ttsLocale) }

    LaunchedEffect(pager.currentPage) { entries.getOrNull(pager.currentPage)?.let(speak) }

    CloseRow(
        title = viewModel.lesson?.title.orEmpty(),
        progress = null,
        trailing = "${pager.currentPage + 1} / ${entries.size}",
        onClose = onClose
    )
    HorizontalPager(state = pager, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp), pageSpacing = 12.dp) { page ->
        WordCard(entries[page], onSpeak = { speak(entries[page]) })
    }
    val last = pager.currentPage == entries.lastIndex
    Button(
        onClick = { if (last) viewModel.startItems() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) { Text(if (last) "Start practice" else "Next word") }
    TextButton(onClick = viewModel::startItems, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Text("I know these") }
}

@Composable
private fun WordCard(entry: LexEntry, onSpeak: () -> Unit) {
    Card(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp), shape = MaterialTheme.shapes.large, colors = appCardColors()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            entry.art?.let { ArticleTag(it) }
            Text(entry.nl, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            entry.formsLine().takeIf { it.isNotEmpty() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            Text(entry.en, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
            if (entry.ex != null) {
                Spacer(Modifier.height(24.dp))
                Text(entry.ex, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                entry.exEn?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
            }
            IconButton(onClick = onSpeak, modifier = Modifier.padding(top = 16.dp)) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play: ${entry.display()}", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ---- Items ----

@Composable
private fun ColumnScope.ItemsPhase(viewModel: LessonViewModel, onClose: () -> Unit) {
    var confirmClose by remember { mutableStateOf(false) }
    val result = viewModel.result
    ResultHaptics(result)

    CloseRow(
        title = "",
        progress = if (viewModel.progressTotal == 0) 0f else viewModel.progressDone.toFloat() / viewModel.progressTotal,
        trailing = "${viewModel.progressDone} / ${viewModel.progressTotal}",
        onClose = { if (viewModel.progressDone >= 1) confirmClose = true else onClose() }
    )

    AnimatedContent(
        targetState = viewModel.step to viewModel.currentItem,
        modifier = Modifier.weight(1f),
        transitionSpec = {
            (slideInHorizontally(tween(250)) { it / 4 } + fadeIn(tween(250))) togetherWith
                (slideOutHorizontally(tween(250)) { -it / 4 } + fadeOut(tween(250)))
        },
        label = "item"
    ) { (step, item) ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (item != null) ExerciseView(item, step, viewModel)
        }
    }

    val item = viewModel.currentItem
    if (item is Item.Choice || item is Item.Gap || item is Item.Order || item is Item.Transform || item is Item.Translate || item is Item.Listen) {
        CheckBar(viewModel, item)
    } else if ((item is Item.Match || item is Item.Speak) && result != null) {
        FeedbackPanel(result, item.explain, onContinue = viewModel::continueNext)
    }

    if (confirmClose) {
        AlertDialog(
            onDismissRequest = { confirmClose = false },
            title = { Text("Leave this lesson?") },
            text = { Text("Your progress in this lesson will be lost.") },
            confirmButton = { TextButton(onClick = onClose) { Text("Leave") } },
            dismissButton = { TextButton(onClick = { confirmClose = false }) { Text("Stay") } }
        )
    }
}

@Composable
private fun ExerciseView(item: Item, step: Int, vm: LessonViewModel) {
    val checked = vm.result != null
    val onInput: (String?) -> Unit = { vm.pending = it }
    when (item) {
        is Item.Choice -> ChoiceExercise(item, step, vm.pending?.toIntOrNull(), vm.result) { vm.pending = it.toString() }
        is Item.Gap -> GapExercise(item, step, !checked, onInput, onDone = vm::check)
        is Item.Transform -> TransformExercise(item, step, !checked, onInput, onDone = vm::check)
        is Item.Translate -> TranslateExercise(item, step, !checked, onInput, onDone = vm::check)
        is Item.Order -> OrderExercise(item, step, !checked, onInput)
        is Item.Match -> MatchExercise(item, step, onDone = vm::submitMatch)
        is Item.Listen ->
            if (item.options.isEmpty()) DictationExercise(item, step, vm.ttsLocale, !checked, onInput, onDone = vm::check)
            else ListenExercise(item, step, vm.ttsLocale, vm.pending?.toIntOrNull(), vm.result) { vm.pending = it.toString() }
        is Item.Speak -> SpeakExercise(
            item, step, vm.ttsLocale, vm.speechTries, vm.speechTry, vm.result,
            onHeard = vm::submitSpeech, onSelfGrade = vm::selfGrade, onSkip = vm::skip
        )
        is Item.OpenPrompt -> PromptExercise(item, step, vm.ttsLocale, onDone = { vm.finishOpenPrompt() })
        // Writing arrives in Stage 5.
        is Item.Write -> ComingSoonExercise(onSkip = vm::skip)
    }
}

// ---- Done ----

@Composable
private fun DonePhase(viewModel: LessonViewModel, onClose: () -> Unit, onNextLesson: (String) -> Unit, onBackToUnit: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (viewModel.isDrill) "Drill complete" else "Lesson complete", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        ProgressRing(progress = viewModel.score / 100f, size = 160.dp, strokeWidth = 14.dp) {
            Text("${viewModel.score}%", style = MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (viewModel.mistakes == 0) "No mistakes. Well done!"
            else if (viewModel.isDrill) "${viewModel.mistakes} ${if (viewModel.mistakes == 1) "mistake" else "mistakes"}. Run it again to improve."
            else "${viewModel.mistakes} ${if (viewModel.mistakes == 1) "mistake" else "mistakes"} added to your review",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        viewModel.nextLesson?.let { next ->
            Button(onClick = { onNextLesson(next.id) }, modifier = Modifier.fillMaxWidth()) { Text("Next lesson: ${next.title}") }
            Spacer(Modifier.height(8.dp))
        }
        if (viewModel.isDrill) {
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Done") }
        } else {
            OutlinedButton(onClick = { onBackToUnit(viewModel.unitId) }, modifier = Modifier.fillMaxWidth()) { Text("Back to unit") }
        }
    }
}

/** The pinned bottom area: "Check" until the item is graded, then the sliding-in feedback panel. */
@Composable
private fun CheckBar(viewModel: LessonViewModel, item: Item) {
    val result = viewModel.result
    Box {
        if (result == null) {
            Button(
                onClick = viewModel::check,
                enabled = viewModel.pending != null,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Check") }
        }
        AnimatedVisibility(visible = result != null, enter = slideInVertically { it }) {
            if (result != null) FeedbackPanel(result, item.explain, onContinue = viewModel::continueNext)
        }
    }
}
