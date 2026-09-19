package com.example.mastivoice.audio

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt

class AudioRecorderManager(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var onAmplitudeUpdate: ((amplitude: Float) -> Unit)? = null
    var onDurationUpdate: ((durationMs: Long) -> Unit)? = null

    @Volatile
    var isRecording: Boolean = false
        private set

    private var recordedShorts: ShortArray? = null

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startRecording(onError: (String) -> Unit = {}): Boolean {
        if (!hasPermission()) {
            onError("Microphone permission not granted")
            return false
        }

        stopRecording()

        val sampleRate = WavUtils.SAMPLE_RATE
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(4096)

        return try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onError("Failed to initialize microphone")
                return false
            }

            audioRecord?.startRecording()
            isRecording = true

            recordingJob = scope.launch {
                val shortBuffer = ShortArray(bufferSize / 2)
                val allShorts = ArrayList<Short>(sampleRate * 5)
                val startTime = System.currentTimeMillis()

                while (isActive && isRecording) {
                    val read = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: -1
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            val sample = shortBuffer[i]
                            allShorts.add(sample)
                            sum += sample * sample
                        }
                        val rms = sqrt(sum / read).toFloat()
                        val normalizedAmp = (rms / 12000f).coerceIn(0f, 1f)

                        val currentDurationMs = System.currentTimeMillis() - startTime
                        withContext(Dispatchers.Main) {
                            onAmplitudeUpdate?.invoke(normalizedAmp)
                            onDurationUpdate?.invoke(currentDurationMs)
                        }
                    }
                }

                recordedShorts = allShorts.toShortArray()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            onError("Error starting recorder: ${e.localizedMessage}")
            false
        }
    }

    fun stopRecording(): ShortArray? {
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null

        return recordedShorts
    }

    fun getRecordedAudio(): ShortArray? = recordedShorts

    fun setRecordedAudio(audio: ShortArray) {
        recordedShorts = audio
    }

    fun clear() {
        recordedShorts = null
    }

    fun release() {
        stopRecording()
        scope.cancel()
    }
}
