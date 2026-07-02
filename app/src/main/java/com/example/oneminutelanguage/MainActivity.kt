package com.example.oneminutelanguage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import com.example.oneminutelanguage.ui.AddWordScreen
import com.example.oneminutelanguage.ui.DatabaseScreen
import com.example.oneminutelanguage.ui.MainViewModel
import com.example.oneminutelanguage.ui.SettingsScreen
import com.example.oneminutelanguage.ui.theme.MeshGradientBackground
import com.example.oneminutelanguage.ui.theme.OneMinuteLanguageTheme
import com.example.oneminutelanguage.widget.ScreenOnForegroundService

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        ScreenOnForegroundService.start(applicationContext)

        val openAddWordScreen = intent?.extras?.getBoolean("navigate_to_add_word", false) ?: false ||
                intent?.getBooleanExtra("navigate_to_add_word", false) ?: false

        setContent {
            OneMinuteLanguageTheme {
                MeshGradientBackground {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding(),
                        color = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {
                        AppNavHost(startAtAddWord = openAddWordScreen)
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun AppNavHost(startAtAddWord: Boolean) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (startAtAddWord) "add_word" else "main"
    ) {
        composable("main") {
            MainScreen(
                onAddWordClick = { navController.navigate("add_word") },
                onViewDatabaseClick = { navController.navigate("database") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("add_word") {
            AddWordScreen(
                onWordSaved = {
                    if (!navController.popBackStack()) {
                        (navController.context as? android.app.Activity)?.finish()
                    }
                }
            )
        }
        composable("database") {
            DatabaseScreen(
                onAddWordClick = { navController.navigate("add_word") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onSettingsUpdated = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    onAddWordClick: () -> Unit = {},
    onViewDatabaseClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val totalWords by viewModel.totalWordsCount.collectAsState(initial = 0)
    val viewsToday by viewModel.todayViewCount.collectAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Widget views today: $viewsToday",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total words: $totalWords",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onAddWordClick,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Text("Add New Word")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onViewDatabaseClick,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Text("View Database")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSettingsClick,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Text("Settings")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    OneMinuteLanguageTheme {
        MainScreen()
    }
}
