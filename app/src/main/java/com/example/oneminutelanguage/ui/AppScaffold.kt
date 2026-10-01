package com.example.oneminutelanguage.ui

import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.School
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
import com.example.oneminutelanguage.ui.learn.CourseSelectScreen
import com.example.oneminutelanguage.ui.learn.DictionaryScreen
import com.example.oneminutelanguage.ui.learn.LearnScreen
import com.example.oneminutelanguage.ui.learn.LessonScreen
import com.example.oneminutelanguage.ui.learn.ReviewScreen
import com.example.oneminutelanguage.ui.learn.UnitScreen

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
                    onQuizClick = { mode -> navController.navigate(if (mode == null) "quiz" else "quiz?mode=$mode") },
                    onChooseCourse = { navController.navigate("course_select") },
                    onLessonClick = { navController.navigate("lesson/$it") },
                    onLearnClick = { navController.switchTab("learn") },
                    onReviewClick = { navController.navigate("review") }
                )
            }
            composable("learn") {
                LearnScreen(
                    onChooseCourse = { navController.navigate("course_select") },
                    onUnitClick = { navController.navigate("unit/$it") },
                    onDictionaryClick = { navController.navigate("dictionary") }
                )
            }
            composable("course_select") {
                CourseSelectScreen(onSelected = { navController.popBackStack() }, onBack = { navController.popBackStack() })
            }
            composable("unit/{unitId}") {
                UnitScreen(onBack = { navController.popBackStack() }, onLessonClick = { navController.navigate("lesson/$it") })
            }
            composable("lesson/{lessonId}") {
                LessonScreen(
                    onClose = { navController.popBackStack() },
                    // Replace the finished lesson, keeping the unit screen underneath.
                    onNextLesson = { id -> navController.navigate("lesson/$id") { popUpTo("lesson/{lessonId}") { inclusive = true } } },
                    onBackToUnit = { unitId ->
                        // Opened from Today (no unit screen below)? Then show the unit instead of just closing.
                        if (!navController.popBackStack("unit/{unitId}", inclusive = false)) {
                            navController.popBackStack()
                            navController.navigate("unit/$unitId")
                        }
                    }
                )
            }
            composable("practice") {
                PracticeScreen(
                    onQuizClick = { mode -> navController.navigate("quiz?mode=$mode") },
                    onReviewClick = { navController.navigate("review") }
                )
            }
            composable("words") {
                DatabaseScreen(
                    onAddWordClick = { navController.navigate("add_word") },
                    onDictionaryClick = { navController.navigate("dictionary") }
                )
            }
            composable("review") { ReviewScreen(onClose = { navController.popBackStack() }) }
            composable("dictionary") { DictionaryScreen(onBack = { navController.popBackStack() }) }
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
