package com.example.mastivoice.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object SoundboardGenerator {

    const val SAMPLE_RATE = 44100

    fun generateAirHorn(): ShortArray {
        val durationSec = 1.05
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        // Air horn fundamental ~466Hz (Bb4) + harmonics with brassy envelope
        val f0 = 466.16
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Attack envelope
            val env = when {
                t < 0.05 -> t / 0.05
                t > 0.85 -> (durationSec - t) / 0.2
                else -> 1.0
            }
            // Harmonics with slight detuning
            val s1 = sin(2.0 * PI * f0 * t)
            val s2 = 0.8 * sin(2.0 * PI * (f0 * 1.5) * t)
            val s3 = 0.6 * sin(2.0 * PI * (f0 * 2.0) * t)
            val s4 = 0.5 * sin(2.0 * PI * (f0 * 2.5) * t)
            val s5 = 0.4 * sin(2.0 * PI * (f0 * 3.0) * t)
            val combined = (s1 + s2 + s3 + s4 + s5) / 3.3
            // Brass saturation
            val sat = if (combined > 0.6) 0.6 else if (combined < -0.6) -0.6 else combined
            val finalVal = (sat * env * 28000).toInt()
            samples[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    fun generateLaser(): ShortArray {
        val durationSec = 0.45
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        // Downward exponential chirp from 2400Hz to 120Hz
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 2400.0 * exp(-9.0 * t) + 120.0
            val phase = 2.0 * PI * freq * t
            val env = (1.0 - t / durationSec)
            val wave = sin(phase)
            val finalVal = (wave * env * 26000).toInt()
            samples[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    fun generateCartoonBoing(): ShortArray {
        val durationSec = 0.75
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        // Frequency sweeps up with heavy pitch vibrato
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val baseFreq = 220.0 + (650.0 * t)
            val vibrato = 45.0 * sin(2.0 * PI * 22.0 * t)
            val freq = baseFreq + vibrato
            val env = exp(-2.8 * t)
            val wave = sin(2.0 * PI * freq * t)
            val finalVal = (wave * env * 27000).toInt()
            samples[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    fun generateSlideWhistle(): ShortArray {
        val durationSec = 0.8
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Up and down frequency slide
            val freq = 500.0 + 900.0 * sin(PI * (t / durationSec))
            val env = sin(PI * (t / durationSec))
            val wave = sin(2.0 * PI * freq * t)
            val finalVal = (wave * env * 24000).toInt()
            samples[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    fun generateDramaticImpact(): ShortArray {
        val durationSec = 1.2
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val rnd = Random(42)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Low sub boom + filtered white noise impact
            val boomFreq = 95.0 * exp(-3.0 * t) + 35.0
            val boom = sin(2.0 * PI * boomFreq * t) * exp(-2.5 * t)
            val noise = (rnd.nextDouble() * 2.0 - 1.0) * exp(-12.0 * t)
            val combined = boom * 0.7 + noise * 0.3
            val finalVal = (combined * 29000).toInt()
            samples[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    fun generateVictoryChime(): ShortArray {
        val durationSec = 1.2
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6 arpeggio
        val noteDur = durationSec / 4.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIdx = (t / noteDur).toInt().coerceIn(0, 3)
            val noteTime = t - (noteIdx * noteDur)
            val freq = notes[noteIdx]
            val env = exp(-3.5 * noteTime)
            val wave = sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * freq * 2.0 * t)
            val finalVal = (wave * env * 23000).toInt()
            samples[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    fun generateApplause(): ShortArray {
        val durationSec = 1.4
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val rnd = Random(123)
        var lp = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val raw = rnd.nextDouble() * 2.0 - 1.0
            // Lowpass smoothing
            lp += 0.25 * (raw - lp)
            // Periodic claps overlay
            val clapBurst = sin(2.0 * PI * 14.0 * t).let { if (it > 0.8) 1.5 else 0.8 }
            val env = when {
                t < 0.2 -> t / 0.2
                t > 1.0 -> (durationSec - t) / 0.4
                else -> 1.0
            }
            val finalVal = (lp * clapBurst * env * 25000).toInt()
            samples[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    fun generateGiggle(): ShortArray {
        val durationSec = 1.1
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // 5 fast pitch chirps mimicking a laugh
            val laughMod = sin(2.0 * PI * 6.5 * t).coerceAtLeast(0.0)
            val freq = 550.0 + 320.0 * laughMod
            val env = exp(-1.8 * t)
            val wave = sin(2.0 * PI * freq * t) * laughMod
            val finalVal = (wave * env * 26000).toInt()
            samples[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }
}
