package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameColorScheme = darkColorScheme(
    primary = ShieldRed,
    onPrimary = Color.White,
    primaryContainer = ShieldRedDark,
    onPrimaryContainer = ShieldSilver,
    secondary = ShieldBlueLight,
    onSecondary = Color.Black,
    secondaryContainer = ShieldBlue,
    onSecondaryContainer = Color.White,
    tertiary = ShieldGold,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = ShieldSilver
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameColorScheme,
        typography = Typography,
        content = content
    )
}
