package com.mymusic.app.playback

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import com.mymusic.app.model.SoundSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EqualizerManager(private val context: Context) {

    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var bassBoost: BassBoost? = null
    private var currentAudioSessionId: Int = 0

    private val _soundSettings = MutableStateFlow(SoundSettings())
    val soundSettings: StateFlow<SoundSettings> = _soundSettings.asStateFlow()

    fun bindAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentAudioSessionId) return
        currentAudioSessionId = audioSessionId
        releaseEffects()

        try {
            // Equalizer
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }

            // Loudness Enhancer (Safe volume boost on Android 19+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = _soundSettings.value.isVolumeBoostEnabled
                    if (_soundSettings.value.isVolumeBoostEnabled) {
                        setTargetGain(_soundSettings.value.volumeBoostPercent * 50) // e.g. 0 to 5000 mB
                    }
                }
            }

            // Bass Boost
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _soundSettings.value.isBassBoostEnabled
                if (_soundSettings.value.isBassBoostEnabled) {
                    setStrength(_soundSettings.value.bassBoostStrength.toShort())
                }
            }

            applySettings(_soundSettings.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVolumeBoost(enabled: Boolean, percent: Int) {
        val clampedPercent = percent.coerceIn(0, 100)
        _soundSettings.value = _soundSettings.value.copy(
            isVolumeBoostEnabled = enabled,
            volumeBoostPercent = clampedPercent
        )
        try {
            loudnessEnhancer?.apply {
                this.enabled = enabled
                if (enabled) {
                    setTargetGain(clampedPercent * 50)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBassBoost(enabled: Boolean, strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _soundSettings.value = _soundSettings.value.copy(
            isBassBoostEnabled = enabled,
            bassBoostStrength = clamped
        )
        try {
            bassBoost?.apply {
                this.enabled = enabled
                if (enabled) {
                    setStrength(clamped.toShort())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerPreset(presetIndex: Int) {
        val count = equalizer?.numberOfPresets?.toInt() ?: 0
        if (presetIndex in 0 until count) {
            try {
                equalizer?.usePreset(presetIndex.toShort())
                _soundSettings.value = _soundSettings.value.copy(selectedPresetIndex = presetIndex)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPresetNames(): List<String> {
        val names = mutableListOf<String>()
        try {
            val count = equalizer?.numberOfPresets?.toInt() ?: 0
            for (i in 0 until count) {
                names.add(equalizer?.getPresetName(i.toShort()) ?: "Preset $i")
            }
        } catch (_: Exception) {}
        return if (names.isNotEmpty()) names else _soundSettings.value.presets
    }

    private fun applySettings(settings: SoundSettings) {
        setVolumeBoost(settings.isVolumeBoostEnabled, settings.volumeBoostPercent)
        setBassBoost(settings.isBassBoostEnabled, settings.bassBoostStrength)
        setEqualizerPreset(settings.selectedPresetIndex)
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
            equalizer = null
            loudnessEnhancer?.release()
            loudnessEnhancer = null
            bassBoost?.release()
            bassBoost = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
