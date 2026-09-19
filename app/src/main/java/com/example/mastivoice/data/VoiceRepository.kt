package com.example.mastivoice.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.mastivoice.audio.WavUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class VoiceRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("masti_voice_prefs", Context.MODE_PRIVATE)
    private val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }

    private val _savedRecordings = MutableStateFlow<List<SavedVoice>>(emptyList())
    val savedRecordings: StateFlow<List<SavedVoice>> = _savedRecordings.asStateFlow()

    init {
        loadRecordings()
    }

    private fun loadRecordings() {
        val jsonString = prefs.getString("recordings_list", "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<SavedVoice>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val filePath = obj.getString("filePath")
                val file = File(filePath)
                if (file.exists()) {
                    val effectName = obj.optString("effectType", AudioEffectType.NORMAL.name)
                    val effect = try {
                        AudioEffectType.valueOf(effectName)
                    } catch (e: Exception) {
                        AudioEffectType.NORMAL
                    }
                    list.add(
                        SavedVoice(
                            id = id,
                            title = obj.getString("title"),
                            effectType = effect,
                            durationMs = obj.optLong("durationMs", 0L),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            filePath = filePath,
                            fileSizeFormatted = formatFileSize(file.length()),
                            pitch = obj.optDouble("pitch", 1.0).toFloat(),
                            speed = obj.optDouble("speed", 1.0).toFloat()
                        )
                    )
                }
            }
            _savedRecordings.value = list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            _savedRecordings.value = emptyList()
        }
    }

    private fun saveListToPrefs(list: List<SavedVoice>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("effectType", item.effectType.name)
                put("durationMs", item.durationMs)
                put("timestamp", item.timestamp)
                put("filePath", item.filePath)
                put("pitch", item.pitch.toDouble())
                put("speed", item.speed.toDouble())
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("recordings_list", jsonArray.toString()).apply()
        _savedRecordings.value = list.sortedByDescending { it.timestamp }
    }

    suspend fun saveRecording(
        title: String,
        effectType: AudioEffectType,
        pcmData: ShortArray,
        durationMs: Long,
        pitch: Float = 1.0f,
        speed: Float = 1.0f
    ): SavedVoice = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val sanitizedTitle = title.ifBlank {
            "Masti_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
        }
        val file = File(recordingsDir, "masti_${System.currentTimeMillis()}.wav")
        WavUtils.writeWavFile(file, pcmData)

        val newVoice = SavedVoice(
            id = id,
            title = sanitizedTitle,
            effectType = effectType,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis(),
            filePath = file.absolutePath,
            fileSizeFormatted = formatFileSize(file.length()),
            pitch = pitch,
            speed = speed
        )

        val updated = _savedRecordings.value.toMutableList().apply { add(0, newVoice) }
        saveListToPrefs(updated)
        newVoice
    }

    suspend fun deleteRecording(id: String) = withContext(Dispatchers.IO) {
        val current = _savedRecordings.value
        val toDelete = current.find { it.id == id }
        if (toDelete != null) {
            val file = File(toDelete.filePath)
            if (file.exists()) {
                file.delete()
            }
            val updated = current.filterNot { it.id == id }
            saveListToPrefs(updated)
        }
    }

    suspend fun renameRecording(id: String, newTitle: String) = withContext(Dispatchers.IO) {
        val current = _savedRecordings.value
        val updated = current.map {
            if (it.id == id) it.copy(title = newTitle.ifBlank { it.title }) else it
        }
        saveListToPrefs(updated)
    }

    fun shareRecording(voice: SavedVoice) {
        val file = File(voice.filePath)
        if (!file.exists()) return

        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, voice.title)
                putExtra(Intent.EXTRA_TEXT, "Listen to my Masti Voice effect: ${voice.title} (${voice.effectType.displayName})!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Masti Voice Audio").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            // Fallback generic send intent
            e.printStackTrace()
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
