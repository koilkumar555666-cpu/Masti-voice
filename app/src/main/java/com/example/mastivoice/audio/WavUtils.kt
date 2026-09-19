package com.example.mastivoice.audio

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavUtils {

    const val SAMPLE_RATE = 44100
    const val CHANNELS = 1
    const val BITS_PER_SAMPLE = 16

    fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int = SAMPLE_RATE) {
        val totalAudioLen = (pcmData.size * 2).toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = (sampleRate * CHANNELS * (BITS_PER_SAMPLE / 8)).toLong()

        FileOutputStream(file).use { out ->
            val header = ByteArray(44)
            val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

            // "RIFF" chunk
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            buffer.position(4)
            buffer.putInt(totalDataLen.toInt())

            // "WAVE" format
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()

            // "fmt " sub-chunk
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            buffer.position(16)
            buffer.putInt(16) // SubChunk1Size (16 for PCM)
            buffer.putShort(1) // AudioFormat (1 for PCM)
            buffer.putShort(CHANNELS.toShort())
            buffer.putInt(sampleRate)
            buffer.putInt(byteRate.toInt())
            buffer.putShort((CHANNELS * (BITS_PER_SAMPLE / 8)).toShort()) // BlockAlign
            buffer.putShort(BITS_PER_SAMPLE.toShort())

            // "data" sub-chunk
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            buffer.position(40)
            buffer.putInt(totalAudioLen.toInt())

            out.write(header)

            val pcmBytes = ByteArray(pcmData.size * 2)
            val pcmBuffer = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmData) {
                pcmBuffer.putShort(sample)
            }
            out.write(pcmBytes)
        }
    }

    fun readWavFile(file: File): ShortArray? {
        if (!file.exists() || file.length() < 44) return null
        return try {
            val bytes = file.readBytes()
            val dataOffset = 44
            val dataSize = bytes.size - dataOffset
            val shortCount = dataSize / 2
            val shorts = ShortArray(shortCount)
            val buffer = ByteBuffer.wrap(bytes, dataOffset, dataSize).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until shortCount) {
                shorts[i] = buffer.short
            }
            shorts
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
