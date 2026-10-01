package com.example.oneminutelanguage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.ui.components.ActionCard
import com.google.mlkit.nl.translate.TranslateLanguage

/** The de / het modes only make sense when the language being learned is Dutch. */
@Composable
internal fun rememberIsDutchTarget(): Boolean {
    val context = LocalContext.current
    return remember { LanguageSettingsStore.getTargetLanguage(context) == TranslateLanguage.DUTCH }
}

@Composable
fun PracticeScreen(onQuizClick: (mode: String) -> Unit) {
    val isDutch = rememberIsDutchTarget()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Practice", style = MaterialTheme.typography.headlineSmall)

        ActionCard(
            icon = Icons.Default.Quiz,
            title = "Meaning quiz",
            subtitle = "See the word, pick its meaning",
            onClick = { onQuizClick(QuizMode.MEANING.name) }
        )
        ActionCard(
            icon = Icons.Default.SwapHoriz,
            title = "Reverse quiz",
            subtitle = "See the meaning, pick the word",
            onClick = { onQuizClick(QuizMode.REVERSE.name) }
        )
        if (isDutch) {
            ActionCard(
                icon = Icons.Default.Spellcheck,
                title = "de / het quiz",
                subtitle = "Pick the article for each noun",
                onClick = { onQuizClick(QuizMode.ARTICLE.name) }
            )
        }
    }
}
