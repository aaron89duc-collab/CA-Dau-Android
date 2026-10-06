package com.example.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.example.audio.GameAudio
import com.example.engine.Camera2D
import com.example.engine.ExplodingBarrel
import com.example.engine.ObjectPool
import com.example.model.DamageType
import com.example.model.IDamageable

/**
 * Solid ground platform or elevated highway bridge.
 */
class Platform(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val isElevatedBridge: Boolean = false
) {
    fun contains(px: Float, py: Float): Boolean {
        return px in left..right && py in top..bottom
    }
}

/**
 * Interactive Checkpoint (GDD Section 6)
 */
class CheckpointTrigger(
    val x: Float,
    val y: Float
) {
    var isActivated: Boolean = false
    val width: Float = 40f
    val height: Float = 90f

    fun checkTrigger(playerX: Float, playerY: Float): Boolean {
        if (!isActivated && kotlin.math.abs(playerX - x) < 60f) {
            isActivated = true
            return true
        }
        return false
    }

    fun render(canvas: Canvas, camera: Camera2D, paint: Paint) {
        val sx = camera.worldToScreenX(x)
        val sy = camera.worldToScreenY(y)

        // Pole
        paint.color = Color.rgb(176, 190, 197)
        paint.style = Paint.Style.FILL
        canvas.drawRect(sx - 4f, sy - height, sx + 4f, sy, paint)

        // Holographic Flag
        paint.color = if (isActivated) Color.rgb(0, 230, 118) else Color.rgb(255, 171, 0)
        canvas.drawRect(sx + 4f, sy - height, sx + 36f, sy - height + 28f, paint)

        // Star on flag
        paint.color = Color.WHITE
        canvas.drawCircle(sx + 20f, sy - height + 14f, 6f, paint)
    }
}

/**
 * Level 1 Environment & Scenery Manager
 * Level 1: "City Under Attack" (Night Cyberpunk City)
 * Manages Platforms, Exploding Barrels, Checkpoints, and 3D Parallax Backdrop
 */
class LevelEnvironment(
    private val pool: ObjectPool,
    private val audio: GameAudio
) {
    val groundY: Float = 750f
    val levelEndX: Float = 9500f

    val platforms = mutableListOf<Platform>()
    val barrels = mutableListOf<ExplodingBarrel>()
    val checkpoint = CheckpointTrigger(3500f, groundY)

    init {
        setupLevel1Geometry()
    }

    private fun setupLevel1Geometry() {
        // Base ground from 0 to 9500
        platforms.add(Platform(-500f, groundY, 9800f, groundY + 400f))

        // S2: Crates & Platforms near Exploding Barrels
        platforms.add(Platform(2200f, groundY - 120f, 2500f, groundY - 90f))
        platforms.add(Platform(2800f, groundY - 140f, 3100f, groundY - 110f))

        // S3: Elevated Highway / Bridge (Cầu vượt)
        platforms.add(Platform(3700f, groundY - 180f, 4400f, groundY - 150f, isElevatedBridge = true))
        platforms.add(Platform(4600f, groundY - 220f, 5300f, groundY - 190f, isElevatedBridge = true))

        // S4: Military Barricades
        platforms.add(Platform(5700f, groundY - 100f, 6000f, groundY - 80f))

        // Exploding Barrels in Segment S2 (GDD Section 9 & 11)
        barrels.add(ExplodingBarrel().apply { reset(2150f, groundY - 60f) })
        barrels.add(ExplodingBarrel().apply { reset(2400f, groundY - 180f) })
        barrels.add(ExplodingBarrel().apply { reset(2950f, groundY - 200f) })
        barrels.add(ExplodingBarrel().apply { reset(3250f, groundY - 60f) })
        barrels.add(ExplodingBarrel().apply { reset(5900f, groundY - 60f) })
    }

    fun damageBarrel(barrel: ExplodingBarrel, damage: Int): Boolean {
        if (!barrel.active || barrel.isExploded) return false
        barrel.hp -= damage
        if (barrel.hp <= 0) {
            barrel.isExploded = true
            barrel.active = false
            audio.playExplosion()
            pool.spawnBurst(barrel.x + barrel.width * 0.5f, barrel.y + barrel.height * 0.5f, 40, 0xFFFF3D00.toInt(), 2.2f)
            pool.spawnDamageNumber(barrel.x, barrel.y - 20f, "BOOM! (80 AoE)", 0xFFFF6E40.toInt())
            return true // Triggered AoE explosion
        }
        return false
    }

    /**
     * Renders 4 Layers of 3D Parallax Depth for high visual fidelity:
     * 1. Sky & distant skyscrapers (slowest parallax 0.08x)
     * 2. Midground neon towers & spotlights (parallax 0.25x)
     * 3. Elevated bridge pillars & railings (parallax 0.65x)
     * 4. Foreground road, neon curb lights, and barricades (parallax 1.0x)
     */
    fun renderBackground(canvas: Canvas, camera: Camera2D, paint: Paint) {
        val w = camera.viewportWidth
        val h = camera.viewportHeight

        // 1. Sky Gradient (Deep Night Blue to Cyber Navy)
        val skyGradient = LinearGradient(0f, 0f, 0f, h, Color.rgb(6, 11, 25), Color.rgb(18, 30, 58), Shader.TileMode.CLAMP)
        paint.shader = skyGradient
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        // Distant 3D Skyscrapers (Parallax 0.1x)
        paint.color = Color.rgb(13, 22, 42)
        val distOffset = (camera.posX * 0.08f) % 300f
        var bx = -distOffset - 200f
        while (bx < w + 200f) {
            canvas.drawRect(bx, h * 0.25f, bx + 160f, h, paint)
            // Glowing cyan/yellow window matrices
            paint.color = 0x3300E5FF
            for (wy in 0..5) {
                canvas.drawRect(bx + 20f, h * 0.3f + wy * 45f, bx + 50f, h * 0.3f + wy * 45f + 25f, paint)
                canvas.drawRect(bx + 80f, h * 0.3f + wy * 45f, bx + 110f, h * 0.3f + wy * 45f + 25f, paint)
            }
            paint.color = Color.rgb(13, 22, 42)
            bx += 200f
        }

        // Midground Neon Towers & Bridge Spans (Parallax 0.3x)
        paint.color = Color.rgb(20, 35, 65)
        val midOffset = (camera.posX * 0.28f) % 450f
        var mx = -midOffset - 250f
        while (mx < w + 250f) {
            canvas.drawRect(mx, h * 0.45f, mx + 220f, h, paint)
            // Neon billboard glow
            paint.color = 0x44D90429
            canvas.drawRect(mx + 30f, h * 0.48f, mx + 190f, h * 0.55f, paint)
            paint.color = Color.rgb(20, 35, 65)
            mx += 360f
        }

        // Checkpoint rendering
        checkpoint.render(canvas, camera, paint)

        // Platforms & Bridges
        for (plat in platforms) {
            val sx1 = camera.worldToScreenX(plat.left)
            val sy1 = camera.worldToScreenY(plat.top)
            val sx2 = camera.worldToScreenX(plat.right)
            val sy2 = camera.worldToScreenY(plat.bottom)

            if (sx2 < 0 || sx1 > w) continue

            if (plat.isElevatedBridge) {
                // Elevated highway concrete slab
                paint.color = Color.rgb(38, 50, 56)
                canvas.drawRoundRect(RectF(sx1, sy1, sx2, sy2), 8f, 8f, paint)
                // Glowing safety guardrail
                paint.color = Color.rgb(255, 193, 7)
                canvas.drawRect(sx1, sy1 - 8f, sx2, sy1, paint)
            } else {
                // Asphalt Street
                paint.color = Color.rgb(25, 33, 44)
                canvas.drawRect(sx1, sy1, sx2, sy2, paint)
                // Neon road curb stripe
                paint.color = Color.rgb(0, 180, 216)
                canvas.drawRect(sx1, sy1, sx2, sy1 + 8f, paint)
            }
        }

        // Exploding Barrels (Segment S2)
        for (b in barrels) {
            if (!b.active || b.isExploded) continue
            val bsx = camera.worldToScreenX(b.x)
            val bsy = camera.worldToScreenY(b.y)

            if (bsx < -100f || bsx > w + 100f) continue

            // Steel barrel cylinder
            paint.color = Color.rgb(216, 67, 21) // Hazard Red-Orange
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(bsx, bsy, bsx + b.width, bsy + b.height), 8f, 8f, paint)

            // Hazard warning band
            paint.color = Color.rgb(255, 235, 59)
            canvas.drawRect(bsx, bsy + 18f, bsx + b.width, bsy + 32f, paint)
            paint.color = Color.BLACK
            canvas.drawText("TNT", bsx + 10f, bsy + 29f, paint)
        }
    }
}
