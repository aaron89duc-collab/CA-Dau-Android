package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.GameEngine
import com.example.model.BossPhase
import com.example.model.ShieldThrowState
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
                    .padding(top = 66.dp)
            )
        }

        // ================= RESPAWN BANNER =================
        if (engine.player.isRespawning) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE69B0018))
                    .border(2.dp, Color.White, RoundedCornerShape(14.dp))
                    .padding(horizontal = 28.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "RESPAWNING HERO ĐẬU...",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // ================= LEFT: CYBER JOYSTICK =================
        CyberJoystick(
            onMove = { x, y ->
                engine.inputMoveX = x
                engine.inputMoveY = y
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp)
        )

        // ================= RIGHT: PREMIUM COMBAT CONTROLS =================
        PremiumActionControls(
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
        // Hero Avatar & Health / Ultimate Bars
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xDD0E1B33), Color(0xBB16264C))
                    )
                )
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Hero Đậu Face Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFFF4081), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_dau),
                    contentDescription = "Hero Đậu",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BÉ ĐẬU",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SHIELD FORCE",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // HP Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val hpFraction = (engine.player.hp.toFloat() / engine.player.maxHp).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { hpFraction },
                        modifier = Modifier
                            .width(130.dp)
                            .height(9.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (hpFraction > 0.35f) Color(0xFF00E676) else Color(0xFFFF1744),
                        trackColor = Color(0x55000000)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${engine.player.hp}/${engine.player.maxHp}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Ultimate Gauge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val ultFraction = (engine.player.ultimateMeter / engine.player.maxUltimateMeter).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { ultFraction },
                        modifier = Modifier
                            .width(130.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ShieldGold,
                        trackColor = Color(0x55000000)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ULT ${(ultFraction * 100).toInt()}%",
                        color = ShieldGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Center: Stage Segment & Engine Telemetry
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xAA081226))
                .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = stats.currentSegment,
                color = ShieldSilver,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Unity 3D Engine • ",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp
                )
                Text(
                    text = "$engineFps FPS",
                    color = if (engineFps >= 55) Color.Green else Color.Yellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right: Coins & Pause Button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xDD0B132B))
                    .border(1.dp, ShieldGold, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "★", color = ShieldGold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${engine.saveManager.getCoins()}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xDD1C2541))
                    .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
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
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xEE1E0B12), Color(0xEE0B132B))
                )
            )
            .border(1.5.dp, if (boss.phase == BossPhase.PHASE_1) Color(0xFFFF1744) else Color(0xFFFF9100), RoundedCornerShape(10.dp))
            .padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.width(360.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BOSS: IRON BEAST MECH",
                color = Color(0xFFFF5252),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = if (boss.phase == BossPhase.PHASE_1) "PHASE 1" else "PHASE 2 [ENRAGED]",
                color = if (boss.phase == BossPhase.PHASE_1) ShieldGold else Color(0xFFFF1744),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val bossFraction = (boss.hp.toFloat() / boss.maxHp).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { bossFraction },
            modifier = Modifier
                .width(360.dp)
                .height(11.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = if (boss.phase == BossPhase.PHASE_1) Color(0xFFFF1744) else Color(0xFFFF3D00),
            trackColor = Color(0x66000000)
        )

        if (boss.isTelegraphing) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = boss.telegraphWarningText,
                color = Color.Yellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun CyberJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 55f

    Box(
        modifier = modifier
            .size(136.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x550B1B3A), Color(0x77060F22))
                )
            )
            .border(2.dp, Color(0xFF00E5FF), CircleShape)
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
        // Inner Guide Rings
        Box(
            modifier = Modifier
                .size(70.dp)
                .border(1.dp, Color(0x3300E5FF), CircleShape)
        )

        // Thumb knob with neon glow
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFF0077B6))
                    )
                )
                .border(2.5.dp, Color.White, CircleShape)
        )
    }
}

@Composable
private fun PremiumActionControls(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ultPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ult_scale"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Column 1: BLOCK & ULTIMATE
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val blockInteraction = remember { MutableInteractionSource() }
            val isBlockPressed by blockInteraction.collectIsPressedAsState()
            LaunchedEffect(isBlockPressed) {
                engine.inputBlockHeld = isBlockPressed
            }

            // BLOCK Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(if (isBlockPressed) Color(0xFF00B4D8) else Color(0xCC0E1F40))
                    .border(2.dp, Color(0xFF00E5FF), CircleShape)
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
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Block", tint = Color.White, modifier = Modifier.size(24.dp))
                        Text(text = "BLOCK", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ULTIMATE Button
            val isUltReady = engine.player.ultimateMeter >= engine.player.maxUltimateMeter
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(if (isUltReady) ultPulse else 1f)
                    .clip(CircleShape)
                    .background(
                        if (isUltReady) {
                            Brush.radialGradient(listOf(Color(0xFFFFB703), Color(0xFFFF5722)))
                        } else {
                            Brush.radialGradient(listOf(Color(0x66333333), Color(0x661A1A1A)))
                        }
                    )
                    .border(2.dp, if (isUltReady) Color.White else Color(0x44FFFFFF), CircleShape)
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
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = "Ultimate", tint = if (isUltReady) Color.White else Color.Gray, modifier = Modifier.size(26.dp))
                        Text(text = "SMASH", color = if (isUltReady) Color.White else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // Column 2: SHIELD & JUMP
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val isShieldInHand = engine.player.shieldWeapon.state == ShieldThrowState.IN_HAND
            // SHIELD BOOMERANG
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (isShieldInHand) Color(0xFFD90429) else Color(0x99555555))
                    .border(2.dp, Color.White, CircleShape)
                    .testTag("shield_button"),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { engine.inputShieldPressed = true },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "Shield Throw", tint = Color.White, modifier = Modifier.size(28.dp))
                        Text(text = if (isShieldInHand) "SHIELD" else "RECALL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // JUMP (Double jump)
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(Color(0xDD0077B6))
                    .border(2.5.dp, Color(0xFF00E5FF), CircleShape)
                    .testTag("jump_button"),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { engine.inputJumpPressed = true },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Jump", tint = Color.White, modifier = Modifier.size(28.dp))
                        Text(text = "JUMP", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Column 3: FIRE (Held auto-fire)
        val fireInteraction = remember { MutableInteractionSource() }
        val isFirePressed by fireInteraction.collectIsPressedAsState()
        LaunchedEffect(isFirePressed) {
            engine.inputFireHeld = isFirePressed
        }

        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(
                    if (isFirePressed) {
                        Brush.radialGradient(listOf(Color(0xFFFF1744), Color(0xFFFF5252)))
                    } else {
                        Brush.radialGradient(listOf(Color(0xFFD90429), Color(0xFF9B0018)))
                    }
                )
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
                    Icon(imageVector = Icons.Default.TrackChanges, contentDescription = "Fire", tint = Color.White, modifier = Modifier.size(32.dp))
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
        color = Color(0xAA000000)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .width(330.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0B142B))
                    .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(18.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME PAUSED",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("RESUME BATTLE", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRestartCheckpoint,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("RESTART CHECKPOINT", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("EXIT TO MAIN MENU", fontWeight = FontWeight.SemiBold)
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
        color = Color(0xCC000000)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .width(380.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0B152E))
                    .border(2.dp, ShieldGold, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "★ VICTORY! ★",
                    color = ShieldGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "BÉ ĐẬU ĐÃ HẠ GỤC IRON BEAST!",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Thời gian:", color = ShieldSilver)
                    Text(text = "${stats.elapsedTimeSeconds.toInt()}s", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Thưởng hoàn thành:", color = ShieldSilver)
                    Text(text = "+250 ★", color = ShieldGold, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Quái đã tiêu diệt:", color = ShieldSilver)
                    Text(text = "${stats.enemiesDefeated}", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("TIẾP TỤC / CHƠI LẠI", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("VỀ MENU CHÍNH", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
