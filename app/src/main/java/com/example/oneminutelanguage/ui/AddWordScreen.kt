package com.example.oneminutelanguage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneminutelanguage.translation.AddWordViewModel
import com.example.oneminutelanguage.translation.TranslationState
import com.example.oneminutelanguage.ui.components.appCardColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel = viewModel(),
    onWordSaved: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var editedTranslation by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.translationState) {
        val state = viewModel.translationState
        if (state is TranslationState.Success) {
            editedTranslation = state.translatedText
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Add new word") },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Same two directions as the old ⇄ button: the source language first, or the language you're learning.
            val sourceName = if (viewModel.targetFirst) viewModel.outputLanguageName else viewModel.inputLanguageName
            val targetName = if (viewModel.targetFirst) viewModel.inputLanguageName else viewModel.outputLanguageName
            val directions = listOf("$sourceName → $targetName", "$targetName → $sourceName")

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                directions.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = (index == 1) == viewModel.targetFirst,
                        onClick = { if ((index == 1) != viewModel.targetFirst) viewModel.toggleDirection() },
                        shape = SegmentedButtonDefaults.itemShape(index, directions.size),
                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Word (${viewModel.inputLanguageName})") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.translateWord(inputText) },
                enabled = inputText.isNotBlank() && viewModel.translationState !is TranslationState.Translating,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Translate")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveWord(
                        originalWord = inputText,
                        translatedWord = editedTranslation,
                        onSaved = onWordSaved
                    )
                },
                enabled = viewModel.translationState is TranslationState.Success && editedTranslation.isNotBlank(),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Word")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = viewModel.translationState) {
                is TranslationState.Idle -> {
                }

                is TranslationState.DownloadingModel -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Downloading language model…")
                    }
                }

                is TranslationState.Translating -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translating…")
                    }
                }

                is TranslationState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = appCardColors()
                    ) {
                        OutlinedTextField(
                            value = editedTranslation,
                            onValueChange = { editedTranslation = it },
                            label = { Text("Translation (${viewModel.outputLanguageName})") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
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
}
