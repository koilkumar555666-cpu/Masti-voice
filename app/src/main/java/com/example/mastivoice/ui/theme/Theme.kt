package com.example.mastivoice.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MastiPurpleLight,
    onPrimary = TextPrimary,
    primaryContainer = MastiPurple,
    onPrimaryContainer = TextPrimary,
    secondary = MastiPink,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFF5B1238),
    onSecondaryContainer = TextPrimary,
    tertiary = MastiCyan,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MastiVoiceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
