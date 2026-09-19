package com.example.mastivoice.audio

import com.example.mastivoice.data.AudioEffectType
import kotlin.math.PI
import kotlin.math.sin

object VoiceEffectsEngine {

    /**
     * Applies voice effects to a 16-bit PCM ShortArray and returns a processed ShortArray.
     */
    fun applyEffect(
        input: ShortArray,
        effectType: AudioEffectType,
        customPitch: Float = 1.0f,
        customSpeed: Float = 1.0f,
        customEcho: Float = 0f,
        sampleRate: Int = WavUtils.SAMPLE_RATE
    ): ShortArray {
        if (input.isEmpty()) return input

        var samples = input.clone()

        // 1. Reverse effect
        if (effectType.isReverse) {
            val reversed = ShortArray(samples.size)
            for (i in samples.indices) {
                reversed[i] = samples[samples.size - 1 - i]
            }
            samples = reversed
        }

        // 2. Robot ring-modulation effect (85Hz metallic carrier with saturation)
        if (effectType.isRobot) {
            val carrierFreq = 85.0
            for (i in samples.indices) {
                val t = i.toDouble() / sampleRate
                val carrier = sin(2.0 * PI * carrierFreq * t)
                val modulated = (samples[i] * carrier * 1.4).toInt()
                // Metallic soft clipping
                samples[i] = modulated.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        // 3. Alien vibrato modulation
        if (effectType.isAlien) {
            val lfoFreq = 6.5
            for (i in samples.indices) {
                val t = i.toDouble() / sampleRate
                val lfo = 0.5 + 0.5 * sin(2.0 * PI * lfoFreq * t)
                val modulated = (samples[i] * (0.4 + 0.7 * lfo)).toInt()
                samples[i] = modulated.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        // 4. Walkie-Talkie / Radio (Bandpass + mild digital crunch)
        if (effectType.isWalkieTalkie) {
            var prev = 0f
            for (i in samples.indices) {
                // High pass filter
                val current = samples[i].toFloat()
                val hp = current - prev * 0.85f
                prev = current
                // Lo-fi quantization (reduce to 6-bit feel)
                val quant = ((hp.toInt() / 256) * 256)
                // Overdrive
                val clipped = (quant * 1.8f).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                samples[i] = clipped.toShort()
            }
        }

        // 5. Megaphone (heavy overdrive saturation & resonant boost)
        if (effectType.isMegaphone) {
            for (i in samples.indices) {
                val original = samples[i].toInt()
                // Symmetrical soft clip
                val boosted = (original * 2.8).toInt()
                val clipped = boosted.coerceIn(-18000, 18000) * 1.6
                samples[i] = clipped.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        // 6. Echo / Delay Cave effect
        val echoDelay = if (effectType.isEcho) effectType.echoDelayMs else if (customEcho > 0.05f) (customEcho * 400).toInt() else 0
        val echoDecay = if (effectType.isEcho) effectType.echoDecay else customEcho

        if (echoDelay > 0 && echoDecay > 0.05f) {
            val delaySamples = (sampleRate * (echoDelay / 1000.0)).toInt().coerceAtLeast(1)
            val output = ShortArray(samples.size + delaySamples * 2)
            System.arraycopy(samples, 0, output, 0, samples.size)

            for (i in samples.indices) {
                val delayedIndex1 = i + delaySamples
                if (delayedIndex1 < output.size) {
                    val echoVal1 = (samples[i] * echoDecay).toInt()
                    output[delayedIndex1] = (output[delayedIndex1] + echoVal1).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                val delayedIndex2 = i + delaySamples * 2
                if (delayedIndex2 < output.size) {
                    val echoVal2 = (samples[i] * echoDecay * echoDecay).toInt()
                    output[delayedIndex2] = (output[delayedIndex2] + echoVal2).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            samples = output
        }

        // 7. Resampling for Pitch / Speed
        val effectivePitch = if (effectType == AudioEffectType.CUSTOM) customPitch else effectType.pitchFactor
        val effectiveSpeed = if (effectType == AudioEffectType.CUSTOM) customSpeed else effectType.speedFactor

        if (kotlin.math.abs(effectivePitch - 1.0f) > 0.03f || kotlin.math.abs(effectiveSpeed - 1.0f) > 0.03f) {
            samples = resampleAudio(samples, effectivePitch, effectiveSpeed)
        }

        return samples
    }

    /**
     * High-quality linear interpolation resampling to modify pitch and tempo.
     */
    private fun resampleAudio(input: ShortArray, pitchFactor: Float, speedFactor: Float): ShortArray {
        // Combined resampling ratio: higher pitch = higher frequency playback = faster stepping
        val ratio = (pitchFactor * speedFactor).coerceIn(0.4f, 2.5f)
        val newLength = (input.size / ratio).toInt().coerceAtLeast(1)
        val output = ShortArray(newLength)

        for (i in 0 until newLength) {
            val sourcePos = i * ratio
            val index0 = sourcePos.toInt().coerceIn(0, input.size - 1)
            val index1 = (index0 + 1).coerceIn(0, input.size - 1)
            val fraction = sourcePos - index0

            val sample0 = input[index0]
            val sample1 = input[index1]
            val interpolated = sample0 + (fraction * (sample1 - sample0)).toInt()
            output[i] = interpolated.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return output
    }
}
