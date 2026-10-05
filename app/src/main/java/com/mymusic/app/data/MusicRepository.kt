package com.mymusic.app.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.mymusic.app.model.Album
import com.mymusic.app.model.Artist
import com.mymusic.app.model.CustomVoiceCommand
import com.mymusic.app.model.Playlist
import com.mymusic.app.model.Track
import com.mymusic.app.model.VoiceAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {

    private val dbHelper = MusicDatabaseHelper(context)
    private val scanner = LocalMediaScanner(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    private val _favorites = MutableStateFlow<List<Track>>(emptyList())
    val favorites: StateFlow<List<Track>> = _favorites.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Track>>(emptyList())
    val recentlyPlayed: StateFlow<List<Track>> = _recentlyPlayed.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _voiceCommands = MutableStateFlow<List<CustomVoiceCommand>>(emptyList())
    val voiceCommands: StateFlow<List<CustomVoiceCommand>> = _voiceCommands.asStateFlow()

    private val _logoLetter = MutableStateFlow("M")
    val logoLetter: StateFlow<String> = _logoLetter.asStateFlow()

    private val _selectedMusicFolder = MutableStateFlow<String?>(null)
    val selectedMusicFolder: StateFlow<String?> = _selectedMusicFolder.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    init {
        loadSettings()
        loadInitialData()
    }

    private fun loadSettings() {
        scope.launch {
            val db = dbHelper.readableDatabase
            try {
                db.rawQuery("SELECT key, value FROM ${MusicDatabaseHelper.TABLE_SETTINGS}", null).use { cursor ->
                    val keyCol = cursor.getColumnIndexOrThrow("key")
                    val valCol = cursor.getColumnIndexOrThrow("value")
                    while (cursor.moveToNext()) {
                        when (cursor.getString(keyCol)) {
                            "logo_letter" -> _logoLetter.value = cursor.getString(valCol)
                            "music_folder" -> _selectedMusicFolder.value = cursor.getString(valCol)
                            "onboarding_done" -> _hasCompletedOnboarding.value = cursor.getString(valCol) == "true"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadInitialData() {
        scope.launch {
            loadTracksFromDb()
            loadPlaylists()
            loadVoiceCommands()
            // Perform initial media scan if db is empty
            if (_allTracks.value.isEmpty()) {
                rescanLibrary()
            }
        }
    }

    suspend fun rescanLibrary(customFolderUri: Uri? = null) = withContext(Dispatchers.IO) {
        val scannedTracks = if (customFolderUri != null) {
            scanner.scanCustomFolder(customFolderUri)
        } else {
            scanner.scanMediaStore()
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (track in scannedTracks) {
                val cv = ContentValues().apply {
                    put("id", track.id)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("album", track.album)
                    put("duration", track.durationMs)
                    put("content_uri", track.contentUriString)
                    put("album_art_uri", track.albumArtUriString)
                    put("path", track.path)
                    put("track_number", track.trackNumber)
                    put("year", track.year)
                    put("date_added", track.dateAdded)
                }
                db.insertWithOnConflict(
                    MusicDatabaseHelper.TABLE_TRACKS,
                    null,
                    cv,
                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        loadTracksFromDb()
    }

    private suspend fun loadTracksFromDb() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val favIds = getFavoriteTrackIds()
        val list = mutableListOf<Track>()

        try {
            db.rawQuery("SELECT * FROM ${MusicDatabaseHelper.TABLE_TRACKS} ORDER BY title ASC", null).use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow("id")
                val titleCol = cursor.getColumnIndexOrThrow("title")
                val artistCol = cursor.getColumnIndexOrThrow("artist")
                val albumCol = cursor.getColumnIndexOrThrow("album")
                val durCol = cursor.getColumnIndexOrThrow("duration")
                val uriCol = cursor.getColumnIndexOrThrow("content_uri")
                val artCol = cursor.getColumnIndexOrThrow("album_art_uri")
                val pathCol = cursor.getColumnIndexOrThrow("path")
                val trackNumCol = cursor.getColumnIndexOrThrow("track_number")
                val yearCol = cursor.getColumnIndexOrThrow("year")
                val dateAddedCol = cursor.getColumnIndexOrThrow("date_added")
                val playCountCol = cursor.getColumnIndexOrThrow("play_count")
                val lastPlayedCol = cursor.getColumnIndexOrThrow("last_played")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    list.add(
                        Track(
                            id = id,
                            title = cursor.getString(titleCol),
                            artist = cursor.getString(artistCol),
                            album = cursor.getString(albumCol),
                            durationMs = cursor.getLong(durCol),
                            contentUriString = cursor.getString(uriCol),
                            albumArtUriString = cursor.getString(artCol),
                            path = cursor.getString(pathCol),
                            trackNumber = cursor.getInt(trackNumCol),
                            year = cursor.getInt(yearCol),
                            dateAdded = cursor.getLong(dateAddedCol),
                            isFavorite = favIds.contains(id),
                            playCount = cursor.getInt(playCountCol),
                            lastPlayedTimestamp = cursor.getLong(lastPlayedCol)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _allTracks.value = list
        _favorites.value = list.filter { it.isFavorite }
        _recentlyPlayed.value = list.filter { it.lastPlayedTimestamp > 0 }
            .sortedByDescending { it.lastPlayedTimestamp }
            .take(15)
    }

    private fun getFavoriteTrackIds(): Set<Long> {
        val set = mutableSetOf<Long>()
        val db = dbHelper.readableDatabase
        try {
            db.rawQuery("SELECT track_id FROM ${MusicDatabaseHelper.TABLE_FAVORITES}", null).use { cursor ->
                val col = cursor.getColumnIndexOrThrow("track_id")
                while (cursor.moveToNext()) {
                    set.add(cursor.getLong(col))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return set
    }

    suspend fun toggleFavorite(trackId: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val isFav = getFavoriteTrackIds().contains(trackId)
        if (isFav) {
            db.delete(MusicDatabaseHelper.TABLE_FAVORITES, "track_id = ?", arrayOf(trackId.toString()))
        } else {
            val cv = ContentValues().apply {
                put("track_id", trackId)
                put("created_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict(
                MusicDatabaseHelper.TABLE_FAVORITES,
                null,
                cv,
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
            )
        }
        loadTracksFromDb()
    }

    suspend fun recordTrackPlayed(trackId: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val now = System.currentTimeMillis()
        db.execSQL("UPDATE ${MusicDatabaseHelper.TABLE_TRACKS} SET play_count = play_count + 1, last_played = $now WHERE id = $trackId")
        val cv = ContentValues().apply {
            put("track_id", trackId)
            put("played_at", now)
        }
        db.insertWithOnConflict(MusicDatabaseHelper.TABLE_HISTORY, null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
        loadTracksFromDb()
    }

    // Playlists
    suspend fun loadPlaylists() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Playlist>()
        try {
            db.rawQuery("SELECT p.id, p.name, p.created_date, p.artwork_uri, COUNT(pt.track_id) as track_count FROM ${MusicDatabaseHelper.TABLE_PLAYLISTS} p LEFT JOIN ${MusicDatabaseHelper.TABLE_PLAYLIST_TRACKS} pt ON p.id = pt.playlist_id GROUP BY p.id", null).use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow("id")
                val nameCol = cursor.getColumnIndexOrThrow("name")
                val dateCol = cursor.getColumnIndexOrThrow("created_date")
                val artCol = cursor.getColumnIndexOrThrow("artwork_uri")
                val countCol = cursor.getColumnIndexOrThrow("track_count")

                while (cursor.moveToNext()) {
                    list.add(
                        Playlist(
                            id = cursor.getLong(idCol),
                            name = cursor.getString(nameCol),
                            createdDate = cursor.getLong(dateCol),
                            trackCount = cursor.getInt(countCol),
                            customArtworkUriString = cursor.getString(artCol)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _playlists.value = list
    }

    suspend fun createPlaylist(name: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", name.trim())
            put("created_date", System.currentTimeMillis())
        }
        db.insert(MusicDatabaseHelper.TABLE_PLAYLISTS, null, cv)
        loadPlaylists()
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(MusicDatabaseHelper.TABLE_PLAYLIST_TRACKS, "playlist_id = ?", arrayOf(playlistId.toString()))
        db.delete(MusicDatabaseHelper.TABLE_PLAYLISTS, "id = ?", arrayOf(playlistId.toString()))
        loadPlaylists()
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("playlist_id", playlistId)
            put("track_id", trackId)
            put("position", System.currentTimeMillis())
        }
        db.insertWithOnConflict(
            MusicDatabaseHelper.TABLE_PLAYLIST_TRACKS,
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        loadPlaylists()
    }

    suspend fun getTracksForPlaylist(playlistId: Long): List<Track> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val trackIds = mutableListOf<Long>()
        try {
            db.rawQuery("SELECT track_id FROM ${MusicDatabaseHelper.TABLE_PLAYLIST_TRACKS} WHERE playlist_id = ? ORDER BY position ASC", arrayOf(playlistId.toString())).use { cursor ->
                val col = cursor.getColumnIndexOrThrow("track_id")
                while (cursor.moveToNext()) {
                    trackIds.add(cursor.getLong(col))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val currentTracks = _allTracks.value.associateBy { it.id }
        trackIds.mapNotNull { currentTracks[it] }
    }

    // Voice Commands
    suspend fun loadVoiceCommands() = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<CustomVoiceCommand>()
        try {
            db.rawQuery("SELECT * FROM ${MusicDatabaseHelper.TABLE_VOICE_COMMANDS} ORDER BY created_at DESC", null).use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow("id")
                val trigCol = cursor.getColumnIndexOrThrow("trigger_phrase")
                val normCol = cursor.getColumnIndexOrThrow("normalized_phrase")
                val actCol = cursor.getColumnIndexOrThrow("action")
                val targetCol = cursor.getColumnIndexOrThrow("target_param")
                val dateCol = cursor.getColumnIndexOrThrow("created_at")

                while (cursor.moveToNext()) {
                    val actionName = cursor.getString(actCol)
                    val action = try { VoiceAction.valueOf(actionName) } catch (_: Exception) { VoiceAction.PLAY }
                    list.add(
                        CustomVoiceCommand(
                            id = cursor.getLong(idCol),
                            triggerPhrase = cursor.getString(trigCol),
                            normalizedPhrase = cursor.getString(normCol),
                            action = action,
                            targetParam = cursor.getString(targetCol),
                            createdAt = cursor.getLong(dateCol)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _voiceCommands.value = list
    }

    suspend fun saveVoiceCommand(triggerPhrase: String, action: VoiceAction, targetParam: String? = null) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val normalized = triggerPhrase.trim().lowercase()
        val cv = ContentValues().apply {
            put("trigger_phrase", triggerPhrase.trim())
            put("normalized_phrase", normalized)
            put("action", action.name)
            put("target_param", targetParam)
            put("created_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict(
            MusicDatabaseHelper.TABLE_VOICE_COMMANDS,
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        loadVoiceCommands()
    }

    suspend fun deleteVoiceCommand(commandId: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(MusicDatabaseHelper.TABLE_VOICE_COMMANDS, "id = ?", arrayOf(commandId.toString()))
        loadVoiceCommands()
    }

    // User Settings
    suspend fun setLogoLetter(letter: String) = withContext(Dispatchers.IO) {
        val sanitized = letter.trim().take(1).uppercase().ifEmpty { "M" }
        saveSetting("logo_letter", sanitized)
        _logoLetter.value = sanitized
    }

    suspend fun setMusicFolder(uriString: String) = withContext(Dispatchers.IO) {
        saveSetting("music_folder", uriString)
        _selectedMusicFolder.value = uriString
    }

    suspend fun setOnboardingCompleted(completed: Boolean) = withContext(Dispatchers.IO) {
        saveSetting("onboarding_done", completed.toString())
        _hasCompletedOnboarding.value = completed
    }

    private fun saveSetting(key: String, value: String) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        db.insertWithOnConflict(
            MusicDatabaseHelper.TABLE_SETTINGS,
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // Helper grouping methods
    fun getAlbums(): List<Album> {
        val tracks = _allTracks.value
        return tracks.groupBy { it.album }
            .map { (albumName, albumTracks) ->
                val first = albumTracks.first()
                Album(
                    id = albumName.hashCode().toLong(),
                    title = albumName,
                    artist = first.artist,
                    trackCount = albumTracks.size,
                    year = first.year,
                    albumArtUriString = first.albumArtUriString
                )
            }
            .sortedBy { it.title }
    }

    fun getArtists(): List<Artist> {
        val tracks = _allTracks.value
        return tracks.groupBy { it.artist }
            .map { (artistName, artistTracks) ->
                Artist(
                    name = artistName,
                    trackCount = artistTracks.size,
                    albumCount = artistTracks.map { it.album }.distinct().size
                )
            }
            .sortedBy { it.name }
    }
}
