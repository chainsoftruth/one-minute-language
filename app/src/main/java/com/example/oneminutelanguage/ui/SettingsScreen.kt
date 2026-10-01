package com.example.oneminutelanguage.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.translation.SupportedLanguages
import com.example.oneminutelanguage.ui.components.appCardColors
import com.example.oneminutelanguage.widget.FOCUS_SET_SIZE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onSettingsUpdated: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    LaunchedEffect(viewModel.applyState) {
        if (viewModel.applyState is SettingsApplyState.Success) {
            onSettingsUpdated()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val applyState = viewModel.applyState
            val isBusy = applyState is SettingsApplyState.DownloadingModels ||
                applyState is SettingsApplyState.Translating

            SettingsGroup("Languages") {
                LanguageRow(
                    label = "Source language (what you type)",
                    selectedCode = viewModel.sourceLanguage,
                    onSelect = viewModel::selectSourceLanguage
                )

                ListItem(
                    modifier = Modifier.clickable(onClick = viewModel::swapLanguages),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(Icons.Default.SwapVert, contentDescription = null) },
                    headlineContent = { Text("Swap languages") }
                )

                LanguageRow(
                    label = "Language you're learning",
                    selectedCode = viewModel.targetLanguage,
                    onSelect = viewModel::selectTargetLanguage
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    if (viewModel.sourceLanguage == viewModel.targetLanguage) {
                        Text(
                            text = "Source and target are the same language — pick two different ones.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = viewModel::applyChanges,
                        enabled = !isBusy && viewModel.sourceLanguage != viewModel.targetLanguage,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Settings")
                    }

                    when (applyState) {
                        is SettingsApplyState.Idle -> Unit

                        is SettingsApplyState.DownloadingModels -> BusyRow("Downloading language model…")

                        is SettingsApplyState.Translating ->
                            BusyRow("Translating existing words… (${applyState.current}/${applyState.total})")

                        is SettingsApplyState.Success -> StatusText("Settings updated.")

                        is SettingsApplyState.Error -> StatusText("Error: ${applyState.message}", isError = true)
                    }
                }
            }

            val defaultWordsState = viewModel.defaultWordsState
            val defaultWordsBusy = defaultWordsState is DefaultWordsState.DownloadingModels ||
                defaultWordsState is DefaultWordsState.Importing ||
                defaultWordsState is DefaultWordsState.Removing

            SettingsGroup("Starter words") {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text("Include default word list") },
                    supportingContent = {
                        Text("Adds a bundled starter vocabulary, translated into your current languages.")
                    },
                    trailingContent = {
                        Switch(
                            checked = viewModel.defaultWordsEnabled,
                            onCheckedChange = viewModel::onToggleDefaultWords,
                            enabled = !defaultWordsBusy
                        )
                    }
                )

                val status: (@Composable ColumnScope.() -> Unit)? = when (defaultWordsState) {
                    is DefaultWordsState.Idle -> null
                    is DefaultWordsState.DownloadingModels -> { { BusyRow("Downloading language model…") } }
                    is DefaultWordsState.Importing -> {
                        { BusyRow("Adding default words… (${defaultWordsState.current}/${defaultWordsState.total})") }
                    }
                    is DefaultWordsState.Removing -> { { BusyRow("Removing default words…") } }
                    is DefaultWordsState.Done -> {
                        { StatusText(if (viewModel.defaultWordsEnabled) "Default words added." else "Default words removed.") }
                    }
                    is DefaultWordsState.Error -> { { StatusText("Error: ${defaultWordsState.message}", isError = true) } }
                }
                if (status != null) {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) { status() }
                }
            }

            SettingsGroup("Widget") {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text("Focus mode") },
                    supportingContent = {
                        Text(
                            "The widget repeats a set of $FOCUS_SET_SIZE words. A word you answer correctly " +
                                "in a quiz makes room for the next one. Off shows a random word each time."
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = viewModel.focusMode,
                            onCheckedChange = viewModel::onToggleFocusMode
                        )
                    }
                )
            }
        }
    }

    if (viewModel.showDisableDefaultWordsConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDisableDefaultWords,
            title = { Text("Remove default words?") },
            text = {
                Text(
                    "This deletes every default word-list entry from your database — including " +
                        "any word you added manually that happens to match one by text. This can't be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDisableDefaultWords) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDisableDefaultWords) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = appCardColors(),
            content = content
        )
    }
}

@Composable
private fun BusyRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun StatusText(text: String, isError: Boolean = false) {
    Text(
        text = text,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
private fun LanguageRow(
    label: String,
    selectedCode: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        ListItem(
            modifier = Modifier.clickable { expanded = true },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(label) },
            supportingContent = { Text(SupportedLanguages.displayNameFor(selectedCode)) },
            trailingContent = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SupportedLanguages.all.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onSelect(option.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
