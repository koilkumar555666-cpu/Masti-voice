package com.example.mastivoice.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastivoice.ui.components.SoundboardItem
import com.example.mastivoice.ui.theme.*
import com.example.mastivoice.viewmodel.MastiVoiceViewModel

private data class SoundboardButtonData(
    val id: String,
    val title: String,
    val emoji: String,
    val accentColor: Color
)

@Composable
fun SoundboardScreen(
    viewModel: MastiVoiceViewModel,
    modifier: Modifier = Modifier
) {
    val soundItems = listOf(
        SoundboardButtonData("air_horn", "Air Horn", "📢", MastiYellow),
        SoundboardButtonData("laser", "Laser Zap", "🔫", MastiCyan),
        SoundboardButtonData("boing", "Boing Jump", "🦘", MastiGreen),
        SoundboardButtonData("whistle", "Slide Whistle", "🍌", MastiOrange),
        SoundboardButtonData("impact", "Dramatic Thud", "🥁", MastiRed),
        SoundboardButtonData("victory", "Victory Chime", "🥳", MastiPurpleLight),
        SoundboardButtonData("applause", "Cheering Crowd", "👏", MastiPink),
        SoundboardButtonData("giggle", "Funny Laugh", "😂", Color(0xFFFF6B6B))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = MastiYellow,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Instant Masti Soundboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any pad for instantaneous meme & party sound effects!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(soundItems) { item ->
                SoundboardItem(
                    title = item.title,
                    emoji = item.emoji,
                    accentColor = item.accentColor,
                    onClick = {
                        viewModel.triggerSoundboard(item.id)
                    }
                )
            }
        }
    }
}
