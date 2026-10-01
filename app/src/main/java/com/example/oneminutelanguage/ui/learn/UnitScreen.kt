package com.example.oneminutelanguage.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.course.LessonKind
import com.example.oneminutelanguage.ui.components.ActionCard

internal fun lessonKindIcon(kind: LessonKind): ImageVector = when (kind) {
    LessonKind.GRAMMAR -> Icons.AutoMirrored.Filled.MenuBook
    LessonKind.VOCAB -> Icons.Default.Translate
    LessonKind.READING -> Icons.Default.AutoStories
    LessonKind.LISTENING -> Icons.Default.Headphones
    LessonKind.SPEAKING -> Icons.Default.Mic
    LessonKind.WRITING -> Icons.Default.Edit
    LessonKind.TEST -> Icons.Default.Flag
}

private fun LessonKind.label() = name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
fun UnitScreen(onBack: () -> Unit, onLessonClick: (String) -> Unit, viewModel: UnitViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp)
    ) {
        when (val state = viewModel.state.collectAsState().value) {
            UnitState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            UnitState.Missing -> {
                BackRow("Unit", onBack)
                Text("This unit isn't available yet.", style = MaterialTheme.typography.bodyLarge)
            }
            is UnitState.Ready -> {
                BackRow(state.unit.title, onBack)
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.unit.subtitle.isNotEmpty()) {
                        Text(
                            state.unit.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    state.unit.lessons.forEach { lesson ->
                        val best = state.progress[lesson.id]?.bestScore
                        ActionCard(
                            icon = lessonKindIcon(lesson.kind),
                            title = lesson.title,
                            subtitle = lesson.kind.label(),
                            badge = best?.let { "✓ $it%" },
                            onClick = { onLessonClick(lesson.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackRow(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}
