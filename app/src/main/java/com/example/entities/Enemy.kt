package com.example.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.audio.GameAudio
import com.example.engine.Camera2D
import com.example.engine.ObjectPool
import com.example.model.DamageType
import com.example.model.EnemyType
import com.example.model.IDamageable
import com.example.model.PowerUpType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class EnemyFsmState {
    Idle,
    Patrol,
    Detect,
    Chase,
    Attack,
    Hurt,
    Dead
}

/**
 * Base Enemy Class with FSM
 * Follows GDD Section 7 & 7.1
 */
abstract class EnemyBase(
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var maxHp: Int,
    val pool: ObjectPool,
    val audio: GameAudio
) : IDamageable {

    var id: Int = nextId++
    var hp: Int = maxHp
    var vx: Float = 0f
    var vy: Float = 0f
    var isGrounded: Boolean = true
    var facingRight: Boolean = false
    var isDead: Boolean = false

    var state: EnemyFsmState = EnemyFsmState.Patrol
    var patrolLeftX: Float = x - 200f
    var patrolRightX: Float = x + 200f
    var detectionRange: Float = 450f
    var attackCooldownTimer: Float = 0f

    var hurtTimer: Float = 0f
    var width: Float = 44f
    var height: Float = 64f

    companion object {
        private var nextId = 1
    }

    abstract fun update(playerX: Float, playerY: Float, dt: Float, groundY: Float)
    abstract fun render(canvas: Canvas, camera: Camera2D, paint: Paint)

    override fun takeDamage(damage: Int, hitPointX: Float, hitPointY: Float, type: DamageType) {
        if (isDead) return
        hp -= damage
        hurtTimer = 0.2f
        state = EnemyFsmState.Hurt
        audio.vibrate(20)
        pool.spawnDamageNumber(x, y - height * 0.7f, "$damage", 0xFFFFEB3B.toInt())
        pool.spawnBurst(hitPointX, hitPointY, 6, 0xFFFF5722.toInt(), 0.7f)

        if (hp <= 0) {
            die()
        }
    }

    open fun die() {
        if (isDead) return
        isDead = true
        state = EnemyFsmState.Dead
        audio.playExplosion()
        pool.spawnBurst(x, y - height * 0.5f, 18, 0xFFFF1744.toInt(), 1.2f)

        // Drop Coin or PowerUp (GDD Section 8)
        val rand = Math.random()
        when {
            rand < 0.60 -> pool.spawnPickup(x, y - 20f, PowerUpType.COIN)
            rand < 0.75 -> pool.spawnPickup(x, y - 20f, PowerUpType.HEALTH)
            rand < 0.85 -> pool.spawnPickup(x, y - 20f, PowerUpType.DOUBLE_SHOT)
            rand < 0.93 -> pool.spawnPickup(x, y - 20f, PowerUpType.SUPER_SHIELD)
            else -> pool.spawnPickup(x, y - 20f, PowerUpType.ULTIMATE)
        }
    }
}

/**
 * Mutant Soldier: Chase + melee attack (GDD Section 7)
 */
class MutantSoldier(
    x: Float,
    y: Float,
    pool: ObjectPool,
    audio: GameAudio
) : EnemyBase(EnemyType.MUTANT_SOLDIER, x, y, 30, pool, audio) {

    private val moveSpeed = 160f
    private val damage = 10

    override fun update(playerX: Float, playerY: Float, dt: Float, groundY: Float) {
        if (isDead) return

        if (hurtTimer > 0f) {
            hurtTimer -= dt
            if (hurtTimer <= 0f) state = EnemyFsmState.Chase
        }

        val dist = abs(playerX - x)

        // FSM Logic
        when (state) {
            EnemyFsmState.Patrol -> {
                if (dist < detectionRange) {
                    state = EnemyFsmState.Chase
                } else {
                    if (facingRight) {
                        vx = moveSpeed * 0.6f
                        if (x >= patrolRightX) facingRight = false
                    } else {
                        vx = -moveSpeed * 0.6f
                        if (x <= patrolLeftX) facingRight = true
                    }
                }
            }
            EnemyFsmState.Chase -> {
                facingRight = playerX > x
                vx = if (facingRight) moveSpeed else -moveSpeed

                if (dist < 50f) {
                    state = EnemyFsmState.Attack
                    attackCooldownTimer = 0.5f
                }
            }
            EnemyFsmState.Attack -> {
                vx = 0f
                attackCooldownTimer -= dt
                if (attackCooldownTimer <= 0f) {
                    state = EnemyFsmState.Chase
                }
            }
            else -> {}
        }

        x += vx * dt
        y = groundY
    }

    override fun render(canvas: Canvas, camera: Camera2D, paint: Paint) {
        if (isDead) return
        val sx = camera.worldToScreenX(x)
        val sy = camera.worldToScreenY(y)

        canvas.save()
        canvas.translate(sx, sy)
        if (!facingRight) canvas.scale(-1f, 1f)

        // Mutant bio-soldier body (Dark purple / toxic green)
        paint.color = if (hurtTimer > 0f) Color.WHITE else Color.rgb(106, 27, 154)
        paint.style = Paint.Style.FILL
        canvas.drawRect(-18f, -height, 18f, 0f, paint)

        // Toxic eye visor
        paint.color = Color.rgb(0, 230, 118)
        canvas.drawCircle(8f, -height + 14f, 5f, paint)

        // Cyber claw
        paint.color = Color.rgb(255, 87, 34)
        canvas.drawRect(14f, -height + 26f, 26f, -height + 34f, paint)

        canvas.restore()
    }
}

/**
 * Flying Drone: Bay + 3-shot burst (GDD Section 7)
 */
class FlyingDrone(
    x: Float,
    y: Float,
    pool: ObjectPool,
    audio: GameAudio
) : EnemyBase(EnemyType.FLYING_DRONE, x, y, 20, pool, audio) {

    private var shootTimer: Float = 1.8f
    private var hoverTimer: Float = 0f
    private val flightAltitudeY: Float = y
    private var burstCount: Int = 0
    private var burstInterval: Float = 0f

    init {
        height = 36f
        width = 44f
    }

    override fun update(playerX: Float, playerY: Float, dt: Float, groundY: Float) {
        if (isDead) return
        hoverTimer += dt

        // Hover sine wave
        y = flightAltitudeY + sin(hoverTimer * 3.0).toFloat() * 25f

        facingRight = playerX > x
        val targetX = playerX + (if (facingRight) -180f else 180f)
        x += (targetX - x) * 1.5f * dt

        // Burst shooting
        shootTimer -= dt
        if (shootTimer <= 0f) {
            burstCount = 3
            burstInterval = 0.12f
            shootTimer = 2.4f
        }

        if (burstCount > 0) {
            burstInterval -= dt
            if (burstInterval <= 0f) {
                burstCount--
                burstInterval = 0.14f
                // Shoot plasma projectile towards player
                val dx = playerX - x
                val dy = playerY - y
                val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val bulletSpeed = 480f
                pool.spawnProjectile(
                    x, y,
                    (dx / len) * bulletSpeed,
                    (dy / len) * bulletSpeed,
                    8,
                    DamageType.Plasma,
                    false,
                    7f,
                    0xFFFF0055.toInt()
                )
            }
        }
    }

    override fun render(canvas: Canvas, camera: Camera2D, paint: Paint) {
        if (isDead) return
        val sx = camera.worldToScreenX(x)
        val sy = camera.worldToScreenY(y)

        canvas.save()
        canvas.translate(sx, sy)

        // Drone metallic chassis
        paint.color = if (hurtTimer > 0f) Color.WHITE else Color.rgb(69, 90, 100)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(0f, 0f, 18f, paint)

        // Rotor blades
        paint.color = Color.rgb(207, 216, 220)
        val rotorOffset = sin(hoverTimer * 25.0).toFloat() * 14f
        canvas.drawLine(-24f, -14f, 24f, -14f, paint)
        canvas.drawCircle(-20f + rotorOffset * 0.2f, -14f, 4f, paint)
        canvas.drawCircle(20f - rotorOffset * 0.2f, -14f, 4f, paint)

        // Glowing red optic scanner
        paint.color = Color.rgb(255, 23, 68)
        canvas.drawCircle(if (facingRight) 6f else -6f, 2f, 6f, paint)

        canvas.restore()
    }
}

/**
 * Shield Soldier: Has frontal shield, blocks direct frontal damage (GDD Section 7)
 */
class ShieldSoldier(
    x: Float,
    y: Float,
    pool: ObjectPool,
    audio: GameAudio
) : EnemyBase(EnemyType.SHIELD_SOLDIER, x, y, 50, pool, audio) {

    private val moveSpeed = 120f

    override fun update(playerX: Float, playerY: Float, dt: Float, groundY: Float) {
        if (isDead) return
        facingRight = playerX > x

        // Slow disciplined advance towards player
        val dist = abs(playerX - x)
        if (dist > 70f) {
            vx = if (facingRight) moveSpeed else -moveSpeed
            x += vx * dt
        } else {
            vx = 0f
        }
        y = groundY
    }

    override fun takeDamage(damage: Int, hitPointX: Float, hitPointY: Float, type: DamageType) {
        if (isDead) return

        // Front shield protects against frontal attacks unless hit from rear or explosion
        val attackFromRight = hitPointX > x
        val isFrontalHit = (facingRight && attackFromRight) || (!facingRight && !attackFromRight)

        if (isFrontalHit && type != DamageType.Explosion) {
            // Clang! Shield blocks
            audio.vibrate(30)
            pool.spawnDamageNumber(x, y - height * 0.6f, "DEFLECTED!", 0xFFB0BEC5.toInt())
            pool.spawnBurst(hitPointX, hitPointY, 8, 0xFFECEFF1.toInt(), 0.9f)
            return
        }

        super.takeDamage(damage, hitPointX, hitPointY, type)
    }

    override fun render(canvas: Canvas, camera: Camera2D, paint: Paint) {
        if (isDead) return
        val sx = camera.worldToScreenX(x)
        val sy = camera.worldToScreenY(y)

        canvas.save()
        canvas.translate(sx, sy)
        if (!facingRight) canvas.scale(-1f, 1f)

        // Body
        paint.color = if (hurtTimer > 0f) Color.WHITE else Color.rgb(38, 50, 56)
        paint.style = Paint.Style.FILL
        canvas.drawRect(-16f, -height, 16f, 0f, paint)

        // Front Heavy Ballistic Shield
        paint.color = Color.rgb(120, 144, 156)
        canvas.drawRect(14f, -height - 4f, 26f, 4f, paint)
        paint.color = Color.rgb(255, 193, 7)
        canvas.drawRect(18f, -height + 12f, 22f, -height + 28f, paint)

        canvas.restore()
    }
}

/**
 * Explosive Bug: Charges fast and detonates! (GDD Section 7)
 */
class ExplosiveBug(
    x: Float,
    y: Float,
    pool: ObjectPool,
    audio: GameAudio
) : EnemyBase(EnemyType.EXPLOSIVE_BUG, x, y, 18, pool, audio) {

    private val moveSpeed = 310f

    init {
        height = 32f
        width = 36f
    }

    override fun update(playerX: Float, playerY: Float, dt: Float, groundY: Float) {
        if (isDead) return
        val dist = abs(playerX - x)
        facingRight = playerX > x

        // Sprints rapidly towards player
        vx = if (facingRight) moveSpeed else -moveSpeed
        x += vx * dt
        y = groundY

        // Proximity detonation
        if (dist < 45f) {
            detonate()
        }
    }

    private fun detonate() {
        die()
        pool.spawnDamageNumber(x, y - 20f, "BOOM!", 0xFFFF3D00.toInt())
        pool.spawnBurst(x, y - 16f, 30, 0xFFFF3D00.toInt(), 2.0f)
    }

    override fun render(canvas: Canvas, camera: Camera2D, paint: Paint) {
        if (isDead) return
        val sx = camera.worldToScreenX(x)
        val sy = camera.worldToScreenY(y)

        canvas.save()
        canvas.translate(sx, sy)

        // Pulsing molten orange abdomen
        paint.color = if (hurtTimer > 0f) Color.WHITE else Color.rgb(255, 61, 0)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(0f, -16f, 15f, paint)

        // Glowing core
        paint.color = Color.rgb(255, 235, 59)
        canvas.drawCircle(0f, -16f, 8f, paint)

        // Spidery legs
        paint.color = Color.BLACK
        paint.strokeWidth = 3f
        canvas.drawLine(-12f, -8f, -22f, 0f, paint)
        canvas.drawLine(12f, -8f, 22f, 0f, paint)

        canvas.restore()
    }
}
