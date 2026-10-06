package com.example.engine

import com.example.model.DamageType
import com.example.model.PowerUpType

/**
 * Pre-allocated Projectile entity with pooling support.
 */
class Projectile {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var radius: Float = 8f
    var damage: Int = 10
    var damageType: DamageType = DamageType.Normal
    var isFromPlayer: Boolean = true
    var lifetime: Float = 0f
    var maxLifetime: Float = 3f
    var isMissile: Boolean = false
    var isShockwave: Boolean = false
    var color: Int = 0xFF00E5FF.toInt()

    fun reset() {
        active = false
        x = 0f
        y = 0f
        vx = 0f
        vy = 0f
        radius = 8f
        damage = 10
        damageType = DamageType.Normal
        isFromPlayer = true
        lifetime = 0f
        maxLifetime = 3f
        isMissile = false
        isShockwave = false
        color = 0xFF00E5FF.toInt()
    }
}

/**
 * Visual particle for hit sparks, explosions, smoke, and shield trail.
 */
class Particle {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var size: Float = 4f
    var life: Float = 0f
    var maxLife: Float = 0.5f
    var color: Int = 0xFFFFD700.toInt()

    fun reset() {
        active = false
        x = 0f
        y = 0f
        vx = 0f
        vy = 0f
        size = 4f
        life = 0f
        maxLife = 0.5f
        color = 0xFFFFD700.toInt()
    }
}

/**
 * Floating combat text to give great feedback (hit dmg, perfect block, crit).
 */
class DamageNumber {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var text: String = ""
    var color: Int = 0xFFFFFFFF.toInt()
    var life: Float = 0f
    var maxLife: Float = 0.7f
    var vy: Float = -60f

    fun reset() {
        active = false
        x = 0f
        y = 0f
        text = ""
        color = 0xFFFFFFFF.toInt()
        life = 0f
        maxLife = 0.7f
        vy = -60f
    }
}

/**
 * Pickup entity (Coin, Health pack, Power-up)
 */
class PickupItem {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vy: Float = 0f
    var type: PowerUpType = PowerUpType.COIN
    var radius: Float = 16f
    var bobTimer: Float = 0f

    fun reset() {
        active = false
        x = 0f
        y = 0f
        vy = 0f
        type = PowerUpType.COIN
        radius = 16f
        bobTimer = 0f
    }
}

/**
 * Exploding Barrel hazard (GDD Section 9 & 11 Segment S2)
 */
class ExplodingBarrel {
    var active: Boolean = true
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 44f
    var height: Float = 60f
    var hp: Int = 20
    var isExploded: Boolean = false
    var fuseTimer: Float = 0f

    fun reset(posX: Float, posY: Float) {
        active = true
        x = posX
        y = posY
        width = 44f
        height = 60f
        hp = 20
        isExploded = false
        fuseTimer = 0f
    }
}

/**
 * Centralized High-Performance Object Pools.
 * Zero-allocation during active combat loop.
 */
class ObjectPool {
    val projectiles: Array<Projectile> = Array(120) { Projectile() }
    val particles: Array<Particle> = Array(250) { Particle() }
    val damageNumbers: Array<DamageNumber> = Array(60) { DamageNumber() }
    val pickups: Array<PickupItem> = Array(40) { PickupItem() }

    fun spawnProjectile(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        damage: Int,
        damageType: DamageType,
        isFromPlayer: Boolean,
        radius: Float = 8f,
        color: Int = 0xFF00E5FF.toInt(),
        isMissile: Boolean = false,
        isShockwave: Boolean = false,
        maxLife: Float = 3f
    ): Projectile? {
        val size = projectiles.size
        for (i in 0 until size) {
            val p = projectiles[i]
            if (!p.active) {
                p.active = true
                p.x = x
                p.y = y
                p.vx = vx
                p.vy = vy
                p.damage = damage
                p.damageType = damageType
                p.isFromPlayer = isFromPlayer
                p.radius = radius
                p.color = color
                p.isMissile = isMissile
                p.isShockwave = isShockwave
                p.lifetime = 0f
                p.maxLifetime = maxLife
                return p
            }
        }
        return null
    }

    fun spawnParticle(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        size: Float,
        maxLife: Float,
        color: Int
    ) {
        val count = particles.size
        for (i in 0 until count) {
            val pt = particles[i]
            if (!pt.active) {
                pt.active = true
                pt.x = x
                pt.y = y
                pt.vx = vx
                pt.vy = vy
                pt.size = size
                pt.life = 0f
                pt.maxLife = maxLife
                pt.color = color
                return
            }
        }
    }

    fun spawnBurst(x: Float, y: Float, count: Int, baseColor: Int, speedScale: Float = 1f) {
        for (i in 0 until count) {
            val angle = (Math.PI * 2.0 * Math.random()).toFloat()
            val speed = (40f + Math.random().toFloat() * 160f) * speedScale
            val vx = (kotlin.math.cos(angle.toDouble()) * speed).toFloat()
            val vy = (kotlin.math.sin(angle.toDouble()) * speed).toFloat()
            val life = 0.25f + Math.random().toFloat() * 0.35f
            val size = 3f + Math.random().toFloat() * 4f
            spawnParticle(x, y, vx, vy, size, life, baseColor)
        }
    }

    fun spawnDamageNumber(x: Float, y: Float, text: String, color: Int) {
        val size = damageNumbers.size
        for (i in 0 until size) {
            val d = damageNumbers[i]
            if (!d.active) {
                d.active = true
                d.x = x
                d.y = y
                d.text = text
                d.color = color
                d.life = 0f
                d.maxLife = 0.7f
                d.vy = -75f
                return
            }
        }
    }

    fun spawnPickup(x: Float, y: Float, type: PowerUpType) {
        val size = pickups.size
        for (i in 0 until size) {
            val item = pickups[i]
            if (!item.active) {
                item.active = true
                item.x = x
                item.y = y
                item.vy = -180f // pop up then land
                item.type = type
                item.radius = 16f
                item.bobTimer = 0f
                return
            }
        }
    }

    fun clearAll() {
        for (p in projectiles) p.reset()
        for (pt in particles) pt.reset()
        for (d in damageNumbers) d.reset()
        for (pi in pickups) pi.reset()
    }
}
