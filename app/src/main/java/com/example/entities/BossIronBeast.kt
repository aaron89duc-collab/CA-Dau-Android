package com.example.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.audio.GameAudio
import com.example.engine.Camera2D
import com.example.engine.ObjectPool
import com.example.model.BossPhase
import com.example.model.DamageType
import com.example.model.IDamageable
import kotlin.math.abs
import kotlin.math.sin

/**
 * Iron Beast - Level 1 Boss
 * Follows GDD Section 10:
 * HP: 1200, 2 Phases
 * Phase 1: Machine gun, ground stomp with shockwave
 * Phase 2: Missile barrage, enraged stomp, exposed core weak point on back (+100% damage!)
 * Telegraph warnings 0.4s - 1.0s
 */
class BossIronBeast(
    var x: Float,
    var y: Float,
    private val pool: ObjectPool,
    private val audio: GameAudio
) : IDamageable {

    val maxHp: Int = 1200
    var hp: Int = maxHp
    var phase: BossPhase = BossPhase.PHASE_1
    var isDead: Boolean = false

    val width: Float = 140f
    val height: Float = 180f
    var facingRight: Boolean = false

    // AI state & timers
    private var actionTimer: Float = 2.0f
    private var stateDuration: Float = 0f
    private var bossAction: BossAction = BossAction.IDLE

    // Telegraphing warning (GDD Section 10: 0.4 - 1.0s)
    var isTelegraphing: Boolean = false
    var telegraphProgress: Float = 0f
    var telegraphDuration: Float = 0.8f
    var telegraphWarningText: String = ""

    // Stomp physics
    private var isStomping: Boolean = false
    private var stompJumpVy: Float = 0f
    private var baseGroundY: Float = y

    // Hurt feedback
    var hurtTimer: Float = 0f

    enum class BossAction {
        IDLE,
        TELEGRAPH_MACHINE_GUN,
        MACHINE_GUN,
        TELEGRAPH_STOMP,
        STOMP,
        TELEGRAPH_MISSILES,
        MISSILES,
        RECOVERY // 1.5s recovery window for player counter-attack (GDD Section 10)
    }

    fun update(playerX: Float, playerY: Float, dt: Float, groundY: Float) {
        if (isDead) return
        baseGroundY = groundY

        if (hurtTimer > 0f) hurtTimer -= dt

        facingRight = playerX > x

        // Phase Transition at 50% HP (600 HP)
        if (phase == BossPhase.PHASE_1 && hp <= 600) {
            phase = BossPhase.PHASE_2
            audio.playBossRoar()
            pool.spawnDamageNumber(x, y - height * 0.8f, "PHASE 2: ENRAGED!", 0xFFFF1744.toInt())
            pool.spawnBurst(x, y - height * 0.5f, 40, 0xFFFF1744.toInt(), 2.0f)
            bossAction = BossAction.RECOVERY
            actionTimer = 2.0f
        }

        actionTimer -= dt

        when (bossAction) {
            BossAction.IDLE -> {
                if (actionTimer <= 0f) {
                    // Pick next attack pattern
                    val rand = Math.random()
                    if (phase == BossPhase.PHASE_1) {
                        if (rand < 0.5) startMachineGunTelegraph() else startStompTelegraph()
                    } else {
                        // Phase 2 includes Missile Barrage!
                        when {
                            rand < 0.35 -> startMachineGunTelegraph()
                            rand < 0.70 -> startMissileTelegraph()
                            else -> startStompTelegraph()
                        }
                    }
                }
            }

            BossAction.TELEGRAPH_MACHINE_GUN,
            BossAction.TELEGRAPH_STOMP,
            BossAction.TELEGRAPH_MISSILES -> {
                telegraphProgress = (telegraphDuration - actionTimer) / telegraphDuration
                if (actionTimer <= 0f) {
                    isTelegraphing = false
                    when (bossAction) {
                        BossAction.TELEGRAPH_MACHINE_GUN -> executeMachineGun(playerX, playerY)
                        BossAction.TELEGRAPH_STOMP -> executeStomp()
                        BossAction.TELEGRAPH_MISSILES -> executeMissiles(playerX, playerY)
                        else -> {}
                    }
                }
            }

            BossAction.MACHINE_GUN -> {
                // Continuous bursts
                stateDuration += dt
                if ((stateDuration * 12).toInt() % 2 == 0) {
                    val gunX = x + (if (facingRight) width * 0.5f else -width * 0.5f)
                    val gunY = y - height * 0.45f
                    val bulletVx = if (facingRight) 720f else -720f
                    pool.spawnProjectile(gunX, gunY, bulletVx, ((Math.random() - 0.5) * 60).toFloat(), 12, DamageType.Normal, false, 8f, 0xFFFF9100.toInt())
                }
                if (actionTimer <= 0f) {
                    enterRecovery()
                }
            }

            BossAction.STOMP -> {
                // Jump up and smash down
                stompJumpVy += 2200f * dt
                y += stompJumpVy * dt
                if (y >= baseGroundY) {
                    y = baseGroundY
                    isStomping = false
                    audio.playExplosion()
                    pool.spawnBurst(x, y, 35, 0xFFFF6D00.toInt(), 2.2f)

                    // Ground shockwaves travelling left and right!
                    val waveSpeed = if (phase == BossPhase.PHASE_2) 580f else 460f
                    pool.spawnProjectile(x - 40f, y - 10f, -waveSpeed, 0f, 18, DamageType.Normal, false, 20f, 0xFFFFAB00.toInt(), isShockwave = true, maxLife = 2.2f)
                    pool.spawnProjectile(x + 40f, y - 10f, waveSpeed, 0f, 18, DamageType.Normal, false, 20f, 0xFFFFAB00.toInt(), isShockwave = true, maxLife = 2.2f)

                    enterRecovery()
                }
            }

            BossAction.MISSILES -> {
                // Phase 2 missile pods
                stateDuration += dt
                if (actionTimer <= 0f) {
                    enterRecovery()
                }
            }

            BossAction.RECOVERY -> {
                // Player counter-attack window (1.5s)
                if (actionTimer <= 0f) {
                    bossAction = BossAction.IDLE
                    actionTimer = 0.8f
                }
            }
        }
    }

    private fun startMachineGunTelegraph() {
        bossAction = BossAction.TELEGRAPH_MACHINE_GUN
        isTelegraphing = true
        telegraphDuration = 0.6f
        actionTimer = telegraphDuration
        telegraphWarningText = "! GATLING CANNON !"
        audio.vibrate(35)
    }

    private fun executeMachineGun(playerX: Float, playerY: Float) {
        bossAction = BossAction.MACHINE_GUN
        actionTimer = 1.4f
        stateDuration = 0f
        audio.playShieldShot()
    }

    private fun startStompTelegraph() {
        bossAction = BossAction.TELEGRAPH_STOMP
        isTelegraphing = true
        telegraphDuration = 0.8f
        actionTimer = telegraphDuration
        telegraphWarningText = "▲ SEISMIC SLAM ▲"
        audio.vibrate(50)
    }

    private fun executeStomp() {
        bossAction = BossAction.STOMP
        isStomping = true
        stompJumpVy = -700f // Leap up into the air
    }

    private fun startMissileTelegraph() {
        bossAction = BossAction.TELEGRAPH_MISSILES
        isTelegraphing = true
        telegraphDuration = 0.9f
        actionTimer = telegraphDuration
        telegraphWarningText = "⚠ MISSILE BARRAGE ⚠"
        audio.vibrate(70)
    }

    private fun executeMissiles(playerX: Float, playerY: Float) {
        bossAction = BossAction.MISSILES
        actionTimer = 1.0f
        audio.playBossRoar()
        // Fire 5 homing / arcing rockets upward
        for (i in -2..2) {
            val podX = x + (i * 24f)
            val podY = y - height * 0.9f
            val launchVx = i * 90f + (if (facingRight) 160f else -160f)
            pool.spawnProjectile(
                podX, podY, launchVx, -520f,
                22, DamageType.Explosion, false,
                12f, 0xFFFF1744.toInt(),
                isMissile = true, maxLife = 2.8f
            )
        }
    }

    private fun enterRecovery() {
        bossAction = BossAction.RECOVERY
        actionTimer = 1.5f // 1.5 seconds player opening (GDD Section 10)
    }

    override fun takeDamage(damage: Int, hitPointX: Float, hitPointY: Float, type: DamageType) {
        if (isDead) return

        // Weak point check: Back Core (GDD Section 10: "Core sau lưng")
        val attackFromRear = (facingRight && hitPointX < x) || (!facingRight && hitPointX > x)
        var actualDamage = damage

        if (attackFromRear) {
            actualDamage *= 2 // 100% bonus damage on exposed back core!
            pool.spawnDamageNumber(x, y - height * 0.9f, "WEAK POINT CRIT! $actualDamage", 0xFFFFD700.toInt())
            pool.spawnBurst(hitPointX, hitPointY, 14, 0xFFFFD700.toInt(), 1.4f)
        } else {
            pool.spawnDamageNumber(x, y - height * 0.7f, "$actualDamage", 0xFFFFEB3B.toInt())
            pool.spawnBurst(hitPointX, hitPointY, 6, 0xFFFF5722.toInt(), 0.8f)
        }

        hp -= actualDamage
        hurtTimer = 0.15f
        audio.vibrate(30)

        if (hp <= 0) {
            hp = 0
            die()
        }
    }

    private fun die() {
        isDead = true
        bossAction = BossAction.IDLE
        audio.playVictoryFanfare()
        pool.spawnBurst(x, y - height * 0.5f, 60, 0xFFFF1744.toInt(), 2.5f)
        pool.spawnBurst(x, y - height * 0.8f, 50, 0xFFFFB703.toInt(), 2.2f)
        pool.spawnDamageNumber(x, y - height, "IRON BEAST DESTROYED!", 0xFF00FFCC.toInt())
    }

    fun render(canvas: Canvas, camera: Camera2D, paint: Paint) {
        if (isDead) return

        val sx = camera.worldToScreenX(x)
        val sy = camera.worldToScreenY(y)

        // Draw Telegraph warning beam if telegraphing
        if (isTelegraphing) {
            paint.color = 0x66FF1744
            paint.style = Paint.Style.FILL
            val warnWidth = (telegraphProgress * 180f)
            val warnX = if (facingRight) sx else sx - warnWidth
            canvas.drawRect(warnX, sy - height, warnX + warnWidth, sy, paint)
        }

        canvas.save()
        canvas.translate(sx, sy)
        if (!facingRight) canvas.scale(-1f, 1f)

        // Heavy Mech Chassis
        val isFlashing = hurtTimer > 0f
        paint.color = if (isFlashing) Color.WHITE else Color.rgb(33, 43, 54)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(-width * 0.5f, -height, width * 0.5f, 0f), 16f, 16f, paint)

        // Armored Chest Plate
        paint.color = if (phase == BossPhase.PHASE_2) Color.rgb(183, 28, 28) else Color.rgb(69, 90, 100)
        canvas.drawRoundRect(RectF(-width * 0.4f, -height * 0.85f, width * 0.4f, -height * 0.35f), 12f, 12f, paint)

        // Exposed Back Core (Weak point - glowing red/cyan sphere)
        paint.color = if (phase == BossPhase.PHASE_2) Color.rgb(255, 23, 68) else Color.rgb(0, 229, 255)
        canvas.drawCircle(-width * 0.45f, -height * 0.6f, 16f, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(-width * 0.45f, -height * 0.6f, 7f, paint)

        // Heavy Gatling cannon arm
        paint.color = Color.rgb(27, 38, 49)
        canvas.drawRect(width * 0.3f, -height * 0.55f, width * 0.8f, -height * 0.35f, paint)
        paint.color = Color.rgb(255, 152, 0)
        canvas.drawCircle(width * 0.8f, -height * 0.45f, 8f, paint)

        // Missile Pods on Shoulder (Phase 2 active)
        if (phase == BossPhase.PHASE_2) {
            paint.color = Color.rgb(213, 0, 0)
            canvas.drawRect(-width * 0.35f, -height - 24f, width * 0.35f, -height, paint)
        }

        // Hydraulic Legs
        paint.color = Color.rgb(15, 23, 30)
        canvas.drawRect(-width * 0.4f, -height * 0.3f, -width * 0.15f, 0f, paint)
        canvas.drawRect(width * 0.15f, -height * 0.3f, width * 0.4f, 0f, paint)

        canvas.restore()
    }
}
