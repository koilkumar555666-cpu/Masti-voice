package com.example.mastivoice.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mastivoice.ui.theme.*
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isRecording: Boolean,
    isPlaying: Boolean,
    currentAmplitude: Float,
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    barCount: Int = 36
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCard
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val totalBars = barCount
            val barSpacing = canvasWidth / totalBars
            val barWidth = (barSpacing * 0.55f).coerceAtLeast(3.dp.toPx())
            val midY = canvasHeight / 2f

            for (i in 0 until totalBars) {
                val x = i * barSpacing + (barSpacing - barWidth) / 2f
                val barProgress = i.toFloat() / totalBars

                // Determine height of this bar
                val barHeightFraction: Float = when {
                    isRecording -> {
                        // Dynamic frequency visualization with amplitude modulation
                        val wave = sin((i * 0.4f) + wavePhase) * 0.4f + 0.6f
                        val base = 0.15f
                        val dyn = currentAmplitude * 0.85f * wave
                        (base + dyn).coerceIn(0.12f, 0.98f)
                    }
                    isPlaying -> {
                        val wave = sin((i * 0.5f) + wavePhase * 1.5f) * 0.35f + 0.65f
                        val isPassed = barProgress <= progress
                        if (isPassed) {
                            (0.3f + 0.65f * wave).coerceIn(0.2f, 0.95f)
                        } else {
                            0.2f
                        }
                    }
                    else -> {
                        // Idle gentle wave
                        val idleWave = (sin(i * 0.3f) * 0.2f + 0.35f).coerceIn(0.15f, 0.6f)
                        idleWave
                    }
                }

                val barHeight = (canvasHeight * barHeightFraction).coerceAtLeast(6.dp.toPx())
                val top = midY - barHeight / 2f

                // Color gradient based on state & playback progress
                val isPastProgress = barProgress <= progress
                val barBrush = when {
                    isRecording -> Brush.verticalGradient(
                        colors = listOf(MastiRed, MastiPink, MastiPurpleLight)
                    )
                    isPlaying && isPastProgress -> Brush.verticalGradient(
                        colors = listOf(MastiCyan, MastiPurpleLight)
                    )
                    isPlaying && !isPastProgress -> Brush.verticalGradient(
                        colors = listOf(SurfaceCardBorder, Color(0xFF2C2448))
                    )
                    else -> Brush.verticalGradient(
                        colors = listOf(MastiPurple.copy(alpha = 0.6f), SurfaceCardBorder)
                    )
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
