package com.example.oneminutelanguage.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneminutelanguage.course.CourseInfo
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.CourseRepository
import com.example.oneminutelanguage.ui.components.appCardColors

/** "What do you want to learn?" One card per course in `assets/courses/index.json`. [onBack] null = shown inside the Learn tab. */
@Composable
fun CourseSelectScreen(onSelected: () -> Unit, onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    var courses by remember { mutableStateOf<List<CourseInfo>?>(null) }
    LaunchedEffect(Unit) { courses = CourseRepository.courses(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(if (onBack == null) WindowInsets.statusBars else WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            }
            Text("What do you want to learn?", style = MaterialTheme.typography.headlineSmall)
        }
        courses?.forEach { course ->
            Card(
                onClick = {
                    CoursePrefs.setSelectedCourse(context, course.id)
                    onSelected()
                },
                enabled = course.available,
                modifier = Modifier.fillMaxWidth().alpha(if (course.available) 1f else 0.5f),
                shape = MaterialTheme.shapes.medium,
                colors = appCardColors()
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(course.flag, fontSize = 48.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(course.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            course.nativeName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (course.available) "${course.levels.first()} → ${course.levels.last()}" else "Coming soon",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
