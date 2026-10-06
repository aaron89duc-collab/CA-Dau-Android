package com.example.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.unity.UnityBridge

/**
 * Dedicated Hardware-Accelerated 60 FPS Render Surface
 * Synchronized with UnityBridge to deliver silky-smooth 60 FPS rendering.
 */
class GameSurfaceView(
    context: Context,
    val engine: GameEngine
) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    @Volatile
    private var isRunning: Boolean = false
    private var renderThread: Thread? = null

    private var targetFps: Int = 60
    private var targetFrameTimeMs: Long = 1000L / targetFps

    // Performance telemetry
    private var frameCount: Int = 0
    private var lastFpsTimestamp: Long = System.currentTimeMillis()
    private var currentFps: Int = 60

    init {
        holder.addCallback(this)
        setZOrderMediaOverlay(false)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        resume()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        engine.camera.viewportWidth = width.toFloat()
        engine.camera.viewportHeight = height.toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    fun resume() {
        if (!isRunning) {
            isRunning = true
            renderThread = Thread(this, "ShieldForce-RenderThread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
        }
    }

    fun pause() {
        isRunning = false
        try {
            renderThread?.join(500)
            renderThread = null
        } catch (_: InterruptedException) {}
    }

    override fun run() {
        var lastTime = System.nanoTime()

        while (isRunning) {
            val now = System.nanoTime()
            val elapsedNanos = now - lastTime
            lastTime = now

            val dt = (elapsedNanos / 1_000_000_000f).coerceIn(0.001f, 0.05f)

            // 1. Simulation Update
            engine.update(dt)

            // 2. Hardware Accelerated Render
            val frameStart = System.currentTimeMillis()
            var canvas: Canvas? = null
            try {
                canvas = holder.lockHardwareCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.rgb(6, 11, 25))
                    engine.render(canvas)
                }
            } catch (_: Exception) {
                // Fallback to standard canvas if hardware canvas locked
                try {
                    canvas = holder.lockCanvas()
                    if (canvas != null) {
                        canvas.drawColor(Color.rgb(6, 11, 25))
                        engine.render(canvas)
                    }
                } catch (_: Exception) {}
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (_: Exception) {}
                }
            }

            // 3. FPS & Performance Telemetry
            val frameTime = System.currentTimeMillis() - frameStart
            frameCount++
            val curTime = System.currentTimeMillis()
            if (curTime - lastFpsTimestamp >= 1000L) {
                currentFps = frameCount
                frameCount = 0
                lastFpsTimestamp = curTime
                UnityBridge.reportPerformance(currentFps, frameTime.toFloat(), 45)
            }

            // 4. Frame pacing sleep for battery & stable 60 FPS
            val sleepTime = targetFrameTimeMs - frameTime
            if (sleepTime > 1) {
                try {
                    Thread.sleep(sleepTime)
                } catch (_: InterruptedException) {}
            }
        }
    }
}
