package com.example.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.audio.GameAudio
import com.example.data.SaveManager
import com.example.entities.BossIronBeast
import com.example.entities.EnemyBase
import com.example.entities.ExplosiveBug
import com.example.entities.FlyingDrone
import com.example.entities.LevelEnvironment
import com.example.entities.MutantSoldier
import com.example.entities.Platform
import com.example.entities.Player
import com.example.entities.ShieldSoldier
import com.example.model.BossPhase
import com.example.model.DamageType
import com.example.model.PlayerState
import com.example.model.ShieldThrowState
import com.example.unity.UnityBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

data class GameSessionStats(
    val elapsedTimeSeconds: Float = 0f,
    val coinsCollected: Int = 0,
    val enemiesDefeated: Int = 0,
    val deaths: Int = 0,
    val currentSegment: String = "Start"
)

/**
 * Core Game Engine Simulation Loop
 * Fulfills GDD Sections 2, 7, 10, 11, 15, 19
 */
class GameEngine(
    val context: Context
) {
    val pool = ObjectPool()
    val audio = GameAudio.getInstance(context)
    val saveManager = SaveManager.getInstance(context)

    val camera = Camera2D()
    val levelEnv = LevelEnvironment(pool, audio)
    val player = Player(pool, audio, saveManager)
    val boss = BossIronBeast(8000f, levelEnv.groundY, pool, audio)

    val enemies = ArrayList<EnemyBase>()
    private val spawnedSegments = HashSet<String>()

    // Game states
    var isPaused: Boolean = false
    var isVictory: Boolean = false

    private val _statsFlow = MutableStateFlow(GameSessionStats())
    val statsFlow: StateFlow<GameSessionStats> = _statsFlow.asStateFlow()

    private var sessionTime: Float = 0f
    private var sessionCoins: Int = 0
    private var sessionDefeated: Int = 0
    private var sessionDeaths: Int = 0
    private var currentSegmentName: String = "Tutorial"

    // Boss Arena Trigger
    private val bossGateX = 7200f
    var isBossFightActive: Boolean = false
        private set

    // Touch inputs
    var inputMoveX: Float = 0f
    var inputMoveY: Float = 0f
    var inputFireHeld: Boolean = false
    var inputJumpPressed: Boolean = false
    var inputShieldPressed: Boolean = false
    var inputBlockHeld: Boolean = false
    var inputUltimatePressed: Boolean = false

    // Rendering Paints
    private val renderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        isFakeBoldText = true
    }

    init {
        resetLevel()
    }

    fun resetLevel() {
        pool.clearAll()
        enemies.clear()
        spawnedSegments.clear()
        sessionTime = 0f
        sessionCoins = 0
        sessionDefeated = 0
        sessionDeaths = 0
        isPaused = false
        isVictory = false
        isBossFightActive = false
        camera.isLockedToBossArena = false

        player.resetToStats()
        player.x = 200f
        player.y = levelEnv.groundY
        player.checkpointX = 200f
        player.checkpointY = levelEnv.groundY

        boss.x = 8000f
        boss.y = levelEnv.groundY
        boss.hp = boss.maxHp
        boss.phase = BossPhase.PHASE_1
        boss.isDead = false

        spawnInitialSegment()
        updateStats()
    }

    private fun spawnInitialSegment() {
        spawnedSegments.add("Start")
        currentSegmentName = "Start: Tutorial Movement"
        // 2 Mutant Soldiers (GDD Section 11)
        enemies.add(MutantSoldier(550f, levelEnv.groundY, pool, audio))
        enemies.add(MutantSoldier(750f, levelEnv.groundY, pool, audio))
    }

    fun update(dt: Float) {
        if (isPaused || isVictory) return

        sessionTime += dt

        // 1. Player Input & Update
        player.handleInput(
            inputMoveX, inputMoveY,
            inputFireHeld, inputJumpPressed, inputShieldPressed,
            inputBlockHeld, inputUltimatePressed,
            dt
        )
        // Reset single-frame tap inputs
        inputJumpPressed = false
        inputShieldPressed = false
        inputUltimatePressed = false

        // Determine ground level for player based on platforms
        val currentGroundY = getPlatformGroundY(player.x, player.y)
        player.update(dt, currentGroundY)

        // 2. Camera Tracking
        camera.update(player.x, player.y, dt)

        // 3. Checkpoint Check (GDD Section 11: Checkpoint between S2 and S3)
        if (levelEnv.checkpoint.checkTrigger(player.x, player.y)) {
            player.checkpointX = levelEnv.checkpoint.x
            player.checkpointY = levelEnv.groundY
            audio.playPickup()
            audio.vibrate(50)
            pool.spawnDamageNumber(player.x, player.y - 70f, "CHECKPOINT SAVED!", 0xFF00E676.toInt())
        }

        // 4. Progressive Segment Spawning (Vertical Slice Level 1)
        checkSegmentProgression()

        // 5. Boss Arena Lock & Activation (GDD Section 11)
        if (!isBossFightActive && player.x >= bossGateX) {
            isBossFightActive = true
            camera.isLockedToBossArena = true
            camera.bossArenaMinX = 7000f
            camera.bossArenaMaxX = 9200f
            audio.playBossRoar()
            camera.triggerShake(0.3f, 15f)
            pool.spawnDamageNumber(player.x, player.y - 80f, "BOSS ENCOUNTER: IRON BEAST!", 0xFFFF1744.toInt())
        }

        // 6. Update Boss if active
        if (isBossFightActive && !boss.isDead) {
            boss.update(player.x, player.y, dt, levelEnv.groundY)
            if (boss.isDead) {
                isVictory = true
                saveManager.setLevelCompleted(1)
                saveManager.addCoins(200) // Level completion bonus
            }
        }

        // 7. Update Enemies
        val enemyCount = enemies.size
        for (i in enemyCount - 1 downTo 0) {
            val enemy = enemies[i]
            val eGround = getPlatformGroundY(enemy.x, enemy.y)
            enemy.update(player.x, player.y, dt, eGround)

            // Enemy melee touch attack against player
            if (!enemy.isDead && abs(enemy.x - player.x) < 40f && abs(enemy.y - player.y) < 55f) {
                player.takeDamage(10, enemy.x, enemy.y, DamageType.Melee)
            }

            if (enemy.isDead) {
                sessionDefeated++
                enemies.removeAt(i)
            }
        }

        // 8. Update Projectiles & Collisions
        updateProjectiles(dt)

        // 9. Update Shield Boomerang Collisions
        updateShieldCollisions()

        // 10. Update Particles & Pickups
        updateParticles(dt)
        updatePickups(dt)

        // 11. Exploding Barrels AoE Check
        checkBarrels()

        // Sync Stats
        updateStats()
    }

    private fun getPlatformGroundY(px: Float, py: Float): Float {
        var highestGround = levelEnv.groundY
        for (plat in levelEnv.platforms) {
            if (px in plat.left..plat.right && py <= plat.top + 15f) {
                if (plat.top < highestGround) {
                    highestGround = plat.top
                }
            }
        }
        return highestGround
    }

    private fun checkSegmentProgression() {
        val px = player.x

        // Segment 1: Đường phố (3 Mutants + 1 Drone)
        if (px >= 850f && !spawnedSegments.contains("S1")) {
            spawnedSegments.add("S1")
            currentSegmentName = "S1: City Street"
            enemies.add(MutantSoldier(1200f, levelEnv.groundY, pool, audio))
            enemies.add(MutantSoldier(1450f, levelEnv.groundY, pool, audio))
            enemies.add(MutantSoldier(1700f, levelEnv.groundY, pool, audio))
            enemies.add(FlyingDrone(1500f, levelEnv.groundY - 180f, pool, audio))
        }

        // Segment 2: Thùng nổ (2 Shield Soldier + 2 Mutant)
        if (px >= 2000f && !spawnedSegments.contains("S2")) {
            spawnedSegments.add("S2")
            currentSegmentName = "S2: Explosive Barrels"
            enemies.add(ShieldSoldier(2300f, levelEnv.groundY, pool, audio))
            enemies.add(MutantSoldier(2550f, levelEnv.groundY, pool, audio))
            enemies.add(ShieldSoldier(2900f, levelEnv.groundY, pool, audio))
            enemies.add(MutantSoldier(3100f, levelEnv.groundY, pool, audio))
        }

        // Segment 3: Cầu vượt (4 Mutant + 2 Drone)
        if (px >= 3600f && !spawnedSegments.contains("S3")) {
            spawnedSegments.add("S3")
            currentSegmentName = "S3: Elevated Highway"
            enemies.add(MutantSoldier(4000f, levelEnv.groundY - 180f, pool, audio))
            enemies.add(MutantSoldier(4300f, levelEnv.groundY - 180f, pool, audio))
            enemies.add(FlyingDrone(4150f, levelEnv.groundY - 320f, pool, audio))
            enemies.add(MutantSoldier(4800f, levelEnv.groundY - 220f, pool, audio))
            enemies.add(MutantSoldier(5100f, levelEnv.groundY - 220f, pool, audio))
            enemies.add(FlyingDrone(5000f, levelEnv.groundY - 340f, pool, audio))
        }

        // Segment 4: Mini encounter (Shield Soldier + Drone + Explosive Bug)
        if (px >= 5600f && !spawnedSegments.contains("S4")) {
            spawnedSegments.add("S4")
            currentSegmentName = "S4: Vanguard Ambush"
            enemies.add(ShieldSoldier(6100f, levelEnv.groundY, pool, audio))
            enemies.add(FlyingDrone(6200f, levelEnv.groundY - 200f, pool, audio))
            enemies.add(ExplosiveBug(6400f, levelEnv.groundY, pool, audio))
        }
    }

    private fun updateProjectiles(dt: Float) {
        val count = pool.projectiles.size
        for (i in 0 until count) {
            val p = pool.projectiles[i]
            if (!p.active) continue

            p.lifetime += dt
            if (p.lifetime >= p.maxLifetime) {
                p.active = false
                continue
            }

            // Missile physics: arc towards player or ground
            if (p.isMissile) {
                p.vy += 700f * dt
            }

            p.x += p.vx * dt
            p.y += p.vy * dt

            // Check projectile hit ground
            if (p.y >= levelEnv.groundY) {
                p.active = false
                pool.spawnBurst(p.x, levelEnv.groundY, 8, p.color, 0.7f)
                if (p.damageType == DamageType.Explosion) {
                    audio.playExplosion()
                    camera.triggerShake(0.12f, 8f)
                }
                continue
            }

            if (p.isFromPlayer) {
                // Hits enemy or barrel or boss
                for (enemy in enemies) {
                    if (!enemy.isDead && abs(p.x - enemy.x) < (p.radius + enemy.width * 0.5f) &&
                        abs(p.y - (enemy.y - enemy.height * 0.5f)) < (p.radius + enemy.height * 0.5f)
                    ) {
                        enemy.takeDamage(p.damage, p.x, p.y, p.damageType)
                        p.active = false
                        break
                    }
                }

                // Hits Boss
                if (p.active && isBossFightActive && !boss.isDead) {
                    if (abs(p.x - boss.x) < (p.radius + boss.width * 0.5f) &&
                        abs(p.y - (boss.y - boss.height * 0.5f)) < (p.radius + boss.height * 0.5f)
                    ) {
                        boss.takeDamage(p.damage, p.x, p.y, p.damageType)
                        p.active = false
                    }
                }

                // Hits Barrel
                if (p.active) {
                    for (b in levelEnv.barrels) {
                        if (b.active && !b.isExploded &&
                            p.x >= b.x && p.x <= b.x + b.width &&
                            p.y >= b.y && p.y <= b.y + b.height
                        ) {
                            levelEnv.damageBarrel(b, p.damage)
                            p.active = false
                            break
                        }
                    }
                }
            } else {
                // Enemy projectile hits Player
                if (abs(p.x - player.x) < (p.radius + player.width * 0.5f) &&
                    abs(p.y - (player.y - player.height * 0.5f)) < (p.radius + player.height * 0.5f)
                ) {
                    player.takeDamage(p.damage, p.x, p.y, p.damageType)
                    p.active = false
                }
            }
        }
    }

    private fun updateShieldCollisions() {
        val s = player.shieldWeapon
        if (s.state == ShieldThrowState.IN_HAND) return

        // 1. Shield hits enemies (Piercing Boomerang!)
        for (enemy in enemies) {
            if (!enemy.isDead && !s.hitEnemiesThisPass.contains(enemy.id)) {
                val dx = s.x - enemy.x
                val dy = s.y - (enemy.y - enemy.height * 0.5f)
                if (sqrt(dx * dx + dy * dy) < (s.radius + enemy.width * 0.5f)) {
                    s.hitEnemiesThisPass.add(enemy.id)
                    enemy.takeDamage(s.baseDamage, s.x, s.y, DamageType.Shield)
                    audio.vibrate(35)
                    pool.spawnBurst(s.x, s.y, 10, 0xFF00B4D8.toInt(), 1f)

                    if (s.hasBombEffect) {
                        audio.playExplosion()
                        pool.spawnBurst(s.x, s.y, 25, 0xFFFF3D00.toInt(), 1.8f)
                    }
                }
            }
        }

        // 2. Shield hits Boss
        if (isBossFightActive && !boss.isDead) {
            val bdx = s.x - boss.x
            val bdy = s.y - (boss.y - boss.height * 0.5f)
            if (sqrt(bdx * bdx + bdy * bdy) < (s.radius + boss.width * 0.5f)) {
                if (!s.hitEnemiesThisPass.contains(-999)) {
                    s.hitEnemiesThisPass.add(-999)
                    boss.takeDamage(s.baseDamage, s.x, s.y, DamageType.Shield)
                    audio.vibrate(45)
                    s.turnAroundNow() // Bounce back upon hitting massive mech!
                }
            }
        }

        // 3. Shield hits Barrels
        for (b in levelEnv.barrels) {
            if (b.active && !b.isExploded &&
                s.x >= b.x - s.radius && s.x <= b.x + b.width + s.radius &&
                s.y >= b.y - s.radius && s.y <= b.y + b.height + s.radius
            ) {
                levelEnv.damageBarrel(b, s.baseDamage)
            }
        }
    }

    private fun checkBarrels() {
        for (b in levelEnv.barrels) {
            if (b.isExploded) {
                val centerX = b.x + b.width * 0.5f
                val centerY = b.y + b.height * 0.5f
                val aoeRadius = 240f

                // Damage surrounding enemies
                for (enemy in enemies) {
                    if (!enemy.isDead) {
                        val dist = abs(enemy.x - centerX)
                        if (dist < aoeRadius) {
                            enemy.takeDamage(80, centerX, centerY, DamageType.Explosion)
                        }
                    }
                }
                b.isExploded = false // Exploded event consumed
            }
        }
    }

    private fun updateParticles(dt: Float) {
        for (pt in pool.particles) {
            if (!pt.active) continue
            pt.life += dt
            if (pt.life >= pt.maxLife) {
                pt.active = false
                continue
            }
            pt.x += pt.vx * dt
            pt.y += pt.vy * dt
        }

        for (d in pool.damageNumbers) {
            if (!d.active) continue
            d.life += dt
            if (d.life >= d.maxLife) {
                d.active = false
                continue
            }
            d.y += d.vy * dt
        }
    }

    private fun updatePickups(dt: Float) {
        for (item in pool.pickups) {
            if (!item.active) continue

            item.bobTimer += dt
            item.vy += 800f * dt
            item.y += item.vy * dt

            if (item.y >= levelEnv.groundY - 16f) {
                item.y = levelEnv.groundY - 16f
                item.vy = 0f
            }

            // Check player pickup
            if (abs(item.x - player.x) < 45f && abs(item.y - (player.y - player.height * 0.5f)) < 55f) {
                audio.playPickup()
                player.applyPowerUp(item.type)
                sessionCoins += 10
                item.active = false
            }
        }
    }

    private fun updateStats() {
        _statsFlow.value = GameSessionStats(
            elapsedTimeSeconds = sessionTime,
            coinsCollected = sessionCoins,
            enemiesDefeated = sessionDefeated,
            deaths = sessionDeaths,
            currentSegment = currentSegmentName
        )
    }

    /**
     * Renders complete game frame.
     * High-performance Canvas render with zero runtime allocations.
     */
    fun render(canvas: Canvas) {
        // 1. Render 3D Parallax City Backdrop & Platforms
        levelEnv.renderBackground(canvas, camera, renderPaint)

        // 2. Render Pickups
        for (item in pool.pickups) {
            if (!item.active) continue
            val sx = camera.worldToScreenX(item.x)
            val sy = camera.worldToScreenY(item.y)
            renderPaint.color = 0xFFFFD700.toInt()
            renderPaint.style = Paint.Style.FILL
            canvas.drawCircle(sx, sy, item.radius, renderPaint)
            renderPaint.color = Color.BLACK
            canvas.drawText("★", sx - 8f, sy + 8f, renderPaint)
        }

        // 3. Render Enemies
        for (enemy in enemies) {
            if (!enemy.isDead) {
                enemy.render(canvas, camera, renderPaint)
            }
        }

        // 4. Render Boss
        if (isBossFightActive) {
            boss.render(canvas, camera, renderPaint)
        }

        // 5. Render Player & Shield Boomerang
        player.render(canvas, camera, renderPaint)

        // 6. Render Projectiles
        for (p in pool.projectiles) {
            if (!p.active) continue
            val sx = camera.worldToScreenX(p.x)
            val sy = camera.worldToScreenY(p.y)
            renderPaint.color = p.color
            renderPaint.style = Paint.Style.FILL
            canvas.drawCircle(sx, sy, p.radius, renderPaint)
        }

        // 7. Render Particles
        for (pt in pool.particles) {
            if (!pt.active) continue
            val sx = camera.worldToScreenX(pt.x)
            val sy = camera.worldToScreenY(pt.y)
            renderPaint.color = pt.color
            canvas.drawCircle(sx, sy, pt.size * (1f - pt.life / pt.maxLife), renderPaint)
        }

        // 8. Render Floating Damage Numbers
        for (d in pool.damageNumbers) {
            if (!d.active) continue
            val sx = camera.worldToScreenX(d.x)
            val sy = camera.worldToScreenY(d.y)
            val alpha = ((1f - d.life / d.maxLife) * 255).toInt().coerceIn(0, 255)
            textPaint.color = d.color
            textPaint.alpha = alpha
            canvas.drawText(d.text, sx, sy, textPaint)
        }
    }
}
