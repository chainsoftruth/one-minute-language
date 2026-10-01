package com.example.oneminutelanguage.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.data.WordEntity
import com.example.oneminutelanguage.translation.LanguageSettingsStore
import com.example.oneminutelanguage.translation.SupportedLanguages
import kotlinx.coroutines.launch

@Composable
fun DatabaseScreen(
    viewModel: DatabaseViewModel = viewModel(),
    onAddWordClick: () -> Unit = {}
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Word Database",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onSearchQueryChange,
                    label = { Text("Search") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onAddWordClick) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add word")
                }
            }

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

            Spacer(modifier = Modifier.height(4.dp))

            if (words.isEmpty()) {
                Text(
                    text = if (query.isBlank()) "No words yet. Tap + to add one." else "No matches for \"$query\".",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                        HorizontalDivider()
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = word.language2Word,
                style = MaterialTheme.typography.titleMedium,
                color = if (word.isEnabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Text(
                text = word.language1Word,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
