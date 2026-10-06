package com.example.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.engine.Camera2D
import com.example.engine.ObjectPool
import com.example.model.DamageType
import com.example.model.ShieldThrowState
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vibranium Shield Boomerang System
 * Follows GDD Section 4.1:
 * OUTBOUND -> va cham/max distance -> RETURN -> CATCH.
 * Khong cho nem shield moi khi shield dang ngoai tay.
 */
class ShieldProjectile(
    private val pool: ObjectPool
) {
    var state: ShieldThrowState = ShieldThrowState.IN_HAND
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var radius: Float = 24f
    var rotationAngle: Float = 0f

    var baseDamage: Int = 35
    var hasBombEffect: Boolean = false
    var hasPlasmaEffect: Boolean = false

    private var startX: Float = 0f
    private var maxRange: Float = 550f
    private var speed: Float = 750f

    // Hit enemies tracking to avoid damaging the same enemy twice on the same pass
    val hitEnemiesThisPass = HashSet<Int>()

    fun throwShield(originX: Float, originY: Float, facingRight: Boolean, damage: Int) {
        if (state != ShieldThrowState.IN_HAND) return
        state = ShieldThrowState.OUTBOUND
        x = originX + (if (facingRight) 25f else -25f)
        y = originY - 15f
        startX = originX
        vx = if (facingRight) speed else -speed
        vy = 0f
        baseDamage = damage
        hitEnemiesThisPass.clear()
    }

    fun update(targetPlayerX: Float, targetPlayerY: Float, dt: Float): Boolean {
        if (state == ShieldThrowState.IN_HAND) return false

        rotationAngle = (rotationAngle + 1200f * dt) % 360f

        // Shield trail particles
        if (Math.random() < 0.7) {
            pool.spawnParticle(
                x, y,
                -vx * 0.15f + ((Math.random() - 0.5) * 40).toFloat(),
                ((Math.random() - 0.5) * 40).toFloat(),
                4f, 0.2f, 0xFF00B4D8.toInt()
            )
        }

        when (state) {
            ShieldThrowState.OUTBOUND -> {
                x += vx * dt
                y += vy * dt

                val traveled = kotlin.math.abs(x - startX)
                if (traveled >= maxRange) {
                    state = ShieldThrowState.RETURN
                    hitEnemiesThisPass.clear()
                }
            }
            ShieldThrowState.RETURN -> {
                // Return trajectory towards player's current chest position
                val targetX = targetPlayerX
                val targetY = targetPlayerY - 25f

                val dx = targetX - x
                val dy = targetY - y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                if (dist < 40f) {
                    // Caught!
                    state = ShieldThrowState.IN_HAND
                    return true // Caught signal
                } else {
                    val returnSpeed = (speed * 1.15f).coerceAtLeast(650f)
                    vx = (dx / dist) * returnSpeed
                    vy = (dy / dist) * returnSpeed
                    x += vx * dt
                    y += vy * dt
                }
            }
            else -> {}
        }
        return false
    }

    fun turnAroundNow() {
        if (state == ShieldThrowState.OUTBOUND) {
            state = ShieldThrowState.RETURN
            hitEnemiesThisPass.clear()
        }
    }

    fun render(canvas: Canvas, camera: Camera2D, paint: Paint) {
        if (state == ShieldThrowState.IN_HAND) return

        val sx = camera.worldToScreenX(x)
        val sy = camera.worldToScreenY(y)

        canvas.save()
        canvas.translate(sx, sy)
        canvas.rotate(rotationAngle)

        // Outer glow
        paint.color = 0x5500B4D8
        paint.style = Paint.Style.FILL
        canvas.drawCircle(0f, 0f, radius + 5f, paint)

        // Outer red ring
        paint.color = Color.rgb(217, 4, 41)
        canvas.drawCircle(0f, 0f, radius, paint)

        // White/silver ring
        paint.color = Color.rgb(237, 242, 244)
        canvas.drawCircle(0f, 0f, radius * 0.78f, paint)

        // Inner red ring
        paint.color = Color.rgb(217, 4, 41)
        canvas.drawCircle(0f, 0f, radius * 0.56f, paint)

        // Center blue circle
        paint.color = Color.rgb(0, 119, 182)
        canvas.drawCircle(0f, 0f, radius * 0.35f, paint)

        // Center star
        paint.color = Color.WHITE
        val starR = radius * 0.28f
        for (i in 0 until 5) {
            val a1 = (i * 72.0 - 90.0) * Math.PI / 180.0
            val a2 = ((i + 2) * 72.0 - 90.0) * Math.PI / 180.0
            canvas.drawLine(
                (cos(a1) * starR).toFloat(), (sin(a1) * starR).toFloat(),
                (cos(a2) * starR).toFloat(), (sin(a2) * starR).toFloat(),
                paint
            )
        }

        canvas.restore()
    }
}
