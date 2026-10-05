package com.mymusic.app.model

import android.net.Uri
import kotlinx.serialization.Serializable

/**
 * Core Track representation in My Music.
 */
@Serializable
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUriString: String,
    val albumArtUriString: String? = null,
    val path: String? = null,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val contentUri: Uri
        get() = Uri.parse(contentUriString)

    val albumArtUri: Uri?
        get() = albumArtUriString?.let { Uri.parse(it) }
}

/**
 * Album grouping.
 */
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val year: Int = 0,
    val albumArtUriString: String? = null
)

/**
 * Artist grouping.
 */
data class Artist(
    val name: String,
    val trackCount: Int,
    val albumCount: Int
)

/**
 * Playlist representation.
 */
@Serializable
data class Playlist(
    val id: Long,
    val name: String,
    val createdDate: Long = System.currentTimeMillis(),
    val trackCount: Int = 0,
    val customArtworkUriString: String? = null
)

/**
 * Repeat modes supported by the player.
 */
enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

/**
 * Available voice command actions.
 */
enum class VoiceAction(val displayName: String, val defaultPhrases: List<String>) {
    PLAY("Play Music", listOf("play", "resume", "start music")),
    PAUSE("Pause Music", listOf("pause", "stop", "hold")),
    NEXT("Next Track", listOf("next", "skip", "next song")),
    PREVIOUS("Previous Track", listOf("previous", "back", "replay")),
    SHUFFLE("Toggle Shuffle", listOf("shuffle", "mix", "random")),
    FAVORITE("Favorite Track", listOf("like", "favorite", "love this song")),
    VOLUME_UP("Volume Up", listOf("volume up", "louder", "boost volume")),
    VOLUME_DOWN("Volume Down", listOf("volume down", "quieter", "lower volume")),
    SLEEP_TIMER_30("Set 30m Sleep Timer", listOf("sleep timer 30", "sleep in 30 minutes", "stop in 30")),
    PLAY_FAVORITES("Play Favorites", listOf("play favorites", "my favorites", "loved songs"))
}

/**
 * Custom trained voice command.
 */
@Serializable
data class CustomVoiceCommand(
    val id: Long = 0,
    val triggerPhrase: String,
    val normalizedPhrase: String,
    val action: VoiceAction,
    val targetParam: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Voice recognition UX state.
 */
sealed class VoiceState {
    object Idle : VoiceState()
    data class Listening(val message: String = "Listening...") : VoiceState()
    data class Processing(val recognizedText: String) : VoiceState()
    data class Saved(val commandText: String, val action: VoiceAction) : VoiceState()
    data class Success(val executedActionText: String) : VoiceState()
    data class Error(val errorMessage: String) : VoiceState()
    data class Unavailable(val reason: String = "Speech recognition is unavailable on this device.") : VoiceState()
}

/**
 * Equalizer & Sound Boost settings.
 */
data class SoundSettings(
    val isVolumeBoostEnabled: Boolean = false,
    val volumeBoostPercent: Int = 0, // 0..100% boost
    val isBassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0..1000
    val selectedPresetIndex: Int = 0,
    val presets: List<String> = listOf("Normal", "Classical", "Dance", "Flat", "Folk", "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock")
)
