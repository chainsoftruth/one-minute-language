package com.example.oneminutelanguage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.ui.components.ActionCard
import com.example.oneminutelanguage.ui.components.StatTile

@Composable
fun TodayScreen(
    onAddWordClick: () -> Unit,
    onSettingsClick: () -> Unit,
    /** null = the quiz setup screen without a preselected mode. */
    onQuizClick: (mode: String?) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val totalWords by viewModel.totalWordsCount.collectAsState(initial = 0)
    val viewsToday by viewModel.todayViewCount.collectAsState(initial = 0)
    val isDutch = rememberIsDutchTarget()

    val greeting = remember {
        when (java.time.LocalTime.now().hour) {
            in 5..11 -> "Good morning! ☀️"
            in 12..17 -> "Good afternoon! 👋"
            in 18..22 -> "Good evening! 🌙"
            else -> "Burning the midnight oil? 🦉"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        // Stage 2 adds the "Continue learning" card and the daily-goal ring here.

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                value = viewsToday.toString(),
                label = "words shown today",
                icon = Icons.Default.Visibility,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = totalWords.toString(),
                label = "words in your collection",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Quick actions", style = MaterialTheme.typography.titleMedium)

        ActionCard(
            icon = Icons.Default.Add,
            title = "Add a word",
            subtitle = "Translate and save it to your list",
            onClick = onAddWordClick
        )
        ActionCard(
            icon = Icons.Default.Quiz,
            title = "Quick quiz",
            subtitle = "Check what you remember",
            onClick = { onQuizClick(null) }
        )
        if (isDutch) {
            ActionCard(
                icon = Icons.Default.Spellcheck,
                title = "de / het drill",
                subtitle = "Pick the article for each noun",
                onClick = { onQuizClick(QuizMode.ARTICLE.name) }
            )
        }
    }
}
