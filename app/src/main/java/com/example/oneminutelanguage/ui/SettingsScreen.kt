package com.example.oneminutelanguage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.translation.SupportedLanguages

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onSettingsUpdated: () -> Unit = {}
) {
    LaunchedEffect(viewModel.applyState) {
        if (viewModel.applyState is SettingsApplyState.Success) {
            onSettingsUpdated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        LanguageDropdown(
            label = "Source language (what you type)",
            selectedCode = viewModel.sourceLanguage,
            onSelect = viewModel::selectSourceLanguage
        )

        Spacer(modifier = Modifier.height(20.dp))

        LanguageDropdown(
            label = "Language you're learning",
            selectedCode = viewModel.targetLanguage,
            onSelect = viewModel::selectTargetLanguage
        )

        if (viewModel.sourceLanguage == viewModel.targetLanguage) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Source and target are the same language — pick two different ones.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val applyState = viewModel.applyState
        val isBusy = applyState is SettingsApplyState.DownloadingModels ||
            applyState is SettingsApplyState.Translating

        Button(
            onClick = viewModel::applyChanges,
            enabled = !isBusy && viewModel.sourceLanguage != viewModel.targetLanguage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Settings")
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (applyState) {
            is SettingsApplyState.Idle -> Unit

            is SettingsApplyState.DownloadingModels -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Downloading language model…")
                }
            }

            is SettingsApplyState.Translating -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Translating existing words… (${applyState.current}/${applyState.total})")
                }
            }

            is SettingsApplyState.Success -> {
                Text(
                    text = "Settings updated.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is SettingsApplyState.Error -> {
                Text(
                    text = "Error: ${applyState.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LanguageDropdown(
    label: String,
    selectedCode: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = SupportedLanguages.displayNameFor(selectedCode)

    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedName)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
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
}
