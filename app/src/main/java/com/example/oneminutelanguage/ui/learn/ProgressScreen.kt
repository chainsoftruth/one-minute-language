package com.example.oneminutelanguage.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.course.CAN_DO
import com.example.oneminutelanguage.course.CAN_DO_SKILLS
import com.example.oneminutelanguage.course.SKILL_KINDS
import com.example.oneminutelanguage.course.activityGrid
import com.example.oneminutelanguage.course.activityStep
import com.example.oneminutelanguage.course.levelCounts
import com.example.oneminutelanguage.ui.components.ActionCard
import com.example.oneminutelanguage.ui.components.StatTile
import com.example.oneminutelanguage.ui.components.appCardColors
import com.example.oneminutelanguage.ui.theme.appColors
import java.time.LocalDate

@Composable
fun ProgressScreen(onBack: () -> Unit, onUnitClick: (String) -> Unit, onPlacementClick: () -> Unit, viewModel: ProgressViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Your progress", style = MaterialTheme.typography.headlineSmall)
        }
        when (val state = viewModel.state.collectAsState().value) {
            ProgressState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            ProgressState.NoCourse -> Text(
                "Pick a course in the Learn tab first. Your progress will show up here.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            is ProgressState.Failed -> Text(state.message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            is ProgressState.Ready -> Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val data = state.data
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(data.currentStreak.toString(), "day streak", Icons.Default.LocalFireDepartment, Modifier.weight(1f), MaterialTheme.appColors.coral)
                    StatTile(data.longestStreak.toString(), "longest streak", Icons.Default.EmojiEvents, Modifier.weight(1f), MaterialTheme.appColors.amber)
                }

                Section("Levels") {
                    data.path.forEach { level ->
                        val (done, total) = levelCounts(level, data.completed)
                        val skipped = level.units.mapNotNull { it.unit }.flatMap { it.lessons }.count { it.id in data.skipped }
                        Meter(
                            label = "${level.outline.level} · ${level.outline.title}",
                            value = if (total == 0) 0f else done.toFloat() / total,
                            text = "$done / $total" + if (skipped > 0) " ($skipped skipped)" else ""
                        )
                    }
                }

                Section("Skills") {
                    SKILL_KINDS.forEach { kind ->
                        val score = data.skills[kind]
                        Meter(
                            label = kind.name.lowercase().replaceFirstChar { it.uppercase() },
                            value = (score ?: 0) / 100f,
                            text = score?.let { "$it%" } ?: "Not started"
                        )
                    }
                    Text(
                        "Average best score of the lessons you finished.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Section("Words") {
                    Meter(
                        label = "Mastered",
                        value = if (data.lexiconSize == 0) 0f else data.mastered.toFloat() / data.lexiconSize,
                        text = "${data.mastered} of ${data.lexiconSize}"
                    )
                    Text(
                        "A word counts as mastered when you can leave it for three weeks between reviews.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Section("Last 12 weeks") { ActivityGrid(data.exercises) }

                Section("B1 can-do list") { CanDoList(viewModel, data, onUnitClick) }

                ActionCard(
                    icon = Icons.Default.Quiz,
                    title = "Placement test",
                    subtitle = "30 questions, about 10 minutes",
                    onClick = onPlacementClick
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = appCardColors()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

/** A label, a bar and the number in words, so the value never depends on colour or the bar alone. */
@Composable
private fun Meter(label: String, value: Float, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(progress = { value.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp))
    }
}

/** 12 weeks as 12 columns of 7 squares (Monday on top); the darker, the more exercises that day. */
@Composable
private fun ActivityGrid(exercises: Map<LocalDate, Int>) {
    val grid = activityGrid(LocalDate.now())
    val activeDays = exercises.values.count { it > 0 }
    val primary = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    val alphas = listOf(0.15f, 0.4f, 0.7f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = "Activity grid: $activeDays active days in the last 12 weeks" },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            grid.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { day ->
                        val step = if (day == null) 0 else activityStep(exercises[day] ?: 0)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    when {
                                        day == null -> androidx.compose.ui.graphics.Color.Transparent
                                        step == 0 -> empty
                                        else -> primary.copy(alpha = alphas[step - 1])
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }
        }
        Text(
            "$activeDays active days. Darker = more exercises.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CanDoList(viewModel: ProgressViewModel, data: ProgressData, onUnitClick: (String) -> Unit) {
    val units = data.path.flatMap { it.units }.mapNotNull { it.unit }.associateBy { it.id }
    val ticked = viewModel.canDo
    Text(
        "${CAN_DO.count { it.id in ticked }} of ${CAN_DO.size} ticked. Tick what you can do now; the chips open the units that train it.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    CAN_DO_SKILLS.forEach { skill ->
        Text(skill, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp))
        CAN_DO.filter { it.skill == skill }.forEach { item ->
            val on = item.id in ticked
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .toggleable(value = on, role = Role.Checkbox, onValueChange = { viewModel.toggleCanDo(item.id, it) }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = on, onCheckedChange = null)
                    Text(item.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 36.dp)) {
                    item.units.mapNotNull { units[it] }.forEach { unit ->
                        AssistChip(onClick = { onUnitClick(unit.id) }, label = { Text("${unit.id} ${unit.title}") })
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}
