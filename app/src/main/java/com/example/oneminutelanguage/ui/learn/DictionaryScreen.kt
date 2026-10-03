package com.example.oneminutelanguage.ui.learn

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.course.LexEntry
import com.example.oneminutelanguage.course.display
import com.example.oneminutelanguage.course.formsLine
import com.example.oneminutelanguage.course.hasSingleArticle
import com.example.oneminutelanguage.course.searchLexicon
import com.example.oneminutelanguage.course.sendToWidgetMessage
import com.example.oneminutelanguage.speech.WordSpeaker
import com.example.oneminutelanguage.ui.components.ArticleTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(onBack: () -> Unit, viewModel: DictionaryViewModel = viewModel()) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<LexEntry?>(null) }
    // Searching ~4,000 entries is too slow for the main thread. A new keystroke cancels the previous search, which also debounces typing.
    var results by remember { mutableStateOf<List<LexEntry>?>(null) }
    LaunchedEffect(viewModel.entries, viewModel.query, viewModel.level, viewModel.topic) {
        if (viewModel.query.isNotEmpty()) delay(150)
        results = withContext(Dispatchers.Default) {
            searchLexicon(viewModel.entries, viewModel.query, viewModel.level, viewModel.topic)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Dictionary") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = { viewModel.query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search Dutch or English, any form") },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
            FilterRow {
                items(listOf("A1", "A2", "B1").filter { l -> viewModel.entries.any { it.lvl == l } }) { l ->
                    FilterChip(selected = viewModel.level == l, onClick = { viewModel.level = if (viewModel.level == l) null else l }, label = { Text(l) })
                }
                items(viewModel.topics) { t ->
                    FilterChip(selected = viewModel.topic == t.id, onClick = { viewModel.topic = if (viewModel.topic == t.id) null else t.id }, label = { Text(t.title) })
                }
            }
            val shown = results
            when {
                viewModel.failed -> Text("Something's wrong with the course files, so the dictionary can't load.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                viewModel.loading || shown == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                shown.isEmpty() -> Text("No words found.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(shown, key = { it.id }) { entry ->
                        ListItem(
                            modifier = Modifier.clickable { selected = entry },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = if (entry.hasSingleArticle()) { { ArticleTag(entry.art.orEmpty()) } } else null,
                            headlineContent = { Text(entry.nl, style = MaterialTheme.typography.titleMedium) },
                            supportingContent = { Text(entry.en) },
                            trailingContent = { LevelChip(entry.lvl) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    selected?.let { entry ->
        WordSheet(
            entry = entry,
            ttsLocale = viewModel.ttsLocale,
            canAdd = viewModel.canSend,
            onAdd = {
                scope.launch {
                    val (added, already) = viewModel.addToWords(entry)
                    snackbar.showSnackbar(sendToWidgetMessage(added, already))
                }
            },
            onDismiss = { selected = null }
        )
    }
}

@Composable
private fun FilterRow(content: LazyListScope.() -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun LevelChip(level: String) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            level,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordSheet(entry: LexEntry, ttsLocale: String, canAdd: Boolean, onAdd: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { WordDetails(entry, ttsLocale, canAdd, onAdd) }
    }
}

/** Everything about one word: forms, example, speaker, the woordenlijst link and "Add to my words". Shared with the reading view. */
@Composable
internal fun ColumnScope.WordDetails(entry: LexEntry, ttsLocale: String, canAdd: Boolean, onAdd: () -> Unit) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            if (entry.hasSingleArticle()) ArticleTag(entry.art.orEmpty())
            Text(entry.nl, style = MaterialTheme.typography.displaySmall)
        }
        IconButton(onClick = { WordSpeaker.speak(context, entry.display(), ttsLocale) }) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play: ${entry.display()}", tint = MaterialTheme.colorScheme.primary)
        }
    }
    Text(entry.en, style = MaterialTheme.typography.titleLarge)
    Text(
        "${entry.pos} · ${entry.lvl}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    entry.formsLine().takeIf { it.isNotEmpty() }?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
    entry.prep?.let { Text("Used with: $it", style = MaterialTheme.typography.bodyMedium) }
    if (entry.ex != null) {
        Text(entry.ex, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        entry.exEn?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    WoordenlijstButton(entry.nl)
    if (canAdd) Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Add to my words") }
}

@Composable
internal fun WoordenlijstButton(word: String) {
    val uri = LocalUriHandler.current
    OutlinedButton(
        onClick = { uri.openUri("https://woordenlijst.org/zoeken/?q=${Uri.encode(word)}") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
        Text("Check on woordenlijst.org", modifier = Modifier.padding(start = 8.dp))
    }
}
