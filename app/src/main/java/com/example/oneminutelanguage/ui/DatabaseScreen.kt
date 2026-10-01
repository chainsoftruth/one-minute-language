package com.example.oneminutelanguage.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.data.WordEntity
import com.example.oneminutelanguage.ui.theme.appColors
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.translation.SupportedLanguages
import com.example.oneminutelanguage.ui.components.ArticleTag
import kotlinx.coroutines.launch

/** The Words tab. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen(
    viewModel: DatabaseViewModel = viewModel(),
    onAddWordClick: () -> Unit = {},
    onDictionaryClick: () -> Unit = {}
) {
    val query by viewModel.searchQuery.collectAsState()
    val words by viewModel.words.collectAsState()

    var pendingBulkEnable by remember { mutableStateOf<Boolean?>(null) }
    var editingWord by remember { mutableStateOf<WordEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    editingWord?.let { word ->
        EditWordDialog(
            word = word,
            onDismiss = { editingWord = null },
            onSave = {
                viewModel.updateWord(it)
                editingWord = null
            }
        )
    }

    pendingBulkEnable?.let { enable ->
        AlertDialog(
            onDismissRequest = { pendingBulkEnable = null },
            title = { Text(if (enable) "Enable all words?" else "Disable all words?") },
            text = {
                Text(
                    if (enable) {
                        "All words will be shown on the widget."
                    } else {
                        "No words will be shown on the widget until you enable some again."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setAllWordsEnabled(enable)
                        pendingBulkEnable = null
                    }
                ) {
                    Text(if (enable) "Enable all" else "Disable all")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingBulkEnable = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        // The app scaffold already pads for the bottom bar.
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeTopAppBar(
                title = { Text("My words") },
                actions = {
                    IconButton(onClick = onDictionaryClick) { Icon(Icons.Default.Book, contentDescription = "Dictionary") }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    // The page gradient's top colour: opaque, but no visible band against the background.
                    scrolledContainerColor = MaterialTheme.appColors.page.first()
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddWordClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add word")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { pendingBulkEnable = true }) {
                    Text("Select all")
                }

                TextButton(onClick = { pendingBulkEnable = false }) {
                    Text("Deselect all")
                }
            }

            if (words.isEmpty()) {
                Text(
                    text = if (query.isBlank()) "No words yet. Tap + to add one." else "No matches for \"$query\".",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                // Bottom padding keeps the last row clear of the FAB.
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(words, key = { it.id }) { word ->
                        WordRow(
                            word = word,
                            onClick = { editingWord = word },
                            onEnabledChange = { viewModel.setWordEnabled(word, it) },
                            onDelete = {
                                viewModel.deleteWord(word)
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Deleted \"${word.language2Word}\"",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreWord(word)
                                    }
                                }
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditWordDialog(
    word: WordEntity,
    onDismiss: () -> Unit,
    onSave: (WordEntity) -> Unit
) {
    val context = LocalContext.current
    var learningWord by remember(word) { mutableStateOf(word.language2Word) }
    var nativeWord by remember(word) { mutableStateOf(word.language1Word) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit word") },
        text = {
            Column {
                OutlinedTextField(
                    value = learningWord,
                    onValueChange = { learningWord = it },
                    label = { Text(SupportedLanguages.displayNameFor(LanguageSettingsStore.getTargetLanguage(context))) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nativeWord,
                    onValueChange = { nativeWord = it },
                    label = { Text(SupportedLanguages.displayNameFor(LanguageSettingsStore.getSourceLanguage(context))) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(word.copy(language1Word = nativeWord.trim(), language2Word = learningWord.trim()))
                },
                enabled = learningWord.isNotBlank() && nativeWord.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WordRow(
    word: WordEntity,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val article = articleOf(word.language2Word)
    val headline = if (article != null) word.language2Word.drop(article.length).trim() else word.language2Word

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = article?.let { { ArticleTag(it) } },
        headlineContent = {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                color = if (word.isEnabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        supportingContent = {
            Text(
                text = word.language1Word,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = word.isEnabled,
                    onCheckedChange = onEnabledChange
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete word",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}
