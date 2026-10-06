package com.example.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SaveManager
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.ShieldGold
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

            // Background cyber gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF0F1E3D), Color(0xFF060B18)),
                            radius = 1200f
                        )
                    )
            )

            // Top Header: Currency Display & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UNITY 3D ENGINE • 60 FPS ACTIVE",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xDD0B132B))
                        .border(1.5.dp, ShieldGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "★", color = ShieldGold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$coins COINS",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Center Content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Main Hero Character Card with image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(Color(0xFFFF4081), Color(0xFF00E5FF), Color(0xFFFFB703), Color(0xFFFF4081))
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF0E1A33)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_dau),
                            contentDescription = "Bé Đậu Hero Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = "BÉ ĐẬU",
                    color = Color(0xFFFF80AB),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "SHIELD FORCE",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )

                Text(
                    text = "2D Run-and-Gun Platformer • Đồ Họa 3D Parallax Mượt Mà",
                    color = Color(0xFF80D8FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onStartGame,
                        modifier = Modifier
                            .width(190.dp)
                            .height(54.dp)
                            .border(2.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))
                            .testTag("play_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShieldRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "CHIẾN ĐẤU MÀN 1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = onLevelSelect,
                        modifier = Modifier
                            .width(160.dp)
                            .height(54.dp)
                            .border(1.5.dp, Color(0xFF00B4D8), RoundedCornerShape(12.dp))
                            .testTag("level_select_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShieldBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.List, contentDescription = "Level Select", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "10 MÀN CHƠI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = onUpgrades,
                        modifier = Modifier
                            .width(160.dp)
                            .height(54.dp)
                            .border(1.5.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                            .testTag("upgrade_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Build, contentDescription = "Upgrades", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "KHO VŨ KHÍ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142244)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = ShieldSilver)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "CÀI ĐẶT", color = ShieldSilver, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onCredits,
                        modifier = Modifier
                            .width(160.dp)
                            .height(44.dp)
                            .testTag("credits_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142244)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Credits", tint = ShieldSilver)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "THÔNG TIN", color = ShieldSilver, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
