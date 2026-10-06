package com.example.engine

import kotlin.math.sqrt

/**
 * High performance 2D vector for real-time physics and camera math.
 */
class Vector2D(var x: Float = 0f, var y: Float = 0f) {

    fun set(newX: Float, newY: Float): Vector2D {
        x = newX
        y = newY
        return this
    }

    fun add(dx: Float, dy: Float): Vector2D {
        x += dx
        y += dy
        return this
    }

    fun length(): Float = sqrt(x * x + y * y)

    fun normalize(): Vector2D {
        val len = length()
        if (len > 0.0001f) {
            x /= len
            y /= len
        }
        return this
    }

    fun distanceTo(otherX: Float, otherY: Float): Float {
        val dx = x - otherX
        val dy = y - otherY
        return sqrt(dx * dx + dy * dy)
    }

    fun copy(): Vector2D = Vector2D(x, y)
}

/**
 * Camera system matching GDD Section 17:
 * - Orthographic camera
 * - Reference aspect 16:9
 * - Follow player with damping
 * - Level boundary clamping
 * - Boss arena locking
 * - Camera shake limited to 0.1 - 0.2s
 */
class Camera2D(
    var viewportWidth: Float = 1920f,
    var viewportHeight: Float = 1080f
) {
    var posX: Float = 0f
    var posY: Float = 0f

    private var targetX: Float = 0f
    private var targetY: Float = 0f

    // Bounds
    var minX: Float = 0f
    var maxX: Float = 12000f
    var minY: Float = -200f
    var maxY: Float = 600f

    var isLockedToBossArena: Boolean = false
    var bossArenaMinX: Float = 7500f
    var bossArenaMaxX: Float = 9500f

    // Screen Shake
    private var shakeTimer: Float = 0f
    private var shakeIntensity: Float = 0f
    var shakeOffsetX: Float = 0f
        private set
    var shakeOffsetY: Float = 0f
        private set

    fun triggerShake(durationSeconds: Float = 0.15f, intensity: Float = 12f) {
        shakeTimer = durationSeconds.coerceAtMost(0.25f)
        shakeIntensity = intensity
    }

    fun update(targetPlayerX: Float, targetPlayerY: Float, dt: Float) {
        // Target tracking with lead offset ahead of player
        targetX = targetPlayerX - viewportWidth * 0.4f
        targetY = targetPlayerY - viewportHeight * 0.6f

        if (isLockedToBossArena) {
            targetX = targetX.coerceIn(bossArenaMinX, bossArenaMaxX - viewportWidth)
        } else {
            targetX = targetX.coerceIn(minX, maxX - viewportWidth)
        }
        targetY = targetY.coerceIn(minY, maxY)

        // Damping lerp (smooth follow)
        val lerpFactor = (8.0f * dt).coerceIn(0f, 1f)
        posX += (targetX - posX) * lerpFactor
        posY += (targetY - posY) * lerpFactor

        // Process shake
        if (shakeTimer > 0f) {
            shakeTimer -= dt
            shakeOffsetX = ((Math.random() - 0.5) * 2.0 * shakeIntensity).toFloat()
            shakeOffsetY = ((Math.random() - 0.5) * 2.0 * shakeIntensity).toFloat()
        } else {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
        }
    }

    fun worldToScreenX(worldX: Float): Float = worldX - posX + shakeOffsetX
    fun worldToScreenY(worldY: Float): Float = worldY - posY + shakeOffsetY

    fun isVisible(worldX: Float, worldY: Float, radius: Float = 100f): Boolean {
        val sx = worldToScreenX(worldX)
        val sy = worldToScreenY(worldY)
        return sx >= -radius && sx <= viewportWidth + radius &&
               sy >= -radius && sy <= viewportHeight + radius
    }
}
