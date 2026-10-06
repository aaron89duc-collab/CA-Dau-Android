package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UpgradeStats(
    val maxHp: Int,
    val shieldDamageMultiplier: Float,
    val moveSpeed: Float,
    val shieldThrowDamage: Int,
    val ultimateDamage: Int
)

class SaveManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("shield_force_save_data", Context.MODE_PRIVATE)

    private val _coinsFlow = MutableStateFlow(getCoins())
    val coinsFlow: StateFlow<Int> = _coinsFlow.asStateFlow()

    private val _highestLevelFlow = MutableStateFlow(getHighestUnlockedLevel())
    val highestLevelFlow: StateFlow<Int> = _highestLevelFlow.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: SaveManager? = null

        fun getInstance(context: Context): SaveManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SaveManager(context).also { INSTANCE = it }
            }
        }

        // Upgrades table from GDD Section 12
        val HP_VALUES = intArrayOf(100, 110, 120, 135, 150)
        val SHIELD_DAMAGE_VALUES = floatArrayOf(1.0f, 1.10f, 1.25f, 1.45f, 1.70f)
        val SPEED_VALUES = floatArrayOf(5.0f, 5.2f, 5.4f, 5.6f, 5.8f)
        val SHIELD_THROW_VALUES = intArrayOf(35, 40, 46, 54, 65)
        val ULTIMATE_VALUES = intArrayOf(150, 170, 195, 225, 260)

        val UPGRADE_COSTS = intArrayOf(0, 120, 220, 350, 500)
    }

    fun getCoins(): Int = prefs.getInt("coins", 150) // Starter bonus coins

    fun addCoins(amount: Int) {
        val updated = getCoins() + amount
        prefs.edit().putInt("coins", updated).apply()
        _coinsFlow.value = updated
    }

    fun spendCoins(amount: Int): Boolean {
        val current = getCoins()
        if (current >= amount) {
            val updated = current - amount
            prefs.edit().putInt("coins", updated).apply()
            _coinsFlow.value = updated
            return true
        }
        return false
    }

    fun getHighestUnlockedLevel(): Int = prefs.getInt("highestUnlockedLevel", 1)

    fun unlockLevel(level: Int) {
        val current = getHighestUnlockedLevel()
        if (level > current) {
            prefs.edit().putInt("highestUnlockedLevel", level).apply()
            _highestLevelFlow.value = level
        }
    }

    fun isLevelCompleted(level: Int): Boolean = prefs.getBoolean("level_${level}_completed", false)

    fun setLevelCompleted(level: Int) {
        prefs.edit().putBoolean("level_${level}_completed", true).apply()
        unlockLevel(level + 1)
    }

    // Upgrade Levels (1 to 5)
    fun getHpUpgradeLevel(): Int = prefs.getInt("hpUpgrade", 1)
    fun getShieldUpgradeLevel(): Int = prefs.getInt("shieldUpgrade", 1)
    fun getSpeedUpgradeLevel(): Int = prefs.getInt("speedUpgrade", 1)
    fun getShieldThrowUpgradeLevel(): Int = prefs.getInt("shieldThrowUpgrade", 1)
    fun getUltimateUpgradeLevel(): Int = prefs.getInt("ultimateUpgrade", 1)

    fun upgradeHp(): Boolean {
        val current = getHpUpgradeLevel()
        if (current < 5 && spendCoins(UPGRADE_COSTS[current])) {
            prefs.edit().putInt("hpUpgrade", current + 1).apply()
            return true
        }
        return false
    }

    fun upgradeShield(): Boolean {
        val current = getShieldUpgradeLevel()
        if (current < 5 && spendCoins(UPGRADE_COSTS[current])) {
            prefs.edit().putInt("shieldUpgrade", current + 1).apply()
            return true
        }
        return false
    }

    fun upgradeSpeed(): Boolean {
        val current = getSpeedUpgradeLevel()
        if (current < 5 && spendCoins(UPGRADE_COSTS[current])) {
            prefs.edit().putInt("speedUpgrade", current + 1).apply()
            return true
        }
        return false
    }

    fun upgradeShieldThrow(): Boolean {
        val current = getShieldThrowUpgradeLevel()
        if (current < 5 && spendCoins(UPGRADE_COSTS[current])) {
            prefs.edit().putInt("shieldThrowUpgrade", current + 1).apply()
            return true
        }
        return false
    }

    fun upgradeUltimate(): Boolean {
        val current = getUltimateUpgradeLevel()
        if (current < 5 && spendCoins(UPGRADE_COSTS[current])) {
            prefs.edit().putInt("ultimateUpgrade", current + 1).apply()
            return true
        }
        return false
    }

    fun getCurrentStats(): UpgradeStats {
        val hpLvl = (getHpUpgradeLevel() - 1).coerceIn(0, 4)
        val shieldLvl = (getShieldUpgradeLevel() - 1).coerceIn(0, 4)
        val speedLvl = (getSpeedUpgradeLevel() - 1).coerceIn(0, 4)
        val throwLvl = (getShieldThrowUpgradeLevel() - 1).coerceIn(0, 4)
        val ultLvl = (getUltimateUpgradeLevel() - 1).coerceIn(0, 4)

        return UpgradeStats(
            maxHp = HP_VALUES[hpLvl],
            shieldDamageMultiplier = SHIELD_DAMAGE_VALUES[shieldLvl],
            moveSpeed = SPEED_VALUES[speedLvl],
            shieldThrowDamage = SHIELD_THROW_VALUES[throwLvl],
            ultimateDamage = ULTIMATE_VALUES[ultLvl]
        )
    }

    // Audio & Haptic settings
    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("soundEnabled", true)
        set(value) = prefs.edit().putBoolean("soundEnabled", value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean("musicEnabled", true)
        set(value) = prefs.edit().putBoolean("musicEnabled", value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean("vibrationEnabled", true)
        set(value) = prefs.edit().putBoolean("vibrationEnabled", value).apply()

    var renderEngineMode: String
        get() = prefs.getString("renderEngineMode", "UNITY_HYBRID") ?: "UNITY_HYBRID"
        set(value) = prefs.edit().putString("renderEngineMode", value).apply()
}
