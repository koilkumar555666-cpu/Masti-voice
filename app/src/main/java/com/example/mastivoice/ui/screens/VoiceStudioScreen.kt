package com.example.mastivoice.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastivoice.data.AudioEffectType
import com.example.mastivoice.ui.components.EffectCard
import com.example.mastivoice.ui.components.WaveformVisualizer
import com.example.mastivoice.ui.theme.*
import com.example.mastivoice.viewmodel.MastiVoiceViewModel
import java.util.Locale

@Composable
fun VoiceStudioScreen(
    viewModel: MastiVoiceViewModel,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDurationMs by viewModel.recordingDurationMs.collectAsState()
    val amplitude by viewModel.currentAmplitude.collectAsState()
    val hasRecording by viewModel.hasRecording.collectAsState()
    val selectedEffect by viewModel.selectedEffect.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPositionMs by viewModel.playbackPositionMs.collectAsState()
    val playbackDurationMs by viewModel.playbackDurationMs.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val customPitch by viewModel.customPitch.collectAsState()
    val customSpeed by viewModel.customSpeed.collectAsState()
    val customEcho by viewModel.customEcho.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveTitleInput by remember { mutableStateOf("") }
    var showFineTuningDialog by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    // Pulse animation for recording ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Status & Tip banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRecording) "🔴" else "✨",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (hasRecording) {
                    IconButton(
                        onClick = { showFineTuningDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("fine_tune_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Fine Tune Audio",
                            tint = MastiCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Live Audio Waveform
        val progressFraction = if (playbackDurationMs > 0) {
            (playbackPositionMs.toFloat() / playbackDurationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        WaveformVisualizer(
            isRecording = isRecording,
            isPlaying = isPlaying,
            currentAmplitude = amplitude,
            progress = progressFraction,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Center Action: Giant Record Button with Pulse
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(MastiRed.copy(alpha = 0.25f))
                )
            }

            Surface(
                modifier = Modifier
                    .size(76.dp)
                    .testTag("record_button")
                    .clickable {
                        if (isRecording) {
                            viewModel.stopRecording()
                        } else {
                            if (viewModel.hasMicPermission()) {
                                viewModel.startRecording()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                shape = CircleShape,
                color = if (isRecording) MastiRed else MastiPurple,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = TextPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // Timer Indicator
        Text(
            text = if (isRecording) {
                val sec = recordingDurationMs / 1000
                val ms = (recordingDurationMs % 1000) / 100
                String.format(Locale.US, "Recording: %02d.%d s", sec, ms)
            } else if (hasRecording) {
                val totalSec = playbackDurationMs / 1000
                val curSec = playbackPositionMs / 1000
                String.format(Locale.US, "%02d:%02d / %02d:%02d", curSec / 60, curSec % 60, totalSec / 60, totalSec % 60)
            } else {
                "Tap Mic to Record"
            },
            style = MaterialTheme.typography.labelLarge,
            color = if (isRecording) MastiRed else TextSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            textAlign = TextAlign.Center
        )

        // Voice Effect Presets Grid Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Voice Effects",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tap to transform",
                style = MaterialTheme.typography.bodySmall,
                color = MastiCyan
            )
        }

        // Voice Effects Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(AudioEffectType.values()) { effect ->
                EffectCard(
                    effect = effect,
                    isSelected = selectedEffect == effect,
                    onClick = {
                        if (effect == AudioEffectType.CUSTOM) {
                            showFineTuningDialog = true
                        }
                        viewModel.selectEffect(effect)
                    }
                )
            }
        }

        // Bottom Playback & Save Bar (Visible when audio has been recorded)
        AnimatedVisibility(
            visible = hasRecording,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Playback Progress Slider
                    Slider(
                        value = progressFraction,
                        onValueChange = { frac -> viewModel.seekTo(frac) },
                        colors = SliderDefaults.colors(
                            thumbColor = MastiCyan,
                            activeTrackColor = MastiCyan,
                            inactiveTrackColor = SurfaceCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Loop toggle
                        IconButton(
                            onClick = { viewModel.toggleLoop() },
                            modifier = Modifier.testTag("loop_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Loop",
                                tint = if (isLooping) MastiCyan else TextTertiary
                            )
                        }

                        // Play/Pause Button
                        FilledIconButton(
                            onClick = { viewModel.togglePlayPause() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MastiPurpleLight
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Save to Library Button
                        Button(
                            onClick = {
                                saveTitleInput = "${selectedEffect.displayName} Voice"
                                showSaveDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MastiPink
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("save_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Save Recording Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Voice Recording",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a name for this ${selectedEffect.displayName} audio file:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = saveTitleInput,
                        onValueChange = { saveTitleInput = it },
                        singleLine = true,
                        placeholder = { Text("My Funny Voice", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentRecording(saveTitleInput) {
                            showSaveDialog = false
                            onNavigateToLibrary()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MastiPink),
                    modifier = Modifier.testTag("confirm_save_btn")
                ) {
                    Text("Save to Library", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Custom Fine-Tuning Dialog
    if (showFineTuningDialog) {
        var pitchVal by remember { mutableFloatStateOf(customPitch) }
        var speedVal by remember { mutableFloatStateOf(customSpeed) }
        var echoVal by remember { mutableFloatStateOf(customEcho) }

        AlertDialog(
            onDismissRequest = { showFineTuningDialog = false },
            title = {
                Text(
                    text = "🎛️ Voice Pitch & Speed Tuner",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Pitch slider
                    Text(
                        text = "Pitch: ${String.format(Locale.US, "%.2f", pitchVal)}x",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = pitchVal,
                        onValueChange = { pitchVal = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MastiPurpleLight,
                            activeTrackColor = MastiPurpleLight
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Speed slider
                    Text(
                        text = "Speed / Tempo: ${String.format(Locale.US, "%.2f", speedVal)}x",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = speedVal,
                        onValueChange = { speedVal = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MastiPink,
                            activeTrackColor = MastiPink
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Echo slider
                    Text(
                        text = "Echo Canyon: ${if (echoVal > 0.05f) String.format(Locale.US, "%.0f%%", echoVal * 100) else "Off"}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = echoVal,
                        onValueChange = { echoVal = it },
                        valueRange = 0f..0.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = MastiCyan,
                            activeTrackColor = MastiCyan
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCustomControls(pitchVal, speedVal, echoVal)
                        showFineTuningDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MastiPurpleLight)
                ) {
                    Text("Apply Tuning", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFineTuningDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
