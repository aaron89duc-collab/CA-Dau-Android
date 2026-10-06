package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.SaveManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class GameAudio private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val saveManager = SaveManager.getInstance(appContext)
    private val audioScope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    companion object {
        @Volatile
        private var INSTANCE: GameAudio? = null

        fun getInstance(context: Context): GameAudio {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameAudio(context).also { INSTANCE = it }
            }
        }

        private const val SAMPLE_RATE = 22050
    }

    fun playShieldShot() {
        if (!saveManager.isSoundEnabled) return
        audioScope.launch {
            // Rapid high-pitch energy zap (900 Hz -> 300 Hz)
            val durationMs = 60
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 950f * (1f - t * 0.7f)
                val angle = 2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)
                val sample = (sin(angle) * (1f - t) * Short.MAX_VALUE * 0.45f).toInt().toShort()
                buffer[i] = sample
            }
            playPcmBuffer(buffer)
        }
    }

    fun playShieldThrow() {
        if (!saveManager.isSoundEnabled) return
        vibrate(30)
        audioScope.launch {
            // Metallic whoosh + spin (modulating freq 450 - 650 Hz)
            val durationMs = 120
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val wobble = sin(2.0 * Math.PI * 30.0 * t) * 120.0
                val freq = 500.0 + wobble
                val angle = 2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)
                val sample = (sin(angle) * (1f - t * 0.5f) * Short.MAX_VALUE * 0.5f).toInt().toShort()
                buffer[i] = sample
            }
            playPcmBuffer(buffer)
        }
    }

    fun playShieldCatch() {
        if (!saveManager.isSoundEnabled) return
        vibrate(40)
        audioScope.launch {
            // High metallic chime catch (1350 Hz with harmonics)
            val durationMs = 100
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq1 = 1350.0
                val freq2 = 2700.0
                val a1 = sin(2.0 * Math.PI * freq1 * (i.toDouble() / SAMPLE_RATE))
                val a2 = sin(2.0 * Math.PI * freq2 * (i.toDouble() / SAMPLE_RATE)) * 0.4
                val decay = (1f - t) * (1f - t)
                val sample = ((a1 + a2) * decay * Short.MAX_VALUE * 0.5f).toInt().toShort()
                buffer[i] = sample
            }
            playPcmBuffer(buffer)
        }
    }

    fun playPerfectBlock() {
        if (!saveManager.isSoundEnabled) return
        vibrate(80)
        audioScope.launch {
            // Sharp crystal impact + power chime (2200 Hz chime + explosion crack)
            val durationMs = 180
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 1800.0 + 400.0 * (1f - t)
                val a1 = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE))
                val noise = if (i < 300) ((Math.random() - 0.5) * 2.0) else 0.0
                val decay = (1f - t)
                val sample = ((a1 * 0.7 + noise * 0.3) * decay * Short.MAX_VALUE * 0.7f).toInt().toShort()
                buffer[i] = sample
            }
            playPcmBuffer(buffer)
        }
    }

    fun playExplosion() {
        if (!saveManager.isSoundEnabled) return
        vibrate(100)
        audioScope.launch {
            val durationMs = 220
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val noise = (Math.random() - 0.5) * 2.0
                val lowFreq = sin(2.0 * Math.PI * 110.0 * (1f - t * 0.8f) * (i.toDouble() / SAMPLE_RATE))
                val decay = (1f - t) * (1f - t)
                val sample = ((noise * 0.6 + lowFreq * 0.4) * decay * Short.MAX_VALUE * 0.65f).toInt().toShort()
                buffer[i] = sample
            }
            playPcmBuffer(buffer)
        }
    }

    fun playJump() {
        if (!saveManager.isSoundEnabled) return
        audioScope.launch {
            val durationMs = 80
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 320.0 + 450.0 * t
                val a = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE))
                buffer[i] = (a * (1f - t) * Short.MAX_VALUE * 0.35f).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playPickup() {
        if (!saveManager.isSoundEnabled) return
        audioScope.launch {
            // Arpeggio chime
            val durationMs = 120
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = if (t < 0.5f) 880.0 else 1320.0
                val a = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE))
                buffer[i] = (a * (1f - t) * Short.MAX_VALUE * 0.45f).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playBossRoar() {
        if (!saveManager.isSoundEnabled) return
        vibrate(200)
        audioScope.launch {
            val durationMs = 350
            val numSamples = (SAMPLE_RATE * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 90.0 + sin(t * 15.0) * 20.0
                val low = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE))
                val noise = (Math.random() - 0.5) * 1.5
                val decay = (1f - t)
                buffer[i] = ((low * 0.7 + noise * 0.3) * decay * Short.MAX_VALUE * 0.8f).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playVictoryFanfare() {
        if (!saveManager.isSoundEnabled) return
        audioScope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            val noteDurationMs = 130
            val totalSamples = (SAMPLE_RATE * noteDurationMs * notes.size / 1000)
            val buffer = ShortArray(totalSamples)
            var sampleIdx = 0
            for (freq in notes) {
                val samplesForNote = (SAMPLE_RATE * noteDurationMs / 1000)
                for (j in 0 until samplesForNote) {
                    val t = j.toFloat() / samplesForNote
                    val a = sin(2.0 * Math.PI * freq * (j.toDouble() / SAMPLE_RATE))
                    buffer[sampleIdx++] = (a * (1f - t * 0.3f) * Short.MAX_VALUE * 0.45f).toInt().toShort()
                }
            }
            playPcmBuffer(buffer)
        }
    }

    fun vibrate(durationMs: Long) {
        if (!saveManager.isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore vibration permissions error if restricted
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(track: AudioTrack?) {
                    track?.release()
                }
                override fun onPeriodicNotification(track: AudioTrack?) {}
            })
        } catch (_: Exception) {
            // Audio track fallback
        }
    }
}
