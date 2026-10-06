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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
fun SettingsScreen(
    saveManager: SaveManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var soundEnabled by remember { mutableStateOf(saveManager.isSoundEnabled) }
    var musicEnabled by remember { mutableStateOf(saveManager.isMusicEnabled) }
    var vibrationEnabled by remember { mutableStateOf(saveManager.isVibrationEnabled) }

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
                    text = "SETTINGS & ENGINE CONFIGURATION",
                    color = ShieldSilver,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Grid / Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Audio & Controls
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AUDIO & HAPTICS",
                        color = ShieldBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    SettingToggleItem(
                        title = "Sound Effects (SFX)",
                        subtitle = "Synthesized retro-futuristic arcade audio",
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            saveManager.isSoundEnabled = it
                        }
                    )

                    SettingToggleItem(
                        title = "Background Music",
                        subtitle = "Dynamic soundtrack during gameplay",
                        checked = musicEnabled,
                        onCheckedChange = {
                            musicEnabled = it
                            saveManager.isMusicEnabled = it
                        }
                    )

                    SettingToggleItem(
                        title = "Haptic Vibration",
                        subtitle = "Tactile impact feedback for hits & explosions",
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            saveManager.isVibrationEnabled = it
                        }
                    )
                }

                // Right Column: Unity Engine & Performance Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "UNITY ENGINE & RENDER PIPELINE",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShieldNavy)
                            .border(1.dp, Color(0x4400B4D8), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            EngineInfoRow(label = "Render Pipeline:", value = "Hardware SurfaceView 60 FPS")
                            EngineInfoRow(label = "Unity Bridge:", value = "UaaL Protocol Active")
                            EngineInfoRow(label = "Reference Resolution:", value = "1920 x 1080 Landscape")
                            EngineInfoRow(label = "Parallax Layers:", value = "4 Depth Planes (3D Skyline)")
                            EngineInfoRow(label = "Memory Optimization:", value = "Zero-GC Object Pooling (120 Pool)")
                            EngineInfoRow(label = "Hot Path Policy:", value = "Non-allocating Array Loop")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ShieldNavy)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(text = subtitle, color = ShieldSilver, fontSize = 10.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ShieldRed,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF263238)
                )
            )
        }
    }
}

@Composable
private fun EngineInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = ShieldSilver, fontSize = 11.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}
