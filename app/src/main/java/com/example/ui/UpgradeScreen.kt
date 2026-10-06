package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaveManager
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.ShieldGold
import com.example.ui.theme.ShieldNavy
import com.example.ui.theme.ShieldRed
import com.example.ui.theme.ShieldSilver

@Composable
fun UpgradeScreen(
    saveManager: SaveManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coins by saveManager.coinsFlow.collectAsState()
    var refreshKey by remember { mutableIntStateOf(0) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF060B19)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AVENGERS ARMORY & UPGRADES",
                        color = ShieldSilver,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC0B132B))
                        .border(1.dp, ShieldGold, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "★", color = ShieldGold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$coins COINS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Upgrade Rows
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val hpLvl = saveManager.getHpUpgradeLevel()
                    val currentVal = SaveManager.HP_VALUES[hpLvl - 1]
                    val nextVal = if (hpLvl < 5) SaveManager.HP_VALUES[hpLvl] else currentVal
                    val cost = if (hpLvl < 5) SaveManager.UPGRADE_COSTS[hpLvl] else 0

                    UpgradeCard(
                        title = "MAX HEALTH (HP)",
                        level = hpLvl,
                        currentValue = "$currentVal HP",
                        nextValue = if (hpLvl < 5) "Next: $nextVal HP" else "MAX LEVEL",
                        cost = cost,
                        canAfford = coins >= cost && hpLvl < 5,
                        onUpgrade = {
                            if (saveManager.upgradeHp()) refreshKey++
                        }
                    )
                }

                item {
                    val shieldLvl = saveManager.getShieldUpgradeLevel()
                    val currentVal = (SaveManager.SHIELD_DAMAGE_VALUES[shieldLvl - 1] * 100).toInt()
                    val nextVal = if (shieldLvl < 5) (SaveManager.SHIELD_DAMAGE_VALUES[shieldLvl] * 100).toInt() else currentVal
                    val cost = if (shieldLvl < 5) SaveManager.UPGRADE_COSTS[shieldLvl] else 0

                    UpgradeCard(
                        title = "SHIELD DAMAGE MULTIPLIER",
                        level = shieldLvl,
                        currentValue = "$currentVal%",
                        nextValue = if (shieldLvl < 5) "Next: $nextVal%" else "MAX LEVEL",
                        cost = cost,
                        canAfford = coins >= cost && shieldLvl < 5,
                        onUpgrade = {
                            if (saveManager.upgradeShield()) refreshKey++
                        }
                    )
                }

                item {
                    val speedLvl = saveManager.getSpeedUpgradeLevel()
                    val currentVal = SaveManager.SPEED_VALUES[speedLvl - 1]
                    val nextVal = if (speedLvl < 5) SaveManager.SPEED_VALUES[speedLvl] else currentVal
                    val cost = if (speedLvl < 5) SaveManager.UPGRADE_COSTS[speedLvl] else 0

                    UpgradeCard(
                        title = "TACTICAL MOVE SPEED",
                        level = speedLvl,
                        currentValue = "$currentVal units/s",
                        nextValue = if (speedLvl < 5) "Next: $nextVal" else "MAX LEVEL",
                        cost = cost,
                        canAfford = coins >= cost && speedLvl < 5,
                        onUpgrade = {
                            if (saveManager.upgradeSpeed()) refreshKey++
                        }
                    )
                }

                item {
                    val throwLvl = saveManager.getShieldThrowUpgradeLevel()
                    val currentVal = SaveManager.SHIELD_THROW_VALUES[throwLvl - 1]
                    val nextVal = if (throwLvl < 5) SaveManager.SHIELD_THROW_VALUES[throwLvl] else currentVal
                    val cost = if (throwLvl < 5) SaveManager.UPGRADE_COSTS[throwLvl] else 0

                    UpgradeCard(
                        title = "SHIELD THROW BOOMERANG POWER",
                        level = throwLvl,
                        currentValue = "$currentVal DMG",
                        nextValue = if (throwLvl < 5) "Next: $nextVal DMG" else "MAX LEVEL",
                        cost = cost,
                        canAfford = coins >= cost && throwLvl < 5,
                        onUpgrade = {
                            if (saveManager.upgradeShieldThrow()) refreshKey++
                        }
                    )
                }

                item {
                    val ultLvl = saveManager.getUltimateUpgradeLevel()
                    val currentVal = SaveManager.ULTIMATE_VALUES[ultLvl - 1]
                    val nextVal = if (ultLvl < 5) SaveManager.ULTIMATE_VALUES[ultLvl] else currentVal
                    val cost = if (ultLvl < 5) SaveManager.UPGRADE_COSTS[ultLvl] else 0

                    UpgradeCard(
                        title = "VIBRANIUM ULTIMATE IMPACT",
                        level = ultLvl,
                        currentValue = "$currentVal DMG",
                        nextValue = if (ultLvl < 5) "Next: $nextVal DMG" else "MAX LEVEL",
                        cost = cost,
                        canAfford = coins >= cost && ultLvl < 5,
                        onUpgrade = {
                            if (saveManager.upgradeUltimate()) refreshKey++
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    title: String,
    level: Int,
    currentValue: String,
    nextValue: String,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ShieldNavy)
            .border(1.dp, Color(0x4400B4D8), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Lv $level/5", color = ShieldGold, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { level / 5f },
                    modifier = Modifier
                        .width(180.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ShieldBlue,
                    trackColor = Color(0x33FFFFFF)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(text = currentValue, color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = nextValue, color = ShieldSilver, fontSize = 10.sp)
                }
            }

            if (level < 5) {
                Button(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) ShieldRed else Color(0xFF37474F),
                        disabledContainerColor = Color(0xFF263238)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "UPGRADE ($cost ★)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color.White else Color.Gray
                    )
                }
            } else {
                Text(
                    text = "MAXED OUT",
                    color = ShieldGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
    }
}
