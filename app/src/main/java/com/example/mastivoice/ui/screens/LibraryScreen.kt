package com.example.mastivoice.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastivoice.data.SavedVoice
import com.example.mastivoice.ui.theme.*
import com.example.mastivoice.viewmodel.MastiVoiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(
    viewModel: MastiVoiceViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordings by viewModel.savedRecordings.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playingId by viewModel.currentlyPlayingLibraryId.collectAsState()

    var voiceToRename by remember { mutableStateOf<SavedVoice?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var voiceToDelete by remember { mutableStateOf<SavedVoice?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saved Recordings (${recordings.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "WAV Audio Studio",
                style = MaterialTheme.typography.bodySmall,
                color = MastiCyan
            )
        }

        if (recordings.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(text = "🎙️", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Recordings Saved Yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Go to the Voice Studio, speak into the mic, transform your voice, and hit Save!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = MastiPurpleLight),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Voice Studio", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(recordings, key = { it.id }) { voice ->
                    val isCurrentPlaying = isPlaying && playingId == voice.id

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isCurrentPlaying) MastiCyan else SurfaceCardBorder,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .testTag("saved_voice_card_${voice.id}"),
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceCard
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play/Pause circular button
                            FilledIconButton(
                                onClick = { viewModel.playLibraryVoice(voice) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isCurrentPlaying) MastiCyan else MastiPurple
                                ),
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("play_library_btn_${voice.id}")
                            ) {
                                Icon(
                                    imageVector = if (isCurrentPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isCurrentPlaying) "Pause" else "Play",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = voice.effectType.emoji,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text(
                                        text = voice.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MastiPurple.copy(alpha = 0.35f)
                                    ) {
                                        Text(
                                            text = voice.effectType.displayName,
                                            fontSize = 11.sp,
                                            color = MastiPurpleLight,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    val sec = voice.durationMs / 1000
                                    val ms = (voice.durationMs % 1000) / 100
                                    Text(
                                        text = String.format(Locale.US, "%d.%ds", sec, ms),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${voice.fileSizeFormatted}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }

                                val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(voice.timestamp))
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary,
                                    fontSize = 10.sp
                                )
                            }

                            // Actions: Share, Rename, Delete
                            IconButton(
                                onClick = { viewModel.shareVoice(voice) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("share_btn_${voice.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MastiCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    voiceToRename = voice
                                    renameInput = voice.title
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("rename_btn_${voice.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { voiceToDelete = voice },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_btn_${voice.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MastiRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    voiceToRename?.let { voice ->
        AlertDialog(
            onDismissRequest = { voiceToRename = null },
            title = {
                Text("Rename Recording", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameVoice(voice.id, renameInput)
                        voiceToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MastiCyan)
                ) {
                    Text("Rename", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { voiceToRename = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Delete Confirmation Dialog
    voiceToDelete?.let { voice ->
        AlertDialog(
            onDismissRequest = { voiceToDelete = null },
            title = {
                Text("Delete Recording?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${voice.title}\"? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVoice(voice.id)
                        voiceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MastiRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { voiceToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
