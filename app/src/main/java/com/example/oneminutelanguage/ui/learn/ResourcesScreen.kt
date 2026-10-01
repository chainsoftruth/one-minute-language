package com.example.oneminutelanguage.ui.learn

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.course.CoursePrefs
import com.example.oneminutelanguage.course.RESOURCE_SKILLS
import com.example.oneminutelanguage.course.Resource
import com.example.oneminutelanguage.course.CourseRepository
import com.example.oneminutelanguage.course.resourcesFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    var resources by mutableStateOf<List<Resource>?>(null); private set

    init {
        viewModelScope.launch {
            // Like the dictionary, the hub is reachable before a course is picked: default to Dutch.
            resources = try {
                CourseRepository.resources(application, CoursePrefs.selectedCourse(application) ?: "nl")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList() // the screen says the list could not be loaded
            }
        }
    }
}

/** Free outside material, grouped by skill, with a level filter. A tap opens the page in the browser. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(onBack: () -> Unit, viewModel: ResourcesViewModel = viewModel()) {
    val uri = LocalUriHandler.current
    var level by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Resources") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        val all = viewModel.resources
        if (all == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        if (all.isEmpty()) {
            Text("The resources list could not be loaded.", modifier = Modifier.padding(padding).padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("A1", "A2", "B1")) { l ->
                        FilterChip(selected = level == l, onClick = { level = if (level == l) null else l }, label = { Text(l) })
                    }
                }
            }
            RESOURCE_SKILLS.forEach { (skill, heading) ->
                val group = resourcesFor(all, skill, level)
                if (group.isEmpty()) return@forEach
                item(key = "h_$skill") {
                    Text(
                        heading,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                    )
                }
                items(group, key = { it.url }) { r ->
                    ListItem(
                        modifier = Modifier.clickable { uri.openUri(r.url) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(r.title) },
                        supportingContent = {
                            Text(listOf(r.levels.joinToString(" · "), if (r.lang == "nl") "Dutch" else "English", r.note).filter { it.isNotEmpty() }.joinToString(" · "))
                        },
                        trailingContent = { Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in browser") }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
