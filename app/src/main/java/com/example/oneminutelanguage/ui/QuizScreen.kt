package com.example.oneminutelanguage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private val CorrectGreen = Color(0xFF2E7D32)
private val CorrectGreenContainer = Color(0xFFA5D6A7)
private val WrongRedContainer = Color(0xFFEF9A9A)
private val NeutralAmber = Color(0xFF8D6E63)
private val NeutralAmberContainer = Color(0xFFFFE082)

@Composable
fun QuizScreen(
    viewModel: QuizViewModel = viewModel(),
    onDone: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Check Progress",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (val phase = viewModel.phase) {
            is QuizPhase.NotEnoughWords -> {
                Text(
                    text = "You need at least ${QuizViewModel.MIN_WORDS} enabled words to start a quiz.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDone) { Text("Back") }
            }

            is QuizPhase.Setup -> SetupPhase(availableWords = phase.availableWords, viewModel = viewModel)

            is QuizPhase.Running -> RunningPhase(viewModel = viewModel)

            is QuizPhase.Finished -> FinishedPhase(viewModel = viewModel, onDone = onDone)
        }
    }
}

@Composable
private fun SetupPhase(availableWords: Int, viewModel: QuizViewModel) {
    Text(
        text = "How many words to check?",
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = "$availableWords words available",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    listOf(15, 30, 60).forEach { count ->
        Button(
            onClick = { viewModel.startQuiz(count) },
            enabled = availableWords >= count,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 6.dp)
        ) {
            Text("$count words")
        }
    }

    Button(
        onClick = { viewModel.startQuiz(null) },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 6.dp)
    ) {
        Text("All ($availableWords words)")
    }
}

@Composable
private fun RunningPhase(viewModel: QuizViewModel) {
    val question = viewModel.questions[viewModel.currentIndex]
    val selected = viewModel.selectedOption

    LaunchedEffect(viewModel.currentIndex) {
        viewModel.speakCurrentWord()
    }

    Text(
        text = "${viewModel.currentIndex + 1} / ${viewModel.questions.size}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = question.prompt,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = viewModel::speakCurrentWord) {
        Text("🔊 Repeat")
    }

    Spacer(modifier = Modifier.height(16.dp))

    question.options.forEachIndexed { index, option ->
        val containerColor = when {
            selected == null -> MaterialTheme.colorScheme.primary
            index == question.correctIndex -> CorrectGreenContainer
            index == selected -> WrongRedContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        val contentColor = when {
            selected == null -> MaterialTheme.colorScheme.onPrimary
            index == question.correctIndex || index == selected -> Color.Black
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Button(
            onClick = { viewModel.selectAnswer(index) },
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor,
                disabledContentColor = contentColor
            ),
            enabled = selected == null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(option, textAlign = TextAlign.Center)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    val dontKnowSelected = selected == QuizViewModel.DONT_KNOW
    Button(
        onClick = { viewModel.selectAnswer(QuizViewModel.DONT_KNOW) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dontKnowSelected) NeutralAmberContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (dontKnowSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = if (dontKnowSelected) NeutralAmberContainer else MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = if (dontKnowSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = selected == null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text("I don't know")
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (selected != null) {
        Text(
            text = when {
                selected == QuizViewModel.DONT_KNOW -> "No worries — remember it for next time"
                selected == question.correctIndex -> "Correct!"
                else -> "Wrong"
            },
            style = MaterialTheme.typography.titleMedium,
            color = when {
                selected == QuizViewModel.DONT_KNOW -> NeutralAmber
                selected == question.correctIndex -> CorrectGreen
                else -> MaterialTheme.colorScheme.error
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = viewModel::nextQuestion,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                if (viewModel.currentIndex + 1 >= viewModel.questions.size) "Finish" else "Next"
            )
        }
    }
}

@Composable
private fun FinishedPhase(viewModel: QuizViewModel, onDone: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Score: ${viewModel.score} / ${viewModel.questions.size}",
            style = MaterialTheme.typography.headlineMedium
        )

        if (viewModel.dontKnowCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Skipped (I don't know): ${viewModel.dontKnowCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralAmber
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
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Deselect ${viewModel.correctWordIds.size} correct words")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Done")
        }
    }
}
