package com.example.oneminutelanguage.ui

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.oneminutelanguage.ui.components.appCardColors

private class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("today", "Today", Icons.Default.Home),
    Tab("learn", "Learn", Icons.Default.School),
    Tab("practice", "Practice", Icons.Default.FitnessCenter),
    Tab("words", "Words", Icons.AutoMirrored.Filled.MenuBook)
)

@Composable
fun AppScaffold(startAtAddWord: Boolean) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Leaves Add Word: back to the previous screen, or closes the app when the widget's + started it (R3).
    val leave: () -> Unit = {
        if (!navController.popBackStack()) (navController.context as? Activity)?.finish()
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        // Tab screens apply the top inset themselves, full-screen routes apply all of safeDrawing.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (tabs.any { it.route == currentRoute }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.switchTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (startAtAddWord) "add_word" else "today",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("today") {
                TodayScreen(
                    onAddWordClick = { navController.navigate("add_word") },
                    onSettingsClick = { navController.navigate("settings") },
                    onQuizClick = { mode -> navController.navigate(if (mode == null) "quiz" else "quiz?mode=$mode") }
                )
            }
            composable("learn") { LearnPlaceholder() }
            composable("practice") {
                PracticeScreen(onQuizClick = { mode -> navController.navigate("quiz?mode=$mode") })
            }
            composable("words") {
                DatabaseScreen(onAddWordClick = { navController.navigate("add_word") })
            }
            composable("add_word") { AddWordScreen(onWordSaved = leave, onBack = leave) }
            composable("settings") {
                SettingsScreen(
                    onSettingsUpdated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "quiz?mode={mode}",
                arguments = listOf(navArgument("mode") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                QuizScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// Stage 2 replaces this with the real Learn tab.
@Composable
private fun LearnPlaceholder() {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text("Learn", style = MaterialTheme.typography.headlineSmall)
        Card(
            modifier = Modifier.padding(top = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = appCardColors()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Deep learning arrives soon", style = MaterialTheme.typography.titleMedium)
                Text(
                    "A structured Dutch course from A1 to B1 is on its way.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
