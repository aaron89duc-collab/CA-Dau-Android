package com.example.model

/**
 * Script Contracts based on GDD Section 16
 */
interface IDamageable {
    fun takeDamage(damage: Int, hitPointX: Float, hitPointY: Float, type: DamageType)
}

enum class DamageType {
    Normal,
    Shield,
    Explosion,
    Plasma,
    Fire,
    Ice,
    Melee
}

enum class PlayerState {
    Idle,
    Run,
    Jump,
    Fall,
    Crouch,
    Shoot,
    ShieldThrow,
    ShieldCatch,
    Block,
    Dash,
    Hurt,
    Dead,
    Respawn,
    Victory
}

enum class ShieldThrowState {
    IN_HAND,
    OUTBOUND,
    MAX_DISTANCE,
    RETURN,
    CATCH
}

enum class PowerUpType {
    SUPER_SHIELD, // +50% damage (30s)
    DOUBLE_SHOT,  // 2 projectiles (30s)
    TRIPLE_SHOT,  // 3 projectiles (30s)
    PLASMA_SHIELD,// Pierce 3 enemies (25s)
    BOMB_SHIELD,  // Shield throw has AoE (20s)
    HEALTH,       // +30 HP (Instant)
    ULTIMATE,     // +50% meter (Instant)
    COIN          // +10 coins (Instant)
}

enum class EnemyType {
    MUTANT_SOLDIER,
    FLYING_DRONE,
    SHIELD_SOLDIER,
    EXPLOSIVE_BUG,
    GIANT_RAT
}

enum class BossPhase {
    PHASE_1,
    PHASE_2,
    DEFEATED
}

data class LevelInfo(
    val id: Int,
    val name: String,
    val environment: String,
    val mainEnemies: String,
    val bossName: String,
    val specialMechanic: String,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false
)
