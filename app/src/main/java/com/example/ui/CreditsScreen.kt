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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.ShieldGold
import com.example.ui.theme.ShieldNavy
import com.example.ui.theme.ShieldRed
import com.example.ui.theme.ShieldSilver

@Composable
fun CreditsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "CREDITS & SPECIFICATIONS",
                    color = ShieldSilver,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShieldNavy)
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CAPTAIN AMERICA: SHIELD FORCE (GDD v1.0)",
                        color = ShieldRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "2D Run-and-Gun Action Platformer with 3D Parallax & Depth Rendering",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "IP NOTICE (from GDD):",
                        color = ShieldGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Captain America và các yếu tố nhận diện liên quan là IP của Marvel/Disney. Dự án triển khai theo Game Design Document MVP cho Android.",
                        color = ShieldSilver,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ARCHITECTURE & TECHNICAL IMPLEMENTATION:",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Hardware SurfaceView 60 FPS Engine + Unity-as-a-Library Bridge Integration\n• Procedural Waveform Audio Synthesis (AudioTrack 22kHz)\n• 0-Allocation Object Pools for Projectiles, Explosions, Particles\n• Jetpack Compose Material 3 Touch & HUD Interface",
                        color = ShieldSilver,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
