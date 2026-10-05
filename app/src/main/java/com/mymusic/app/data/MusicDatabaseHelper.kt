package com.mymusic.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mymusic.app.model.CustomVoiceCommand
import com.mymusic.app.model.Playlist
import com.mymusic.app.model.Track
import com.mymusic.app.model.VoiceAction

class MusicDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mymusic.db"
        private const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_TRACKS = "tracks"
        const val TABLE_FAVORITES = "favorites"
        const val TABLE_PLAYLISTS = "playlists"
        const val TABLE_PLAYLIST_TRACKS = "playlist_tracks"
        const val TABLE_HISTORY = "history"
        const val TABLE_VOICE_COMMANDS = "voice_commands"
        const val TABLE_SETTINGS = "settings"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_TRACKS (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                artist TEXT NOT NULL,
                album TEXT NOT NULL,
                duration INTEGER NOT NULL,
                content_uri TEXT NOT NULL,
                album_art_uri TEXT,
                path TEXT,
                track_number INTEGER DEFAULT 0,
                year INTEGER DEFAULT 0,
                date_added INTEGER NOT NULL,
                play_count INTEGER DEFAULT 0,
                last_played INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_FAVORITES (
                track_id INTEGER PRIMARY KEY,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_PLAYLISTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                created_date INTEGER NOT NULL,
                artwork_uri TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_PLAYLIST_TRACKS (
                playlist_id INTEGER,
                track_id INTEGER,
                position INTEGER,
                PRIMARY KEY (playlist_id, track_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_HISTORY (
                track_id INTEGER,
                played_at INTEGER,
                PRIMARY KEY (track_id, played_at)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_VOICE_COMMANDS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                trigger_phrase TEXT NOT NULL UNIQUE,
                normalized_phrase TEXT NOT NULL,
                action TEXT NOT NULL,
                target_param TEXT,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_SETTINGS (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """.trimIndent())

        // Insert default playlists
        db.execSQL("INSERT INTO $TABLE_PLAYLISTS (name, created_date) VALUES ('My Top Tracks', ${System.currentTimeMillis()})")
        db.execSQL("INSERT INTO $TABLE_PLAYLISTS (name, created_date) VALUES ('Chill Mix', ${System.currentTimeMillis()})")
        db.execSQL("INSERT INTO $TABLE_PLAYLISTS (name, created_date) VALUES ('Energy Boost', ${System.currentTimeMillis()})")

        // Seed default voice commands
        seedDefaultVoiceCommands(db)
    }

    private fun seedDefaultVoiceCommands(db: SQLiteDatabase) {
        val defaults = listOf(
            Triple("play music", "play music", VoiceAction.PLAY),
            Triple("pause", "pause", VoiceAction.PAUSE),
            Triple("next song", "next song", VoiceAction.NEXT),
            Triple("skip", "skip", VoiceAction.NEXT),
            Triple("previous song", "previous song", VoiceAction.PREVIOUS),
            Triple("favorite this", "favorite this", VoiceAction.FAVORITE),
            Triple("shuffle all", "shuffle all", VoiceAction.SHUFFLE),
            Triple("volume up", "volume up", VoiceAction.VOLUME_UP),
            Triple("volume down", "volume down", VoiceAction.VOLUME_DOWN),
            Triple("play favorites", "play favorites", VoiceAction.PLAY_FAVORITES)
        )
        for ((trigger, norm, action) in defaults) {
            val cv = ContentValues().apply {
                put("trigger_phrase", trigger)
                put("normalized_phrase", norm)
                put("action", action.name)
                put("created_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict(TABLE_VOICE_COMMANDS, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration logic if schema changes in future versions
    }
}
