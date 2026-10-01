package com.example.oneminutelanguage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.oneminutelanguage.ui.theme.AppBackground
import androidx.core.content.ContextCompat
import com.example.oneminutelanguage.ui.AppScaffold
import com.example.oneminutelanguage.ui.theme.OneMinuteLanguageTheme
import com.example.oneminutelanguage.widget.ScreenOnForegroundService

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        ScreenOnForegroundService.start(applicationContext)

        val openAddWordScreen = intent?.extras?.getBoolean("navigate_to_add_word", false) ?: false ||
                intent?.getBooleanExtra("navigate_to_add_word", false) ?: false

        setContent {
            OneMinuteLanguageTheme {
                AppBackground {
                    AppScaffold(startAtAddWord = openAddWordScreen)
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
