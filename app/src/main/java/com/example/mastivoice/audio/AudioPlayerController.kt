package com.example.mastivoice.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import java.io.File

class AudioPlayerController {

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var onProgressUpdate: ((currentMs: Long, totalMs: Long) -> Unit)? = null
    var onPlaybackFinished: (() -> Unit)? = null

    @Volatile
    var isPlaying: Boolean = false
        private set

    @Volatile
    var isPaused: Boolean = false
        private set

    private var currentData: ShortArray? = null
    private var currentSampleRate: Int = WavUtils.SAMPLE_RATE
    private var playbackOffset = 0
    private var loopEnabled = false

    fun setLooping(enabled: Boolean) {
        loopEnabled = enabled
    }

    fun play(pcmData: ShortArray, sampleRate: Int = WavUtils.SAMPLE_RATE, startPositionMs: Long = 0) {
        stop()
        if (pcmData.isEmpty()) return

        currentData = pcmData
        currentSampleRate = sampleRate
        val totalMs = (pcmData.size * 1000L) / sampleRate
        val offsetSamples = ((startPositionMs * sampleRate) / 1000L).toInt().coerceIn(0, pcmData.size)
        playbackOffset = offsetSamples

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize * 4)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        isPlaying = true
        isPaused = false

        playbackJob = scope.launch {
            val chunkSize = 2048
            val track = audioTrack ?: return@launch

            while (isActive && isPlaying) {
                if (isPaused) {
                    delay(50)
                    continue
                }

                val data = currentData ?: break
                if (playbackOffset >= data.size) {
                    if (loopEnabled) {
                        playbackOffset = 0
                    } else {
                        break
                    }
                }

                val toWrite = minOf(chunkSize, data.size - playbackOffset)
                val written = track.write(data, playbackOffset, toWrite)
                if (written > 0) {
                    playbackOffset += written
                    val currentMs = (playbackOffset * 1000L) / sampleRate
                    withContext(Dispatchers.Main) {
                        onProgressUpdate?.invoke(currentMs, totalMs)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                isPlaying = false
                isPaused = false
                playbackOffset = 0
                onPlaybackFinished?.invoke()
            }
        }
    }

    fun playFile(file: File) {
        val pcm = WavUtils.readWavFile(file)
        if (pcm != null) {
            play(pcm)
        }
    }

    fun pause() {
        if (isPlaying && !isPaused) {
            isPaused = true
            audioTrack?.pause()
        }
    }

    fun resume() {
        if (isPlaying && isPaused) {
            isPaused = false
            audioTrack?.play()
        }
    }

    fun seekTo(progressMs: Long) {
        val data = currentData ?: return
        val targetSample = ((progressMs * currentSampleRate) / 1000L).toInt().coerceIn(0, data.size)
        playbackOffset = targetSample
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            // Ignore release exceptions
        }
        audioTrack = null
        isPlaying = false
        isPaused = false
        playbackOffset = 0
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
