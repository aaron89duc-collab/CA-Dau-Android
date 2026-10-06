package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.engine.GameEngine
import com.example.engine.GameSurfaceView

@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Retain engine instance across recompositions
    val gameEngine = remember { GameEngine(context) }
    val surfaceView = remember { GameSurfaceView(context, gameEngine) }

    BackHandler {
        onNavigateBack()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    surfaceView.resume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    surfaceView.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            surfaceView.pause()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Hardware-Accelerated 60 FPS SurfaceView Render Layer
        AndroidView(
            factory = { surfaceView },
            modifier = Modifier.fillMaxSize()
        )

        // 2. High-Fidelity Jetpack Compose HUD & Controls Overlay Layer
        GameHudOverlay(
            engine = gameEngine,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.fillMaxSize()
        )
    }
}
