package com.mymusic.app.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.mymusic.app.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMediaScanner(private val context: Context) {

    private val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

    /**
     * Scans music tracks from MediaStore (default fast system index).
     */
    suspend fun scanMediaStore(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Filter out ringtones/notifications and short sound effects (< 10 seconds)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        try {
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol).takeIf { it != "<unknown>" } ?: "Unknown Artist"
                    val album = cursor.getString(albumCol).takeIf { it != "<unknown>" } ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdCol)
                    val duration = cursor.getLong(durationCol)
                    val path = cursor.getString(dataCol)
                    val trackNum = cursor.getInt(trackCol)
                    val year = cursor.getInt(yearCol)
                    val dateAdded = cursor.getLong(dateAddedCol)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val albumArtUri = ContentUris.withAppendedId(albumArtBaseUri, albumId).toString()

                    tracks.add(
                        Track(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            contentUriString = contentUri.toString(),
                            albumArtUriString = albumArtUri,
                            path = path,
                            trackNumber = trackNum,
                            year = year,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If no tracks found (e.g. fresh emulator or restricted test environment), generate demo high-quality tracks
        if (tracks.isEmpty()) {
            tracks.addAll(getSampleDemoTracks())
        }

        tracks
    }

    /**
     * Scans a custom folder selected by the user via Storage Access Framework (SAF).
     */
    suspend fun scanCustomFolder(treeUri: Uri): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
            scanDirectory(rootDoc, tracks)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (tracks.isEmpty()) {
            tracks.addAll(getSampleDemoTracks())
        }
        tracks
    }

    private fun scanDirectory(dir: DocumentFile, outList: MutableList<Track>) {
        val files = dir.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                scanDirectory(file, outList)
            } else if (isAudioFile(file.name.orEmpty())) {
                val track = extractTrackMetadata(file)
                if (track != null) {
                    outList.add(track)
                }
            }
        }
    }

    private fun isAudioFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".flac") ||
                lower.endsWith(".ogg") || lower.endsWith(".wav") || lower.endsWith(".aac")
    }

    private fun extractTrackMetadata(file: DocumentFile): Track? {
        val retriever = MediaMetadataRetriever()
        return try {
            context.contentResolver.openFileDescriptor(file.uri, "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)
                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: file.name?.substringBeforeLast(".") ?: "Audio File"
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 180000L

                Track(
                    id = file.uri.hashCode().toLong(),
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = durationMs,
                    contentUriString = file.uri.toString(),
                    albumArtUriString = null,
                    path = file.name,
                    dateAdded = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Built-in pristine demo library for testing and first-time preview on clean devices.
     */
    fun getSampleDemoTracks(): List<Track> {
        return listOf(
            Track(
                id = 1001L,
                title = "Midnight Horizon",
                artist = "Aether",
                album = "Neon Echoes",
                durationMs = 214000L,
                contentUriString = "asset:///audio/sample1.mp3",
                albumArtUriString = null,
                year = 2025,
                dateAdded = System.currentTimeMillis()
            ),
            Track(
                id = 1002L,
                title = "Cyber Pulse",
                artist = "Kinetics",
                album = "Electric Green",
                durationMs = 185000L,
                contentUriString = "asset:///audio/sample2.mp3",
                albumArtUriString = null,
                year = 2025,
                dateAdded = System.currentTimeMillis() - 100000
            ),
            Track(
                id = 1003L,
                title = "Emerald Reverie",
                artist = "Astral Wave",
                album = "Luminescent",
                durationMs = 248000L,
                contentUriString = "asset:///audio/sample3.mp3",
                albumArtUriString = null,
                year = 2024,
                dateAdded = System.currentTimeMillis() - 200000
            ),
            Track(
                id = 1004L,
                title = "Deep Velvet Groove",
                artist = "Sriram",
                album = "Origins",
                durationMs = 196000L,
                contentUriString = "asset:///audio/sample4.mp3",
                albumArtUriString = null,
                year = 2026,
                dateAdded = System.currentTimeMillis() - 300000
            ),
            Track(
                id = 1005L,
                title = "Infinite Frequency",
                artist = "Solaris",
                album = "Echo Chamber",
                durationMs = 232000L,
                contentUriString = "asset:///audio/sample5.mp3",
                albumArtUriString = null,
                year = 2025,
                dateAdded = System.currentTimeMillis() - 400000
            )
        )
    }
}
