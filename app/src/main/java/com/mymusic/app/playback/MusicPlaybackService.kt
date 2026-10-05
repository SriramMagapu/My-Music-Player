package com.mymusic.app.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mymusic.app.MainActivity
import com.mymusic.app.R
import com.mymusic.app.model.RepeatMode
import com.mymusic.app.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {

    companion object {
        const val CHANNEL_ID = "my_music_playback_channel"
        const val NOTIFICATION_ID = 1001

        // Singleton reference for in-app UI controller observation
        var instance: MusicPlaybackService? = null
            private set
    }

    private var mediaSession: MediaSession? = null
    lateinit var player: ExoPlayer
        private set
    lateinit var equalizerManager: EqualizerManager
        private set

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    // State flows for Compose UI
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffleOn = MutableStateFlow(false)
    val isShuffleOn: StateFlow<Boolean> = _isShuffleOn.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    // Sleep Timer
    private var sleepCountDown: CountDownTimer? = null
    private val _sleepTimerRemainingMs = MutableStateFlow(0L)
    val sleepTimerRemainingMs: StateFlow<Long> = _sleepTimerRemainingMs.asStateFlow()

    // Noisy audio receiver (e.g. unplug headphones / disconnect Bluetooth)
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        equalizerManager = EqualizerManager(this)

        createNotificationChannel()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // Handles audio focus automatically
            .setHandleAudioBecomingNoisy(true)
            .build()

        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionPendingIntent)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    startProgressTracking()
                    equalizerManager.bindAudioSession(player.audioSessionId)
                } else {
                    stopProgressTracking()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    onTrackEnded()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrackFromMediaItem(mediaItem)
            }
        })

        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(becomingNoisyReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(becomingNoisyReceiver, filter)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls and media notification for My Music player"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        _queue.value = tracks
        _queueIndex.value = startIndex.coerceIn(0, tracks.lastIndex)

        val mediaItems = tracks.map { track ->
            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(track.albumArtUri)
                .build()

            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.contentUri)
                .setMediaMetadata(metadata)
                .build()
        }

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()

        _currentTrack.value = tracks[_queueIndex.value]
    }

    fun playTrack(track: Track) {
        val currentQ = _queue.value.toMutableList()
        val existingIndex = currentQ.indexOfFirst { it.id == track.id }
        if (existingIndex != -1) {
            _queueIndex.value = existingIndex
            player.seekToDefaultPosition(existingIndex)
            player.play()
        } else {
            val newQ = listOf(track) + currentQ
            playTracks(newQ, 0)
        }
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun next() {
        if (_queue.value.isEmpty()) return
        val nextIdx = if (_isShuffleOn.value) {
            (0 until _queue.value.size).random()
        } else {
            (_queueIndex.value + 1) % _queue.value.size
        }
        _queueIndex.value = nextIdx
        player.seekToDefaultPosition(nextIdx)
        player.play()
        _currentTrack.value = _queue.value[nextIdx]
    }

    fun previous() {
        if (_queue.value.isEmpty()) return
        if (player.currentPosition > 3000L) {
            player.seekTo(0L)
            return
        }
        val prevIdx = if (_queueIndex.value > 0) _queueIndex.value - 1 else _queue.value.lastIndex
        _queueIndex.value = prevIdx
        player.seekToDefaultPosition(prevIdx)
        player.play()
        _currentTrack.value = _queue.value[prevIdx]
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffleOn.value
        _isShuffleOn.value = newShuffle
        player.shuffleModeEnabled = newShuffle
    }

    fun cycleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = nextMode
        player.repeatMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun addToQueue(track: Track) {
        val updated = _queue.value + track
        _queue.value = updated
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.albumArtUri)
            .build()
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.contentUri)
            .setMediaMetadata(metadata)
            .build()
        player.addMediaItem(mediaItem)
    }

    fun setSleepTimer(minutes: Int) {
        sleepCountDown?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemainingMs.value = 0L
            return
        }
        val durationMs = minutes * 60 * 1000L
        _sleepTimerRemainingMs.value = durationMs

        sleepCountDown = object : CountDownTimer(durationMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _sleepTimerRemainingMs.value = millisUntilFinished
                // Smooth fade out in the last 15 seconds
                if (millisUntilFinished < 15000L) {
                    val fadeRatio = millisUntilFinished / 15000f
                    player.volume = fadeRatio.coerceIn(0.1f, 1.0f)
                }
            }

            override fun onFinish() {
                _sleepTimerRemainingMs.value = 0L
                player.pause()
                player.volume = 1.0f
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepCountDown?.cancel()
        _sleepTimerRemainingMs.value = 0L
        player.volume = 1.0f
    }

    private fun onTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                player.seekTo(0L)
                player.play()
            }
            RepeatMode.ALL -> next()
            RepeatMode.OFF -> {
                if (_queueIndex.value < _queue.value.lastIndex) {
                    next()
                } else {
                    player.pause()
                }
            }
        }
    }

    private fun updateCurrentTrackFromMediaItem(mediaItem: MediaItem?) {
        val currentMediaId = mediaItem?.mediaId?.toLongOrNull() ?: return
        val current = _queue.value.find { it.id == currentMediaId }
        if (current != null) {
            _currentTrack.value = current
            _queueIndex.value = _queue.value.indexOf(current)
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive && player.isPlaying) {
                _currentPositionMs.value = player.currentPosition
                _durationMs.value = player.duration.coerceAtLeast(0L)
                delay(250)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
    }

    override fun onDestroy() {
        instance = null
        try {
            unregisterReceiver(becomingNoisyReceiver)
        } catch (_: Exception) {}
        cancelSleepTimer()
        stopProgressTracking()
        equalizerManager.releaseEffects()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
