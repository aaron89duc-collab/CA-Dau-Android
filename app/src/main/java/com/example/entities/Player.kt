package com.example.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.audio.GameAudio
import com.example.data.SaveManager
import com.example.engine.Camera2D
import com.example.engine.ObjectPool
import com.example.model.DamageType
import com.example.model.IDamageable
import com.example.model.PlayerState
import com.example.model.PowerUpType
import com.example.model.ShieldThrowState
import kotlin.math.cos
import kotlin.math.sin

/**
 * Player Controller & Combat System
 * Follows GDD Sections 3, 4, 5 & 6
 */
class Player(
    private val pool: ObjectPool,
    private val audio: GameAudio,
    private val saveManager: SaveManager
) : IDamageable {

    var x: Float = 200f
    var y: Float = 750f // Ground level
    var vx: Float = 0f
    var vy: Float = 0f

    val width: Float = 48f
    val height: Float = 72f

    var state: PlayerState = PlayerState.Idle
    var facingRight: Boolean = true
    var isGrounded: Boolean = true
    private var jumpCount: Int = 0
    private val maxJumps: Int = 2

    // Stats from SaveManager
    var maxHp: Int = 100
    var hp: Int = 100
    var moveSpeed: Float = 360f
    var shieldMultiplier: Float = 1.0f
    var shieldThrowDamage: Int = 35
    var ultimateBaseDamage: Int = 150

    // Combat timers
    private var shootCooldownTimer: Float = 0f
    val shootCooldown: Float = 0.15f

    // Shield boomerang
    val shieldWeapon = ShieldProjectile(pool)

    // Blocking & Perfect Block (GDD Section 4.2)
    var isBlocking: Boolean = false
    var blockActivationTime: Float = 0f
    private val perfectBlockWindow: Float = 0.12f

    // Dash / Smash
    var isDashing: Boolean = false
    private var dashTimer: Float = 0f
    private var dashCooldownTimer: Float = 0f

    // Ultimate meter (0 to 100)
    var ultimateMeter: Float = 0f
    val maxUltimateMeter: Float = 100f
    var isUltimateActive: Boolean = false
    private var ultimateTimer: Float = 0f

    // Power-ups
    var activePowerUp: PowerUpType? = null
    var powerUpTimer: Float = 0f

    // Invulnerability
    var invulnerableTimer: Float = 0f
    var respawnTimer: Float = 0f
    var isRespawning: Boolean = false

    // Checkpoint
    var checkpointX: Float = 200f
    var checkpointY: Float = 750f

    // Animation & rendering
    private var animTimer: Float = 0f
    private val playerPath = Path()

    fun resetToStats() {
        val stats = saveManager.getCurrentStats()
        maxHp = stats.maxHp
        hp = maxHp
        moveSpeed = stats.moveSpeed * 68f
        shieldMultiplier = stats.shieldDamageMultiplier
        shieldThrowDamage = stats.shieldThrowDamage
        ultimateBaseDamage = stats.ultimateDamage
        jumpCount = 0
        ultimateMeter = 20f
        invulnerableTimer = 0f
        state = PlayerState.Idle
    }

    fun handleInput(
        moveAxisX: Float,
        moveAxisY: Float,
        isFireHeld: Boolean,
        isJumpPressed: Boolean,
        isShieldPressed: Boolean,
        isBlockHeld: Boolean,
        isUltimatePressed: Boolean,
        dt: Float
    ) {
        if (state == PlayerState.Dead || isRespawning) return

        // 1. Horizontal Movement
        if (!isDashing) {
            val deadzone = 0.15f
            if (kotlin.math.abs(moveAxisX) > deadzone) {
                vx = moveAxisX * moveSpeed
                facingRight = moveAxisX > 0f
                if (isGrounded && !isBlocking) {
                    state = PlayerState.Run
                }
            } else {
                vx = 0f
                if (isGrounded && !isBlocking) {
                    state = PlayerState.Idle
                }
            }
        }

        // Crouch check
        if (isGrounded && moveAxisY > 0.6f && !isDashing) {
            state = PlayerState.Crouch
            vx = 0f
        }

        // 2. Jumping & Double Jump (GDD Section 3: Double Jump mở sẵn)
        if (isJumpPressed) {
            if (isGrounded || jumpCount < maxJumps) {
                vy = -640f
                isGrounded = false
                jumpCount++
                state = PlayerState.Jump
                audio.playJump()
                pool.spawnBurst(x, y + height * 0.5f, 6, 0xFFFFFFFF.toInt(), 0.5f)
            }
        }

        // 3. Block / Perfect Block
        val prevBlocking = isBlocking
        isBlocking = isBlockHeld
        if (isBlocking && !prevBlocking) {
            blockActivationTime = 0f
            state = PlayerState.Block
        }

        // 4. Fire (Auto-fire when held, GDD Section 3.2: Cooldown 0.12-0.18s)
        if (isFireHeld && shootCooldownTimer <= 0f && !isBlocking) {
            fireShieldShot()
            shootCooldownTimer = shootCooldown
        }

        // 5. Shield Throw (Boomerang)
        if (isShieldPressed) {
            if (shieldWeapon.state == ShieldThrowState.IN_HAND) {
                val dmg = (shieldThrowDamage * shieldMultiplier).toInt()
                shieldWeapon.throwShield(x, y - height * 0.4f, facingRight, dmg)
                audio.playShieldThrow()
                state = PlayerState.ShieldThrow
            } else if (shieldWeapon.state == ShieldThrowState.OUTBOUND) {
                // Quick return recall
                shieldWeapon.turnAroundNow()
            }
        }

        // 6. Dash / Shield Smash
        if (moveAxisY < -0.7f && dashCooldownTimer <= 0f && !isDashing) {
            startDash()
        }

        // 7. Ultimate
        if (isUltimatePressed && ultimateMeter >= maxUltimateMeter && !isUltimateActive) {
            triggerUltimate()
        }
    }

    private fun startDash() {
        isDashing = true
        dashTimer = 0.22f
        dashCooldownTimer = 1.0f
        state = PlayerState.Dash
        vx = if (facingRight) moveSpeed * 2.4f else -moveSpeed * 2.4f
        vy = 0f
        audio.vibrate(60)
        pool.spawnBurst(x, y, 12, 0xFF00B4D8.toInt(), 1.2f)
    }

    private fun triggerUltimate() {
        isUltimateActive = true
        ultimateTimer = 0.8f
        ultimateMeter = 0f
        audio.playBossRoar()
        pool.spawnBurst(x, y, 40, 0xFFD90429.toInt(), 2.5f)
        pool.spawnBurst(x, y, 30, 0xFF00B4D8.toInt(), 2.0f)
        pool.spawnDamageNumber(x, y - 60f, "ULTIMATE SMASH!", 0xFFFFD700.toInt())
    }

    private fun fireShieldShot() {
        audio.playShieldShot()
        val spawnX = x + (if (facingRight) width * 0.6f else -width * 0.6f)
        val spawnY = y - height * 0.45f
        val bulletSpeed = 950f
        val bulletVx = if (facingRight) bulletSpeed else -bulletSpeed
        val dmg = (10 * shieldMultiplier).toInt()

        when (activePowerUp) {
            PowerUpType.DOUBLE_SHOT -> {
                pool.spawnProjectile(spawnX, spawnY - 8f, bulletVx, 0f, dmg + 5, DamageType.Normal, true, 8f, 0xFF00E5FF.toInt())
                pool.spawnProjectile(spawnX, spawnY + 8f, bulletVx, 0f, dmg + 5, DamageType.Normal, true, 8f, 0xFF00E5FF.toInt())
            }
            PowerUpType.TRIPLE_SHOT -> {
                pool.spawnProjectile(spawnX, spawnY - 12f, bulletVx, -80f, dmg + 8, DamageType.Normal, true, 8f, 0xFFFFB703.toInt())
                pool.spawnProjectile(spawnX, spawnY, bulletVx, 0f, dmg + 8, DamageType.Normal, true, 8f, 0xFFFFB703.toInt())
                pool.spawnProjectile(spawnX, spawnY + 12f, bulletVx, 80f, dmg + 8, DamageType.Normal, true, 8f, 0xFFFFB703.toInt())
            }
            PowerUpType.SUPER_SHIELD -> {
                pool.spawnProjectile(spawnX, spawnY, bulletVx, 0f, (dmg * 1.5f).toInt(), DamageType.Shield, true, 12f, 0xFFFF0055.toInt())
            }
            else -> {
                pool.spawnProjectile(spawnX, spawnY, bulletVx, 0f, dmg, DamageType.Normal, true, 8f, 0xFF00E5FF.toInt())
            }
        }
    }

    fun update(dt: Float, groundY: Float): Boolean {
        animTimer += dt

        if (shootCooldownTimer > 0f) shootCooldownTimer -= dt
        if (dashCooldownTimer > 0f) dashCooldownTimer -= dt
        if (invulnerableTimer > 0f) invulnerableTimer -= dt

        if (isBlocking) {
            blockActivationTime += dt
        }

        // Powerup countdown
        if (powerUpTimer > 0f) {
            powerUpTimer -= dt
            if (powerUpTimer <= 0f) {
                activePowerUp = null
            }
        }

        // Ultimate timer
        if (isUltimateActive) {
            ultimateTimer -= dt
            if (ultimateTimer <= 0f) {
                isUltimateActive = false
            }
        }

        // Respawning countdown
        if (isRespawning) {
            respawnTimer -= dt
            if (respawnTimer <= 0f) {
                isRespawning = false
                invulnerableTimer = 2.0f // 2s invulnerability after respawn (GDD Section 4)
                state = PlayerState.Idle
            }
            return false
        }

        // Dash handling
        if (isDashing) {
            dashTimer -= dt
            x += vx * dt
            if (dashTimer <= 0f) {
                isDashing = false
                vx = 0f
            }
        } else {
            // Normal Physics
            x += vx * dt

            // Gravity
            vy += 1500f * dt
            y += vy * dt

            if (y >= groundY) {
                y = groundY
                vy = 0f
                isGrounded = true
                jumpCount = 0
                if (!isBlocking && state != PlayerState.Run && state != PlayerState.Crouch) {
                    state = PlayerState.Idle
                }
            } else {
                isGrounded = false
                if (vy > 0 && state != PlayerState.Hurt) {
                    state = PlayerState.Fall
                }
            }
        }

        // Update Boomerang Shield
        val caught = shieldWeapon.update(x, y - height * 0.4f, dt)
        if (caught) {
            audio.playShieldCatch()
            pool.spawnBurst(x, y - height * 0.4f, 8, 0xFFEDF2F4.toInt(), 0.8f)
            state = PlayerState.ShieldCatch
        }

        return caught
    }

    override fun takeDamage(damage: Int, hitPointX: Float, hitPointY: Float, type: DamageType) {
        if (invulnerableTimer > 0f || state == PlayerState.Dead || isRespawning) return

        // Direction check for Block
        val attackFromRight = hitPointX > x
        val isFacingAttack = (facingRight && attackFromRight) || (!facingRight && !attackFromRight)

        if (isBlocking && isFacingAttack) {
            // Check Perfect Block (within 0.12s of activating block)
            if (blockActivationTime <= perfectBlockWindow) {
                // Perfect Block! Reflect, +10% ultimate meter, 0.05s hit-stop
                audio.playPerfectBlock()
                ultimateMeter = (ultimateMeter + 10f).coerceAtMost(maxUltimateMeter)
                pool.spawnDamageNumber(x, y - 50f, "PERFECT BLOCK!", 0xFF00FFCC.toInt())
                pool.spawnBurst(hitPointX, hitPointY, 15, 0xFF00FFCC.toInt(), 1.5f)

                // Deflect bullet back towards enemy
                val deflectVx = if (facingRight) 1100f else -1100f
                pool.spawnProjectile(x, y - 25f, deflectVx, 0f, 40, DamageType.Shield, true, 10f, 0xFF00FFCC.toInt())
                return
            } else {
                // Normal block - blocks 100% of front projectile damage!
                audio.vibrate(25)
                pool.spawnDamageNumber(x, y - 40f, "BLOCKED!", 0xFF80D8FF.toInt())
                pool.spawnBurst(hitPointX, hitPointY, 6, 0xFF80D8FF.toInt(), 0.8f)
                return
            }
        }

        // Damage received
        hp -= damage
        audio.vibrate(70)
        invulnerableTimer = 0.7f // GDD Section 3.2
        state = PlayerState.Hurt
        pool.spawnDamageNumber(x, y - 30f, "-$damage", 0xFFFF3366.toInt())
        pool.spawnBurst(x, y - 20f, 10, 0xFFFF3366.toInt(), 1f)

        if (hp <= 0) {
            hp = 0
            dieAndRespawn()
        }
    }

    private fun dieAndRespawn() {
        state = PlayerState.Dead
        isRespawning = true
        respawnTimer = 1.2f // Brief death/respawn transition
        audio.playExplosion()
        pool.spawnBurst(x, y, 25, 0xFFFF1744.toInt(), 1.5f)

        // Respawn at latest checkpoint (GDD Section 6)
        x = checkpointX
        y = checkpointY
        hp = maxHp
        vx = 0f
        vy = 0f
        shieldWeapon.state = ShieldThrowState.IN_HAND
    }

    fun applyPowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.HEALTH -> {
                hp = (hp + 30).coerceAtMost(maxHp)
                pool.spawnDamageNumber(x, y - 40f, "+30 HP", 0xFF00E676.toInt())
            }
            PowerUpType.ULTIMATE -> {
                ultimateMeter = (ultimateMeter + 50f).coerceAtMost(maxUltimateMeter)
                pool.spawnDamageNumber(x, y - 40f, "+50% ULTIMATE", 0xFFFFD700.toInt())
            }
            PowerUpType.COIN -> {
                saveManager.addCoins(10)
                pool.spawnDamageNumber(x, y - 40f, "+10 COIN", 0xFFFFD700.toInt())
            }
            PowerUpType.SUPER_SHIELD -> {
                activePowerUp = type
                powerUpTimer = 30f
                pool.spawnDamageNumber(x, y - 40f, "SUPER SHIELD!", 0xFFFF1744.toInt())
            }
            PowerUpType.DOUBLE_SHOT -> {
                activePowerUp = type
                powerUpTimer = 30f
                pool.spawnDamageNumber(x, y - 40f, "DOUBLE SHOT!", 0xFF00E5FF.toInt())
            }
            PowerUpType.TRIPLE_SHOT -> {
                activePowerUp = type
                powerUpTimer = 30f
                pool.spawnDamageNumber(x, y - 40f, "TRIPLE SHOT!", 0xFFFFB703.toInt())
            }
            PowerUpType.BOMB_SHIELD -> {
                activePowerUp = type
                powerUpTimer = 20f
                shieldWeapon.hasBombEffect = true
                pool.spawnDamageNumber(x, y - 40f, "BOMB SHIELD!", 0xFFFF5722.toInt())
            }
            PowerUpType.PLASMA_SHIELD -> {
                activePowerUp = type
                powerUpTimer = 25f
                shieldWeapon.hasPlasmaEffect = true
                pool.spawnDamageNumber(x, y - 40f, "PLASMA SHIELD!", 0xFF9C27B0.toInt())
            }
        }
    }

    fun render(canvas: Canvas, camera: Camera2D, paint: Paint) {
        if (isRespawning) return

        // Invulnerability flicker
        if (invulnerableTimer > 0f && ((invulnerableTimer * 20).toInt() % 2 == 0)) {
            return
        }

        val sx = camera.worldToScreenX(x)
        val sy = camera.worldToScreenY(y)

        canvas.save()
        canvas.translate(sx, sy)
        if (!facingRight) {
            canvas.scale(-1f, 1f)
        }

        // Draw Player Body (Heroic Suit - Navy, Red & Silver Stripes)
        val drawHeight = if (state == PlayerState.Crouch) height * 0.65f else height
        val topY = -drawHeight

        // Head & Mask
        paint.color = Color.rgb(0, 119, 182) // Shield Blue
        paint.style = Paint.Style.FILL
        canvas.drawCircle(0f, topY + 12f, 14f, paint)

        // Silver Helmet 'A' / Wing markings
        paint.color = Color.WHITE
        canvas.drawCircle(3f, topY + 9f, 4f, paint)

        // Torso / Suit
        paint.color = Color.rgb(11, 19, 43) // Deep Navy
        canvas.drawRect(-16f, topY + 22f, 16f, 0f, paint)

        // Red & White Torso Stripes
        paint.color = Color.rgb(217, 4, 41)
        canvas.drawRect(-14f, topY + 36f, -6f, -4f, paint)
        paint.color = Color.WHITE
        canvas.drawRect(-6f, topY + 36f, 2f, -4f, paint)
        paint.color = Color.rgb(217, 4, 41)
        canvas.drawRect(2f, topY + 36f, 10f, -4f, paint)

        // Silver Star on chest
        paint.color = Color.WHITE
        canvas.drawCircle(0f, topY + 28f, 5f, paint)

        // Legs / Boots
        paint.color = Color.rgb(217, 4, 41) // Red boots
        val legAnimOffset = if (state == PlayerState.Run) (sin(animTimer * 16.0) * 8f).toFloat() else 0f
        canvas.drawRect(-14f, -4f, -4f, 6f + legAnimOffset, paint)
        canvas.drawRect(4f, -4f, 14f, 6f - legAnimOffset, paint)

        // Arm / Shield in hand
        if (shieldWeapon.state == ShieldThrowState.IN_HAND) {
            if (isBlocking) {
                // Shield held firmly forward with blue forcefield
                paint.color = 0x4400B4D8
                canvas.drawCircle(22f, topY + 30f, 32f, paint)

                paint.color = Color.rgb(217, 4, 41)
                canvas.drawCircle(22f, topY + 30f, 20f, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(22f, topY + 30f, 15f, paint)
                paint.color = Color.rgb(0, 119, 182)
                canvas.drawCircle(22f, topY + 30f, 8f, paint)
            } else {
                // Shield strapped to arm
                paint.color = Color.rgb(217, 4, 41)
                canvas.drawCircle(12f, topY + 32f, 16f, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(12f, topY + 32f, 12f, paint)
                paint.color = Color.rgb(0, 119, 182)
                canvas.drawCircle(12f, topY + 32f, 6f, paint)
            }
        }

        canvas.restore()

        // Render traveling shield boomerang
        shieldWeapon.render(canvas, camera, paint)
    }
}
