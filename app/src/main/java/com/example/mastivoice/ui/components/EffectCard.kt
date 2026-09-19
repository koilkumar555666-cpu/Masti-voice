package com.example.mastivoice.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastivoice.data.AudioEffectType
import com.example.mastivoice.ui.theme.*

@Composable
fun EffectCard(
    effect: AudioEffectType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1.0f,
        animationSpec = spring(),
        label = "cardScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MastiCyan else SurfaceCardBorder,
        label = "borderColor"
    )

    val bgBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF2E1A47), Color(0xFF1E1438))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(SurfaceCard, SurfaceDark)
        )
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .height(115.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("effect_card_${effect.name.lowercase()}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = effect.emoji,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = effect.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = effect.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MastiCyan,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}
