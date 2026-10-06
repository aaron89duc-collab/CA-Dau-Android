package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameEngine
import com.example.model.BossPhase
import com.example.model.ShieldThrowState
import com.example.ui.theme.HazardOrange
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.ShieldGold
import com.example.ui.theme.ShieldNavy
import com.example.ui.theme.ShieldRed
import com.example.ui.theme.ShieldSilver
import com.example.unity.UnityBridge
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun GameHudOverlay(
    engine: GameEngine,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by engine.statsFlow.collectAsState()
    val engineFps by UnityBridge.engineFps.collectAsState()
    var isPaused by remember { mutableStateOf(false) }

    // Sync pause state
    LaunchedEffect(isPaused) {
        engine.isPaused = isPaused
    }

    Box(modifier = modifier.fillMaxSize()) {

        // ================= TOP HUD =================
        TopHudBar(
            engine = engine,
            stats = stats,
            engineFps = engineFps,
            onPauseClick = { isPaused = true },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ================= BOSS HUD BAR =================
        if (engine.isBossFightActive && !engine.boss.isDead) {
            BossHudBar(
                boss = engine.boss,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 58.dp)
            )
        }

        // ================= RESPAWN BANNER (GDD Section 13: 'Respawning...' - no Game Over) =================
        if (engine.player.isRespawning) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC9B0018))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "RESPAWNING AT CHECKPOINT...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        // ================= LEFT: VIRTUAL JOYSTICK =================
        VirtualJoystick(
            onMove = { x, y ->
                engine.inputMoveX = x
                engine.inputMoveY = y
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp)
        )

        // ================= RIGHT: COMBAT ACTION BUTTONS =================
        ActionControls(
            engine = engine,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 20.dp)
        )

        // ================= PAUSE DIALOG =================
        if (isPaused) {
            PauseDialog(
                onResume = { isPaused = false },
                onRestartCheckpoint = {
                    isPaused = false
                    engine.player.x = engine.player.checkpointX
                    engine.player.y = engine.levelEnv.groundY
                    engine.player.hp = engine.player.maxHp
                },
                onExitToMenu = {
                    isPaused = false
                    onNavigateBack()
                }
            )
        }

        // ================= VICTORY DIALOG =================
        if (engine.isVictory) {
            VictoryDialog(
                stats = stats,
                onNextLevel = {
                    engine.resetLevel()
                },
                onExitToMenu = onNavigateBack
            )
        }
    }
}

@Composable
private fun TopHudBar(
    engine: GameEngine,
    stats: com.example.engine.GameSessionStats,
    engineFps: Int,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player Health Bar & Ultimate Gauge
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xAA0B132B))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // HP Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HP",
                    color = ShieldRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.width(28.dp)
                )
                val hpFraction = (engine.player.hp.toFloat() / engine.player.maxHp).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { hpFraction },
                    modifier = Modifier
                        .width(140.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (hpFraction > 0.3f) ShieldRed else Color.Red,
                    trackColor = Color(0x55333333)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${engine.player.hp}/${engine.player.maxHp}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ultimate Gauge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ULT",
                    color = ShieldGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.width(28.dp)
                )
                val ultFraction = (engine.player.ultimateMeter / engine.player.maxUltimateMeter).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { ultFraction },
                    modifier = Modifier
                        .width(140.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ShieldGold,
                    trackColor = Color(0x55333333)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${(ultFraction * 100).toInt()}%",
                    color = ShieldSilver,
                    fontSize = 10.sp
                )
            }
        }

        // Center: Stage Segment & Engine Telemetry
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stats.currentSegment,
                color = ShieldSilver,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Unity Bridge 60 FPS",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$engineFps FPS",
                    color = if (engineFps >= 50) Color.Green else Color.Yellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right: Coins & Pause
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xAA0B132B))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "★", color = ShieldGold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${engine.saveManager.getCoins()}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA1C2541))
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause Game",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun BossHudBar(
    boss: com.example.entities.BossIronBeast,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xDD0B132B))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.width(320.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BOSS: IRON BEAST",
                color = ShieldRed,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
            Text(
                text = if (boss.phase == BossPhase.PHASE_1) "PHASE 1" else "PHASE 2 [ENRAGED]",
                color = if (boss.phase == BossPhase.PHASE_1) ShieldGold else Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Boss Health Bar with Phase Threshold Indicators (GDD Section 10: 66% & 33%)
        val bossFraction = (boss.hp.toFloat() / boss.maxHp).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { bossFraction },
            modifier = Modifier
                .width(320.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (boss.phase == BossPhase.PHASE_1) ShieldRed else Color.Red,
            trackColor = Color(0x55333333)
        )

        if (boss.isTelegraphing) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = boss.telegraphWarningText,
                color = Color.Yellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Virtual Floating/Fixed Joystick (GDD Section 5: Left side, responsive)
 */
@Composable
private fun VirtualJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 55f

    Box(
        modifier = modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(Color(0x550B132B))
            .border(2.dp, Color(0x6600B4D8), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = offsetX + dragAmount.x
                        val newY = offsetY + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)

                        if (dist <= maxRadius) {
                            offsetX = newX
                            offsetY = newY
                        } else {
                            offsetX = (newX / dist) * maxRadius
                            offsetY = (newY / dist) * maxRadius
                        }
                        onMove(offsetX / maxRadius, offsetY / maxRadius)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00B4D8), Color(0xFF0077B6))
                    )
                )
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

/**
 * Right Action Controls (FIRE, JUMP, SHIELD, BLOCK, ULTIMATE)
 * Fulfills GDD Section 5: Touch target >= 64dp
 */
@Composable
private fun ActionControls(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Column 1: BLOCK & DASH
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val blockInteraction = remember { MutableInteractionSource() }
            val isBlockPressed by blockInteraction.collectIsPressedAsState()
            LaunchedEffect(isBlockPressed) {
                engine.inputBlockHeld = isBlockPressed
            }

            // BLOCK / PERFECT BLOCK Button
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(if (isBlockPressed) ShieldBlue else Color(0xCC1C2541))
                    .border(2.dp, Color(0xFF00B4D8), CircleShape)
                    .testTag("block_button"),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { },
                    interactionSource = blockInteraction,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Block", tint = Color.White, modifier = Modifier.size(22.dp))
                        Text(text = "BLOCK", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ULTIMATE Button
            val isUltReady = engine.player.ultimateMeter >= engine.player.maxUltimateMeter
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(if (isUltReady) Color(0xFFFFB703) else Color(0x66444444))
                    .border(2.dp, if (isUltReady) Color.White else Color.Transparent, CircleShape)
                    .testTag("ultimate_button"),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (isUltReady) engine.inputUltimatePressed = true
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = "Ultimate", tint = if (isUltReady) Color.Black else Color.Gray, modifier = Modifier.size(24.dp))
                        Text(text = "ULT", color = if (isUltReady) Color.Black else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Column 2: SHIELD THROW & JUMP
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // SHIELD THROW Boomerang
            val isShieldInHand = engine.player.shieldWeapon.state == ShieldThrowState.IN_HAND
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(if (isShieldInHand) ShieldRed else Color(0x99555555))
                    .border(2.dp, Color.White, CircleShape)
                    .testTag("shield_button"),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { engine.inputShieldPressed = true },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "Shield Throw", tint = Color.White, modifier = Modifier.size(26.dp))
                        Text(text = if (isShieldInHand) "SHIELD" else "RECALL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // JUMP (Double jump enabled)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC0077B6))
                    .border(2.dp, Color(0xFF00E5FF), CircleShape)
                    .testTag("jump_button"),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { engine.inputJumpPressed = true },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Jump", tint = Color.White, modifier = Modifier.size(26.dp))
                        Text(text = "JUMP", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Column 3: FIRE (Hold for auto-fire)
        val fireInteraction = remember { MutableInteractionSource() }
        val isFirePressed by fireInteraction.collectIsPressedAsState()
        LaunchedEffect(isFirePressed) {
            engine.inputFireHeld = isFirePressed
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isFirePressed) Color(0xFFFF5722) else Color(0xDDE63946))
                .border(3.dp, Color.White, CircleShape)
                .testTag("fire_button"),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { },
                interactionSource = fireInteraction,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.TrackChanges, contentDescription = "Fire", tint = Color.White, modifier = Modifier.size(30.dp))
                    Text(text = "FIRE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun PauseDialog(
    onResume: () -> Unit,
    onRestartCheckpoint: () -> Unit,
    onExitToMenu: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0x99000000)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ShieldNavy)
                    .border(2.dp, Color(0xFF00B4D8), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldRed)
                ) {
                    Text("RESUME GAME")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRestartCheckpoint,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldBlue)
                ) {
                    Text("RESTART CHECKPOINT")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                ) {
                    Text("MAIN MENU")
                }
            }
        }
    }
}

@Composable
private fun VictoryDialog(
    stats: com.example.engine.GameSessionStats,
    onNextLevel: () -> Unit,
    onExitToMenu: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xAA000000)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ShieldNavy)
                    .border(2.dp, ShieldGold, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LEVEL COMPLETE!",
                    color = ShieldGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(
                    text = "Iron Beast Defeated",
                    color = ShieldSilver,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Time:", color = ShieldSilver)
                    Text(text = "${stats.elapsedTimeSeconds.toInt()}s", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Coins Earned:", color = ShieldSilver)
                    Text(text = "+200 ★", color = ShieldGold, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Enemies Eliminated:", color = ShieldSilver)
                    Text(text = "${stats.enemiesDefeated}", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldRed)
                ) {
                    Text("PLAY AGAIN / NEXT LEVEL")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                ) {
                    Text("RETURN TO MENU")
                }
            }
        }
    }
}
