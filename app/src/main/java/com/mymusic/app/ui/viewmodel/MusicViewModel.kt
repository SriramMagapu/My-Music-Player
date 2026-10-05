package com.mymusic.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mymusic.app.data.MusicRepository
import com.mymusic.app.model.Album
import com.mymusic.app.model.Artist
import com.mymusic.app.model.CustomVoiceCommand
import com.mymusic.app.model.Playlist
import com.mymusic.app.model.RepeatMode
import com.mymusic.app.model.SoundSettings
import com.mymusic.app.model.Track
import com.mymusic.app.model.VoiceAction
import com.mymusic.app.model.VoiceState
import com.mymusic.app.playback.MusicPlaybackService
import com.mymusic.app.voice.VoiceCommandManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab {
    TRACKS,
    ALBUMS,
    PLAYLISTS,
    FAVORITES
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MusicRepository(application)
    val voiceManager = VoiceCommandManager(application, repository)

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Library Tabs & Search
    private val _selectedTab = MutableStateFlow(LibraryTab.TRACKS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered Tracks
    val filteredTracks: StateFlow<List<Track>> = combine(
        repository.allTracks,
        _searchQuery
    ) { tracks, query ->
        if (query.isBlank()) tracks
        else {
            val q = query.trim().lowercase()
            tracks.filter {
                it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.album.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Favorites
    val filteredFavorites: StateFlow<List<Track>> = combine(
        repository.favorites,
        _searchQuery
    ) { favs, query ->
        if (query.isBlank()) favs
        else {
            val q = query.trim().lowercase()
            favs.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Albums
    val albums: StateFlow<List<Album>> = repository.allTracks.combine(_searchQuery) { _, query ->
        val list = repository.getAlbums()
        if (query.isBlank()) list
        else {
            val q = query.trim().lowercase()
            list.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Artists
    val artists: StateFlow<List<Artist>> = repository.allTracks.combine(_searchQuery) { _, query ->
        val list = repository.getArtists()
        if (query.isBlank()) list
        else {
            val q = query.trim().lowercase()
            list.filter { it.name.lowercase().contains(q) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Track>> = repository.recentlyPlayed
    val playlists: StateFlow<List<Playlist>> = repository.playlists
    val voiceCommands: StateFlow<List<CustomVoiceCommand>> = repository.voiceCommands
    val logoLetter: StateFlow<String> = repository.logoLetter
    val selectedMusicFolder: StateFlow<String?> = repository.selectedMusicFolder
    val hasCompletedOnboarding: StateFlow<Boolean> = repository.hasCompletedOnboarding

    // Now Playing & Playback State
    val currentTrack: StateFlow<Track?> get() = MusicPlaybackService.instance?.currentTrack ?: MutableStateFlow(null)
    val isPlaying: StateFlow<Boolean> get() = MusicPlaybackService.instance?.isPlaying ?: MutableStateFlow(false)
    val currentPositionMs: StateFlow<Long> get() = MusicPlaybackService.instance?.currentPositionMs ?: MutableStateFlow(0L)
    val durationMs: StateFlow<Long> get() = MusicPlaybackService.instance?.durationMs ?: MutableStateFlow(0L)
    val isShuffleOn: StateFlow<Boolean> get() = MusicPlaybackService.instance?.isShuffleOn ?: MutableStateFlow(false)
    val repeatMode: StateFlow<RepeatMode> get() = MusicPlaybackService.instance?.repeatMode ?: MutableStateFlow(RepeatMode.OFF)
    val queue: StateFlow<List<Track>> get() = MusicPlaybackService.instance?.queue ?: MutableStateFlow(emptyList())
    val sleepTimerRemainingMs: StateFlow<Long> get() = MusicPlaybackService.instance?.sleepTimerRemainingMs ?: MutableStateFlow(0L)

    // Voice State
    val voiceState: StateFlow<VoiceState> = voiceManager.voiceState
    val soundLevel: StateFlow<Float> = voiceManager.soundLevel

    // Sound Settings
    val soundSettings: StateFlow<SoundSettings>
        get() = MusicPlaybackService.instance?.equalizerManager?.soundSettings ?: MutableStateFlow(SoundSettings())

    // Volume Swipe Gesture State
    private val _volumePercent = MutableStateFlow(getDeviceVolumePercent())
    val volumePercent: StateFlow<Int> = _volumePercent.asStateFlow()

    private val _isVolumeHudVisible = MutableStateFlow(false)
    val isVolumeHudVisible: StateFlow<Boolean> = _isVolumeHudVisible.asStateFlow()

    private var volumeHudJob: Job? = null

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Playback Operations
    fun playTrackList(tracks: List<Track>, startIndex: Int = 0) {
        val service = MusicPlaybackService.instance ?: return
        service.playTracks(tracks, startIndex)
        if (tracks.isNotEmpty()) {
            val track = tracks[startIndex.coerceIn(0, tracks.lastIndex)]
            viewModelScope.launch {
                repository.recordTrackPlayed(track.id)
            }
        }
    }

    fun playSingleTrack(track: Track) {
        val service = MusicPlaybackService.instance ?: return
        service.playTrack(track)
        viewModelScope.launch {
            repository.recordTrackPlayed(track.id)
        }
    }

    fun togglePlayPause() {
        MusicPlaybackService.instance?.togglePlayPause()
    }

    fun nextTrack() {
        val service = MusicPlaybackService.instance ?: return
        service.next()
        service.currentTrack.value?.let { track ->
            viewModelScope.launch {
                repository.recordTrackPlayed(track.id)
            }
        }
    }

    fun previousTrack() {
        val service = MusicPlaybackService.instance ?: return
        service.previous()
        service.currentTrack.value?.let { track ->
            viewModelScope.launch {
                repository.recordTrackPlayed(track.id)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        MusicPlaybackService.instance?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        MusicPlaybackService.instance?.toggleShuffle()
    }

    fun cycleRepeatMode() {
        MusicPlaybackService.instance?.cycleRepeatMode()
    }

    fun addToQueue(track: Track) {
        MusicPlaybackService.instance?.addToQueue(track)
    }

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId)
        }
    }

    // Playlists
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    suspend fun getPlaylistTracks(playlistId: Long): List<Track> {
        return repository.getTracksForPlaylist(playlistId)
    }

    // Volume Gesture Handling (Swipe up/down on left side)
    fun adjustVolumeDelta(deltaRatio: Float) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val deltaSteps = (deltaRatio * maxVolume).toInt().coerceIn(-2, 2)
        if (deltaSteps != 0) {
            val newVolume = (currentVolume + deltaSteps).coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
            _volumePercent.value = ((newVolume.toFloat() / maxVolume.toFloat()) * 100).toInt()
            showVolumeHud()
        }
    }

    private fun getDeviceVolumePercent(): Int {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return ((currentVolume.toFloat() / maxVolume.toFloat()) * 100).toInt()
    }

    private fun showVolumeHud() {
        _isVolumeHudVisible.value = true
        volumeHudJob?.cancel()
        volumeHudJob = viewModelScope.launch {
            delay(1800)
            _isVolumeHudVisible.value = false
        }
    }

    // Equalizer & Sound Boost
    fun setVolumeBoost(enabled: Boolean, percent: Int) {
        MusicPlaybackService.instance?.equalizerManager?.setVolumeBoost(enabled, percent)
    }

    fun setBassBoost(enabled: Boolean, strength: Int) {
        MusicPlaybackService.instance?.equalizerManager?.setBassBoost(enabled, strength)
    }

    fun setEqualizerPreset(presetIndex: Int) {
        MusicPlaybackService.instance?.equalizerManager?.setEqualizerPreset(presetIndex)
    }

    fun getEqualizerPresetNames(): List<String> {
        return MusicPlaybackService.instance?.equalizerManager?.getPresetNames() ?: emptyList()
    }

    // Sleep Timer
    fun setSleepTimer(minutes: Int) {
        MusicPlaybackService.instance?.setSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        MusicPlaybackService.instance?.cancelSleepTimer()
    }

    // Voice Commands
    fun startVoiceListening(isTrainingMode: Boolean = false, trainingAction: VoiceAction? = null) {
        voiceManager.startListening(isTrainingMode, trainingAction)
    }

    fun stopVoiceListening() {
        voiceManager.stopListening()
    }

    fun saveVoiceCommand(phrase: String, action: VoiceAction) {
        viewModelScope.launch {
            repository.saveVoiceCommand(phrase, action)
        }
    }

    fun deleteVoiceCommand(commandId: Long) {
        viewModelScope.launch {
            repository.deleteVoiceCommand(commandId)
        }
    }

    // Settings
    fun setLogoLetter(letter: String) {
        viewModelScope.launch {
            repository.setLogoLetter(letter)
        }
    }

    fun setMusicFolder(uri: Uri) {
        viewModelScope.launch {
            repository.setMusicFolder(uri.toString())
            repository.rescanLibrary(uri)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            val folderStr = repository.selectedMusicFolder.value
            val folderUri = folderStr?.let { Uri.parse(it) }
            repository.rescanLibrary(folderUri)
        }
    }
}
