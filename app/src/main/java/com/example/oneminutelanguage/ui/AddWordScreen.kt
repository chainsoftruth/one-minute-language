package com.example.oneminutelanguage.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.translation.AddWordViewModel
import com.example.oneminutelanguage.translation.TranslationState

@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel = viewModel(),
    onWordSaved: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Add New Word",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Word (${viewModel.sourceLanguageName})") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.translateWord(inputText) },
            enabled = inputText.isNotBlank() && viewModel.translationState !is TranslationState.Translating,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Translate")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.saveWord(
                    originalWord = inputText,
                    onSaved = onWordSaved
                )
            },
            enabled = viewModel.translationState is TranslationState.Success,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Word")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // React to the current translation state
        when (val state = viewModel.translationState) {
            is TranslationState.Idle -> {
                // Nothing to show yet
            }

            is TranslationState.DownloadingModel -> {
                Row {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Downloading language model…")
                }
            }

            is TranslationState.Translating -> {
                Row {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Translating…")
                }
            }

            is TranslationState.Success -> {
                Text(
                    text = "Translation: ${state.translatedText}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            is TranslationState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}