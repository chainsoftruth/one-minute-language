package com.example.oneminutelanguage.ui.learn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.course.PathLevel
import com.example.oneminutelanguage.course.PathUnit
import com.example.oneminutelanguage.course.UnitKind
import com.example.oneminutelanguage.course.firstUnfinished
import com.example.oneminutelanguage.ui.components.ProgressRing
import com.example.oneminutelanguage.ui.components.appCardColors

internal fun unitKindIcon(kind: UnitKind): ImageVector = when (kind) {
    UnitKind.GRAMMAR -> Icons.AutoMirrored.Filled.MenuBook
    UnitKind.THEME -> Icons.Default.Forum
    UnitKind.CHECKPOINT -> Icons.Default.Flag
}

/** Units that aren't written yet still need an icon; the id says what they will be (a1.g04, a1.t01, a1.cp). */
private fun kindFromId(id: String) = when {
    id.contains(".g") -> UnitKind.GRAMMAR
    id.contains(".t") -> UnitKind.THEME
    else -> UnitKind.CHECKPOINT
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LearnScreen(
    onChooseCourse: () -> Unit,
    onUnitClick: (String) -> Unit,
    viewModel: LearnViewModel = viewModel()
) {
    when (val state = viewModel.state.collectAsState().value) {
        LearnState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        // Nothing selected yet: the selector is the Learn tab. Picking a course flips the state to Ready.
        LearnState.NoCourse -> CourseSelectScreen(onSelected = {})
        is LearnState.Ready -> {
            val completed = state.progress.keys
            val nextUnitId = firstUnfinished(state.path, completed)?.first?.id
            LazyColumn(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Learn", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                        AssistChip(
                            onClick = onChooseCourse,
                            label = { Text("${state.course.flag} ${state.course.name} ▾") }
                        )
                    }
                }
                state.path.forEach { level ->
                    stickyHeader { LevelHeader(level, completed) }
                    items(level.units, key = { it.id }) { entry ->
                        UnitCard(
                            entry = entry,
                            completed = completed,
                            isNext = entry.id == nextUnitId,
                            onClick = { onUnitClick(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelHeader(level: PathLevel, completed: Set<String>) {
    val lessons = level.units.mapNotNull { it.unit }.flatMap { it.lessons }
    Surface(color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${level.outline.level} · ${level.outline.title}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${lessons.count { it.id in completed }}/${lessons.size} lessons",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UnitCard(entry: PathUnit, completed: Set<String>, isNext: Boolean, onClick: () -> Unit) {
    val unit = entry.unit
    Card(
        onClick = onClick,
        enabled = unit != null,
        modifier = Modifier.fillMaxWidth().alpha(if (unit != null) 1f else 0.55f),
        shape = MaterialTheme.shapes.medium,
        colors = appCardColors()
    ) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = 72.dp).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val done = unit?.lessons?.count { it.id in completed } ?: 0
            val total = unit?.lessons?.size ?: 0
            ProgressRing(
                progress = if (total == 0) 0f else done.toFloat() / total,
                size = 48.dp,
                strokeWidth = 5.dp
            ) {
                Icon(
                    unitKindIcon(unit?.kind ?: kindFromId(entry.id)),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(unit?.title ?: entry.id, style = MaterialTheme.typography.titleMedium)
                Text(
                    when {
                        unit == null -> "Coming soon"
                        unit.subtitle.isNotEmpty() -> unit.subtitle
                        else -> "$done / $total lessons"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isNext) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        "Next",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
