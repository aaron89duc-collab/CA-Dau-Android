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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun MainMenuScreen(
    saveManager: SaveManager,
    onStartGame: () -> Unit,
    onLevelSelect: () -> Unit,
    onUpgrades: () -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coins by saveManager.coinsFlow.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF060B19)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Background subtle gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF081226), Color(0xFF050914))
                        )
                    )
            )

            // Top Header: Currency Display
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC0B132B))
                        .border(1.5.dp, ShieldGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
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

            // Center Content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Shield Emblem Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ShieldRed)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ShieldRed)
                                .align(Alignment.Center)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(ShieldBlue)
                                    .align(Alignment.Center)
                            ) {
                                Text(
                                    text = "★",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = "CAPTAIN AMERICA",
                    color = ShieldSilver,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "SHIELD FORCE",
                    color = ShieldRed,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Unity Engine 3D Integration • 60 FPS Render",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onStartGame,
                        modifier = Modifier
                            .width(180.dp)
                            .height(52.dp)
                            .testTag("play_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShieldRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "PLAY LEVEL 1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Button(
                        onClick = onLevelSelect,
                        modifier = Modifier
                            .width(160.dp)
                            .height(52.dp)
                            .testTag("level_select_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShieldBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.List, contentDescription = "Level Select", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "10 LEVELS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Button(
                        onClick = onUpgrades,
                        modifier = Modifier
                            .width(160.dp)
                            .height(52.dp)
                            .testTag("upgrade_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Build, contentDescription = "Upgrades", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "ARMORY", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Settings & Credits
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSettings,
                        modifier = Modifier
                            .width(160.dp)
                            .height(44.dp)
                            .testTag("settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C2541)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = ShieldSilver)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "SETTINGS", color = ShieldSilver, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onCredits,
                        modifier = Modifier
                            .width(160.dp)
                            .height(44.dp)
                            .testTag("credits_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C2541)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Credits", tint = ShieldSilver)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "CREDITS", color = ShieldSilver, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
