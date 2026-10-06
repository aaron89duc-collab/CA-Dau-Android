package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaveManager
import com.example.model.LevelInfo
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.ShieldGold
import com.example.ui.theme.ShieldNavy
import com.example.ui.theme.ShieldRed
import com.example.ui.theme.ShieldSilver

@Composable
fun LevelSelectScreen(
    saveManager: SaveManager,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highestUnlocked by saveManager.highestLevelFlow.collectAsState()

    // 10 levels from GDD Section 9
    val levels = listOf(
        LevelInfo(1, "City Under Attack", "Night City", "Mutant/Drone", "Iron Beast", "Vertical Slice • Barrels"),
        LevelInfo(2, "Abandoned Factory", "Industrial", "Robot/Bug", "Mecha Titan", "Conveyors & Crushers"),
        LevelInfo(3, "Dark Forest", "Shadow Woods", "Werewolf/Spider", "Forest Beast", "Chasm Platforms"),
        LevelInfo(4, "Desert Base", "Arid Dunes", "Scorpion/Sand", "Sand Worm", "Sandstorms"),
        LevelInfo(5, "Ice Mountain", "Glacial Peaks", "Ice Monster", "Frozen Golem", "Slippery Ice"),
        LevelInfo(6, "Underground Lab", "High-Tech Lab", "Cyborg/Mutant", "Bio-Titan", "Laser Traps"),
        LevelInfo(7, "Volcanic World", "Magma Cavern", "Fire/Lava", "Lava Dragon", "Molten Lava Waves"),
        LevelInfo(8, "Alien Planet", "Xenosphere", "Alien/Parasite", "Alien Queen", "Plasma Fields"),
        LevelInfo(9, "Dark Fortress", "Citadel", "Mixed Elite", "3 Mini-Bosses", "Boss Rush"),
        LevelInfo(10, "Final War", "Apex Sanctum", "Overlord Vanguard", "Overlord", "3 Phase Climax")
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF060B19)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    text = "LEVEL SELECT (10 MISSIONS)",
                    color = ShieldSilver,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of 10 levels
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(levels) { level ->
                    val isUnlocked = level.id <= highestUnlocked
                    val isCompleted = saveManager.isLevelCompleted(level.id)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isUnlocked) ShieldNavy else Color(0x550B132B))
                            .border(
                                width = if (level.id == 1) 2.dp else 1.dp,
                                color = if (isUnlocked) (if (isCompleted) ShieldGold else ShieldBlue) else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = isUnlocked) {
                                onSelectLevel(level.id)
                            }
                            .padding(12.dp)
                            .testTag("level_node_${level.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isUnlocked) ShieldRed else Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${level.id}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = ShieldGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = level.name,
                                color = if (isUnlocked) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )

                            Text(
                                text = "Boss: ${level.bossName}",
                                color = if (isUnlocked) ShieldRed else Color.DarkGray,
                                fontSize = 10.sp,
                                maxLines = 1
                            )

                            Text(
                                text = level.specialMechanic,
                                color = if (isUnlocked) Color(0xFF00E5FF) else Color.DarkGray,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
