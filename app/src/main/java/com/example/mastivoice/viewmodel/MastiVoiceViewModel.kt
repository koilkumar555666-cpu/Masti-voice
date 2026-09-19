package com.example.mastivoice.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastivoice.audio.*
import com.example.mastivoice.data.AudioEffectType
import com.example.mastivoice.data.SavedVoice
import com.example.mastivoice.data.VoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MastiVoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VoiceRepository(application)
    private val recorderManager = AudioRecorderManager(application)
    private val playerController = AudioPlayerController()
    private val soundboardPlayer = AudioPlayerController()

    val savedRecordings: StateFlow<List<SavedVoice>> = repository.savedRecordings

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _hasRecording = MutableStateFlow(false)
    val hasRecording: StateFlow<Boolean> = _hasRecording.asStateFlow()

    private val _selectedEffect = MutableStateFlow(AudioEffectType.CHIPMUNK)
    val selectedEffect: StateFlow<AudioEffectType> = _selectedEffect.asStateFlow()

    private val _customPitch = MutableStateFlow(1.2f)
    val customPitch: StateFlow<Float> = _customPitch.asStateFlow()

    private val _customSpeed = MutableStateFlow(1.0f)
    val customSpeed: StateFlow<Float> = _customSpeed.asStateFlow()

    private val _customEcho = MutableStateFlow(0.0f)
    val customEcho: StateFlow<Float> = _customEcho.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _playbackDurationMs = MutableStateFlow(0L)
    val playbackDurationMs: StateFlow<Long> = _playbackDurationMs.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _statusMessage = MutableStateFlow("Tap the microphone to record your voice!")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _currentlyPlayingLibraryId = MutableStateFlow<String?>(null)
    val currentlyPlayingLibraryId: StateFlow<String?> = _currentlyPlayingLibraryId.asStateFlow()

    private var rawRecordedPcm: ShortArray? = null
    private var processedPcm: ShortArray? = null

    init {
        recorderManager.onAmplitudeUpdate = { amp ->
            _currentAmplitude.value = amp
        }
        recorderManager.onDurationUpdate = { dur ->
            _recordingDurationMs.value = dur
        }

        playerController.onProgressUpdate = { currentMs, totalMs ->
            _playbackPositionMs.value = currentMs
            _playbackDurationMs.value = totalMs
        }
        playerController.onPlaybackFinished = {
            _isPlaying.value = false
            _playbackPositionMs.value = 0L
            _currentlyPlayingLibraryId.value = null
        }
    }

    fun hasMicPermission(): Boolean = recorderManager.hasPermission()

    fun startRecording() {
        vibrateHaptic()
        playerController.stop()
        _isPlaying.value = false
        _recordingDurationMs.value = 0L
        _currentAmplitude.value = 0f

        val success = recorderManager.startRecording { error ->
            _statusMessage.value = error
        }
        if (success) {
            _isRecording.value = true
            _statusMessage.value = "Recording... Speak clearly into the microphone!"
        }
    }

    fun stopRecording() {
        vibrateHaptic()
        val data = recorderManager.stopRecording()
        _isRecording.value = false
        _currentAmplitude.value = 0f

        if (data != null && data.isNotEmpty()) {
            rawRecordedPcm = data
            _hasRecording.value = true
            val durSec = String.format(java.util.Locale.US, "%.1f", data.size.toFloat() / WavUtils.SAMPLE_RATE)
            _statusMessage.value = "Recorded $durSec sec! Select an effect below to listen."
            applyCurrentEffectAndPrepare()
        } else {
            _statusMessage.value = "Recording was too short. Try again!"
        }
    }

    fun selectEffect(effect: AudioEffectType) {
        vibrateHaptic()
        _selectedEffect.value = effect
        if (rawRecordedPcm != null) {
            applyCurrentEffectAndPrepare()
        }
    }

    fun updateCustomControls(pitch: Float, speed: Float, echo: Float) {
        _customPitch.value = pitch
        _customSpeed.value = speed
        _customEcho.value = echo
        _selectedEffect.value = AudioEffectType.CUSTOM
        if (rawRecordedPcm != null) {
            applyCurrentEffectAndPrepare()
        }
    }

    private fun applyCurrentEffectAndPrepare() {
        val raw = rawRecordedPcm ?: return
        viewModelScope.launch(Dispatchers.Default) {
            val processed = VoiceEffectsEngine.applyEffect(
                input = raw,
                effectType = _selectedEffect.value,
                customPitch = _customPitch.value,
                customSpeed = _customSpeed.value,
                customEcho = _customEcho.value
            )
            processedPcm = processed
            withContext(Dispatchers.Main) {
                val totalMs = (processed.size * 1000L) / WavUtils.SAMPLE_RATE
                _playbackDurationMs.value = totalMs
                // Automatically preview effect
                playProcessedAudio()
            }
        }
    }

    fun togglePlayPause() {
        vibrateHaptic()
        if (_isPlaying.value) {
            playerController.pause()
            _isPlaying.value = false
        } else {
            if (playerController.isPaused) {
                playerController.resume()
                _isPlaying.value = true
            } else {
                playProcessedAudio()
            }
        }
    }

    fun playProcessedAudio() {
        val data = processedPcm ?: return
        playerController.stop()
        playerController.setLooping(_isLooping.value)
        _isPlaying.value = true
        _currentlyPlayingLibraryId.value = null
        playerController.play(data)
        _statusMessage.value = "Playing with ${_selectedEffect.value.displayName} effect!"
    }

    fun seekTo(progressFraction: Float) {
        val duration = _playbackDurationMs.value
        val seekMs = (duration * progressFraction).toLong()
        _playbackPositionMs.value = seekMs
        playerController.seekTo(seekMs)
    }

    fun toggleLoop() {
        vibrateHaptic()
        val next = !_isLooping.value
        _isLooping.value = next
        playerController.setLooping(next)
    }

    fun saveCurrentRecording(title: String, onSaved: (SavedVoice) -> Unit) {
        val data = processedPcm ?: return
        viewModelScope.launch {
            val voice = repository.saveRecording(
                title = title,
                effectType = _selectedEffect.value,
                pcmData = data,
                durationMs = _playbackDurationMs.value,
                pitch = _customPitch.value,
                speed = _customSpeed.value
            )
            _statusMessage.value = "Saved to Library: ${voice.title}"
            onSaved(voice)
        }
    }

    // --- Soundboard ---

    fun triggerSoundboard(soundType: String) {
        vibrateHaptic()
        viewModelScope.launch(Dispatchers.Default) {
            val pcm = when (soundType) {
                "air_horn" -> SoundboardGenerator.generateAirHorn()
                "laser" -> SoundboardGenerator.generateLaser()
                "boing" -> SoundboardGenerator.generateCartoonBoing()
                "whistle" -> SoundboardGenerator.generateSlideWhistle()
                "impact" -> SoundboardGenerator.generateDramaticImpact()
                "victory" -> SoundboardGenerator.generateVictoryChime()
                "applause" -> SoundboardGenerator.generateApplause()
                "giggle" -> SoundboardGenerator.generateGiggle()
                else -> SoundboardGenerator.generateAirHorn()
            }
            withContext(Dispatchers.Main) {
                soundboardPlayer.stop()
                soundboardPlayer.play(pcm)
            }
        }
    }

    // --- Library Management ---

    fun playLibraryVoice(voice: SavedVoice) {
        vibrateHaptic()
        if (_currentlyPlayingLibraryId.value == voice.id && _isPlaying.value) {
            playerController.pause()
            _isPlaying.value = false
        } else {
            playerController.stop()
            _currentlyPlayingLibraryId.value = voice.id
            _isPlaying.value = true
            playerController.playFile(File(voice.filePath))
        }
    }

    fun shareVoice(voice: SavedVoice) {
        vibrateHaptic()
        repository.shareRecording(voice)
    }

    fun renameVoice(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameRecording(id, newTitle)
        }
    }

    fun deleteVoice(id: String) {
        vibrateHaptic()
        if (_currentlyPlayingLibraryId.value == id) {
            playerController.stop()
            _isPlaying.value = false
            _currentlyPlayingLibraryId.value = null
        }
        viewModelScope.launch {
            repository.deleteRecording(id)
        }
    }

    private fun vibrateHaptic() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    override fun onCleared() {
        super.onCleared()
        recorderManager.release()
        playerController.release()
        soundboardPlayer.release()
    }
}
