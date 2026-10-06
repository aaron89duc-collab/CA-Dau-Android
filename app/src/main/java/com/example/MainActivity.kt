package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.SaveManager
import com.example.ui.CreditsScreen
import com.example.ui.GameScreen
import com.example.ui.LevelSelectScreen
import com.example.ui.MainMenuScreen
import com.example.ui.SettingsScreen
import com.example.ui.UpgradeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.unity.UnityBridge

sealed class AppScreen {
    data object MainMenu : AppScreen()
    data object LevelSelect : AppScreen()
    data object Upgrade : AppScreen()
    data class Game(val levelId: Int = 1) : AppScreen()
    data object Settings : AppScreen()
    data object Credits : AppScreen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable fullscreen immersive mode for landscape gaming
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Initialize Unity Engine Integration Bridge
        UnityBridge.initializeEngine(applicationContext)

        val saveManager = SaveManager.getInstance(applicationContext)

        setContent {
            MyApplicationTheme {
                ShieldForceApp(saveManager = saveManager)
            }
        }
    }
}

@Composable
fun ShieldForceApp(
    saveManager: SaveManager,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.MainMenu) }

    when (val screen = currentScreen) {
        is AppScreen.MainMenu -> {
            MainMenuScreen(
                saveManager = saveManager,
                onStartGame = { currentScreen = AppScreen.Game(1) },
                onLevelSelect = { currentScreen = AppScreen.LevelSelect },
                onUpgrades = { currentScreen = AppScreen.Upgrade },
                onSettings = { currentScreen = AppScreen.Settings },
                onCredits = { currentScreen = AppScreen.Credits },
                modifier = modifier.fillMaxSize()
            )
        }

        is AppScreen.LevelSelect -> {
            LevelSelectScreen(
                saveManager = saveManager,
                onSelectLevel = { levelId -> currentScreen = AppScreen.Game(levelId) },
                onBack = { currentScreen = AppScreen.MainMenu },
                modifier = modifier.fillMaxSize()
            )
        }

        is AppScreen.Upgrade -> {
            UpgradeScreen(
                saveManager = saveManager,
                onBack = { currentScreen = AppScreen.MainMenu },
                modifier = modifier.fillMaxSize()
            )
        }

        is AppScreen.Game -> {
            GameScreen(
                onNavigateBack = { currentScreen = AppScreen.MainMenu },
                modifier = modifier.fillMaxSize()
            )
        }

        is AppScreen.Settings -> {
            SettingsScreen(
                saveManager = saveManager,
                onBack = { currentScreen = AppScreen.MainMenu },
                modifier = modifier.fillMaxSize()
            )
        }

        is AppScreen.Credits -> {
            CreditsScreen(
                onBack = { currentScreen = AppScreen.MainMenu },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
