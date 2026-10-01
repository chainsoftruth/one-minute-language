package com.example.oneminutelanguage.ui

import android.os.Build
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.ui.components.AnswerOption
import com.example.oneminutelanguage.ui.components.AnswerState
import com.example.oneminutelanguage.ui.components.ProgressRing
import com.example.oneminutelanguage.ui.theme.appColors

@Composable
fun QuizScreen(
    viewModel: QuizViewModel = viewModel(),
    onDone: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDone) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Quiz", style = MaterialTheme.typography.headlineSmall)
        }

        val phase = viewModel.phase
        if (phase is QuizPhase.Running) {
            LinearProgressIndicator(
                progress = { (viewModel.currentIndex + 1f) / viewModel.questions.size },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${viewModel.currentIndex + 1} / ${viewModel.questions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (phase) {
                is QuizPhase.NotEnoughWords -> {
                    Text(
                        text = "You need at least ${QuizViewModel.MIN_WORDS} enabled words to start a quiz.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onDone) { Text("Back") }
                }

                is QuizPhase.Setup -> SetupPhase(phase = phase, viewModel = viewModel)

                is QuizPhase.Running -> RunningPhase(viewModel = viewModel)

                is QuizPhase.Finished -> FinishedPhase(viewModel = viewModel, onDone = onDone)
            }
        }
    }
}

@Composable
private fun SetupPhase(phase: QuizPhase.Setup, viewModel: QuizViewModel) {
    val modes = buildList {
        add(QuizMode.MEANING to "Meaning")
        add(QuizMode.REVERSE to "Reverse")
        if (phase.articleWords != null) add(QuizMode.ARTICLE to "de / het")
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        modes.forEach { (mode, label) ->
            FilterChip(
                selected = viewModel.mode == mode,
                onClick = { viewModel.selectMode(mode) },
                label = { Text(label) }
            )
        }
    }
    Text(
        text = when (viewModel.mode) {
            QuizMode.MEANING -> "See the word, pick its meaning"
            QuizMode.REVERSE -> "See the meaning, pick the word"
            QuizMode.ARTICLE -> "Pick the article for each noun"
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    val availableWords = if (viewModel.mode == QuizMode.ARTICLE) phase.articleWords ?: 0 else phase.availableWords

    Text(
        text = "How many words to check?",
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = "$availableWords words available",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    listOf(15, 30, 60).forEach { count ->
        Button(
            onClick = { viewModel.startQuiz(count) },
            enabled = availableWords >= count,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Text("$count words")
        }
    }

    Button(
        onClick = { viewModel.startQuiz(null) },
        enabled = availableWords > 0,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text("All ($availableWords words)")
    }
}

@Composable
private fun RunningPhase(viewModel: QuizViewModel) {
    val selected = viewModel.selectedOption
    val haptics = LocalHapticFeedback.current

    // Reverse and de / het speak the answer after it's given (see QuizViewModel.selectAnswer).
    val speaksPrompt = viewModel.mode == QuizMode.MEANING
    LaunchedEffect(viewModel.currentIndex) {
        if (speaksPrompt) viewModel.speakCurrentWord()
    }

    LaunchedEffect(selected) {
        val question = viewModel.questions[viewModel.currentIndex]
        when (selected) {
            null, QuizViewModel.DONT_KNOW -> Unit
            question.correctIndex ->
                haptics.performHapticFeedback(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackType.Confirm else HapticFeedbackType.LongPress)
            else ->
                haptics.performHapticFeedback(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackType.Reject else HapticFeedbackType.LongPress)
        }
    }

    AnimatedContent(
        targetState = viewModel.currentIndex,
        transitionSpec = {
            (slideInHorizontally(tween(250)) { it / 4 } + fadeIn(tween(250))) togetherWith
                (slideOutHorizontally(tween(250)) { -it / 4 } + fadeOut(tween(250)))
        },
        label = "question"
    ) { index ->
        val question = viewModel.questions[index]
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = question.prompt,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = viewModel::speakCurrentWord,
                        enabled = speaksPrompt || selected != null
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repeat audio")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            question.options.forEachIndexed { optionIndex, option ->
                AnswerOption(
                    text = option,
                    state = when {
                        selected == null -> AnswerState.IDLE
                        optionIndex == question.correctIndex -> AnswerState.CORRECT
                        optionIndex == selected -> AnswerState.WRONG
                        else -> AnswerState.DIMMED
                    },
                    onClick = { viewModel.selectAnswer(optionIndex) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
        onClick = { viewModel.selectAnswer(QuizViewModel.DONT_KNOW) },
        enabled = selected == null,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.appColors.warning,
            disabledContentColor = MaterialTheme.appColors.warning.copy(alpha = 0.38f)
        )
    ) {
        Text("I don't know")
    }

    AnimatedVisibility(
        visible = selected != null,
        enter = slideInVertically { it / 2 } + fadeIn()
    ) {
        val question = viewModel.questions[viewModel.currentIndex]
        val appColors = MaterialTheme.appColors
        val (icon, color, message) = when {
            selected == QuizViewModel.DONT_KNOW -> Triple(Icons.Default.Info, appColors.warning, "No worries — remember it for next time")
            selected == question.correctIndex -> Triple(Icons.Default.Check, appColors.success, "Correct!")
            else -> Triple(Icons.Default.Close, MaterialTheme.colorScheme.error, "Wrong")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(8.dp))
            FeedbackRow(icon, color, message)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = viewModel::nextQuestion,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (viewModel.currentIndex + 1 >= viewModel.questions.size) "Finish" else "Next")
            }
        }
    }
}

@Composable
private fun FeedbackRow(icon: ImageVector, color: Color, message: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = color)
        Text(text = message, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun FinishedPhase(viewModel: QuizViewModel, onDone: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProgressRing(
            progress = viewModel.score.toFloat() / viewModel.questions.size,
            size = 168.dp,
            strokeWidth = 14.dp,
            color = MaterialTheme.appColors.success
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${viewModel.score} / ${viewModel.questions.size}", style = MaterialTheme.typography.displaySmall)
                Text(
                    text = "correct",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (viewModel.dontKnowCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Skipped (I don't know): ${viewModel.dontKnowCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.appColors.warning
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.correctWordIds.isNotEmpty()) {
            if (viewModel.correctWordsDisabled) {
                Text(
                    text = "${viewModel.correctWordIds.size} words removed from the widget rotation.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "You answered ${viewModel.correctWordIds.size} words correctly. " +
                        "Remove them from the widget rotation?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = viewModel::disableCorrectWords,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Deselect ${viewModel.correctWordIds.size} correct words")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = onDone,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}
