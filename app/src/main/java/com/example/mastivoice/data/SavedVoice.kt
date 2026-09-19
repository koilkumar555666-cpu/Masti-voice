package com.example.mastivoice.data

data class SavedVoice(
    val id: String,
    val title: String,
    val effectType: AudioEffectType,
    val durationMs: Long,
    val timestamp: Long,
    val filePath: String,
    val fileSizeFormatted: String,
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f
)
