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
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.material3.Card
import androidx.compose.ui.platform.LocalContext
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.firstUnfinished
import com.example.oneminutelanguage.ui.components.ActionCard
import com.example.oneminutelanguage.ui.components.HeroCard
import com.example.oneminutelanguage.ui.components.ProgressRing
import com.example.oneminutelanguage.ui.components.appCardColors
import com.example.oneminutelanguage.ui.learn.LearnState
import com.example.oneminutelanguage.ui.components.StatTile
import com.example.oneminutelanguage.ui.theme.appColors
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Surface

@Composable
fun TodayScreen(
    onAddWordClick: () -> Unit,
    onSettingsClick: () -> Unit,
    /** null = the quiz setup screen without a preselected mode. */
    onQuizClick: (mode: String?) -> Unit,
    onChooseCourse: () -> Unit,
    onLessonClick: (lessonId: String) -> Unit,
    onLearnClick: () -> Unit,
    onReviewClick: () -> Unit,
    onProgressClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val totalWords by viewModel.totalWordsCount.collectAsState(initial = 0)
    val viewsToday by viewModel.todayViewCount.collectAsState(initial = 0)
    val isDutch = rememberIsDutchTarget()
    val learnState by viewModel.learnState.collectAsState()
    val dueReviews by viewModel.dueCount.collectAsState()
    val (exercisesDone, streakDays) = viewModel.exercisesAndStreak.collectAsState().value
    val dailyGoal = remember { CoursePrefs.dailyGoal(context) }

    val greeting = remember {
        when (java.time.LocalTime.now().hour) {
            in 5..11 -> "Good morning! ☀️"
            in 12..17 -> "Good afternoon! 👋"
            in 18..22 -> "Good evening! 🌙"
            else -> "Burning the midnight oil? 🦉"
        }
    }
    val today = remember {
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE d MMMM"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = today,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = greeting, style = MaterialTheme.typography.headlineSmall)
            }
            if (streakDays > 0) {
                Surface(shape = CircleShape, color = MaterialTheme.appColors.coral.container) {
                    Text(
                        "🔥 $streakDays",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.appColors.coral.color
                    )
                }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        ContinueSection(
            learnState = learnState,
            exercisesDone = exercisesDone,
            dailyGoal = dailyGoal,
            streakDays = streakDays,
            onChooseCourse = onChooseCourse,
            onLessonClick = onLessonClick,
            onLearnClick = onLearnClick,
            onProgressClick = onProgressClick
        )

        if (dueReviews > 0) {
            ActionCard(
                icon = Icons.Default.Replay,
                title = if (dueReviews == 1) "1 review due" else "$dueReviews reviews due",
                subtitle = "Keep your words fresh",
                onClick = onReviewClick,
                accent = MaterialTheme.appColors.coral
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                value = viewsToday.toString(),
                label = "words shown today",
                icon = Icons.Default.Visibility,
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.appColors.teal
            )
            StatTile(
                value = totalWords.toString(),
                label = "words in your collection",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.appColors.violet
            )
        }

        Text("Quick actions", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))

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
            onClick = { onQuizClick(null) },
            accent = MaterialTheme.appColors.violet
        )
        if (isDutch) {
            ActionCard(
                icon = Icons.Default.Spellcheck,
                title = "de / het drill",
                subtitle = "Pick the article for each noun",
                onClick = { onQuizClick(QuizMode.ARTICLE.name) },
                accent = MaterialTheme.appColors.amber
            )
        }
    }
}

/** "Continue learning" hero card and the daily goal with its streak. */
@Composable
private fun ContinueSection(
    learnState: LearnState,
    exercisesDone: Int,
    dailyGoal: Int,
    streakDays: Int,
    onChooseCourse: () -> Unit,
    onLessonClick: (String) -> Unit,
    onLearnClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    when (learnState) {
        LearnState.Loading -> Unit
        is LearnState.Failed -> Unit // the Learn tab explains what is wrong
        LearnState.NoCourse -> HeroCard(
            title = "Deep learning",
            subtitle = "Choose a language",
            onClick = onChooseCourse
        )
        is LearnState.Ready -> {
            val next = firstUnfinished(learnState.path, learnState.progress.keys)
            if (next == null) {
                HeroCard(title = "Continue learning", subtitle = "All available lessons done", onClick = onLearnClick, progress = 1f)
            } else {
                val (unit, lesson) = next
                val levelLessons = learnState.path.first { level -> level.units.any { it.id == unit.id } }
                    .units.mapNotNull { it.unit }.flatMap { it.lessons }
                HeroCard(
                    title = "Continue learning",
                    subtitle = "${unit.title} · ${lesson.title}",
                    onClick = { onLessonClick(lesson.id) },
                    progress = levelLessons.count { it.id in learnState.progress }.toFloat() / levelLessons.size
                )
            }
        }
    }

    // The goal card opens the Progress screen.
    Card(onClick = onProgressClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = appCardColors()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val goalMet = exercisesDone >= dailyGoal
            ProgressRing(
                progress = exercisesDone.toFloat() / dailyGoal,
                size = 80.dp,
                strokeWidth = 9.dp,
                color = if (goalMet) MaterialTheme.appColors.success else MaterialTheme.appColors.coral.color
            ) {
                Text("$exercisesDone", style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(if (goalMet) "Goal reached ✓" else "Daily goal", style = MaterialTheme.typography.titleMedium)
                Text(
                    "$exercisesDone / $dailyGoal exercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "🔥 $streakDays ${if (streakDays == 1) "day" else "days"} streak",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.appColors.coral.color,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
