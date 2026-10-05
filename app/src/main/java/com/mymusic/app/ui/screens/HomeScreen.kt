package com.mymusic.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mymusic.app.model.Album
import com.mymusic.app.model.Playlist
import com.mymusic.app.model.Track
import com.mymusic.app.ui.components.DynamicLogo
import com.mymusic.app.ui.components.TrackItemRow
import com.mymusic.app.ui.theme.BrandGreenAccent
import com.mymusic.app.ui.theme.BrandGreenDarkContainer
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextLowEmphasis
import com.mymusic.app.ui.theme.TextMediumEmphasis
import com.mymusic.app.ui.viewmodel.LibraryTab
import com.mymusic.app.ui.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onOpenVoiceCommands: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenPlaylistDetail: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allTracks by viewModel.filteredTracks.collectAsState()
    val favorites by viewModel.filteredFavorites.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val logoLetter by viewModel.logoLetter.collectAsState()

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setMusicFolder(uri)
        }
    }

    var selectedAlbumForTracks by remember { mutableStateOf<Album?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNearBlack)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DynamicLogo(
                    letter = logoLetter,
                    size = 40.dp,
                    isPlaying = isPlaying
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "MY MUSIC",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextHighEmphasis,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Local Library",
                        fontSize = 12.sp,
                        color = BrandGreenPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Voice Command Quick Mic Button
                IconButton(
                    onClick = onOpenVoiceCommands,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BrandGreenDarkContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Voice Commands",
                        tint = BrandGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Refresh / Scan folder button
                IconButton(
                    onClick = { folderPickerLauncher.launch(null) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = "Choose Folder",
                        tint = TextHighEmphasis,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search songs, artists, albums...", color = TextLowEmphasis, fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = "Search", tint = BrandGreenPrimary, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = TextMediumEmphasis, modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = BrandGreenPrimary,
                unfocusedBorderColor = Color(0x1AFFFFFF),
                cursorColor = BrandGreenPrimary,
                focusedTextColor = TextHighEmphasis,
                unfocusedTextColor = TextHighEmphasis
            )
        )

        // Quick Category Filter Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Pair(LibraryTab.TRACKS, "Tracks (${allTracks.size})"),
                Pair(LibraryTab.ALBUMS, "Albums (${albums.size})"),
                Pair(LibraryTab.PLAYLISTS, "Playlists (${playlists.size})"),
                Pair(LibraryTab.FAVORITES, "Favorites (${favorites.size})")
            )

            items(tabs) { (tab, title) ->
                val isSelected = selectedTab == tab && selectedAlbumForTracks == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) BrandGreenPrimary else SurfaceDark
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrandGreenPrimary else Color(0x1AFFFFFF),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            selectedAlbumForTracks = null
                            viewModel.selectTab(tab)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) DeepNearBlack else TextHighEmphasis
                    )
                }
            }
        }

        // Content Area based on Tab
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recently Played Section (shown when on Tracks tab and not searching)
            if (selectedTab == LibraryTab.TRACKS && searchQuery.isBlank() && recentlyPlayed.isNotEmpty() && selectedAlbumForTracks == null) {
                item {
                    Text(
                        text = "Recently Played",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 8.dp)
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(recentlyPlayed) { track ->
                            RecentTrackCard(
                                track = track,
                                isPlaying = isPlaying && currentTrack?.id == track.id,
                                logoLetter = logoLetter,
                                onClick = { viewModel.playSingleTrack(track) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All Tracks",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
            }

            // Albums Tab
            if (selectedTab == LibraryTab.ALBUMS && selectedAlbumForTracks == null) {
                items(albums) { album ->
                    AlbumItemRow(
                        album = album,
                        logoLetter = logoLetter,
                        onClick = { selectedAlbumForTracks = album }
                    )
                }
            }

            // Album Drilldown Track List
            if (selectedAlbumForTracks != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedAlbumForTracks = null }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Back", tint = BrandGreenPrimary)
                        }
                        Text(
                            text = selectedAlbumForTracks!!.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighEmphasis
                        )
                    }
                }
                val albumTracks = allTracks.filter { it.album == selectedAlbumForTracks!!.title }
                itemsIndexed(albumTracks) { index, track ->
                    TrackItemRow(
                        track = track,
                        isPlaying = isPlaying,
                        isCurrentTrack = currentTrack?.id == track.id,
                        logoLetter = logoLetter,
                        onTrackClick = { viewModel.playTrackList(albumTracks, index) },
                        onToggleFavorite = { viewModel.toggleFavorite(track.id) },
                        onAddToQueue = { viewModel.addToQueue(track) },
                        onAddToPlaylist = onOpenPlaylists
                    )
                }
            }

            // Playlists Tab
            if (selectedTab == LibraryTab.PLAYLISTS && selectedAlbumForTracks == null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playlists",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighEmphasis
                        )
                        Button(
                            onClick = onOpenPlaylists,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = DeepNearBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Playlist", color = DeepNearBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                items(playlists) { playlist ->
                    PlaylistItemRow(
                        playlist = playlist,
                        logoLetter = logoLetter,
                        onClick = { onOpenPlaylistDetail(playlist) }
                    )
                }
            }

            // Favorites Tab
            if (selectedTab == LibraryTab.FAVORITES && selectedAlbumForTracks == null) {
                if (favorites.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No favorites yet",
                            subtitle = "Songs you love will appear here. Tap the heart on any track to favorite.",
                            buttonText = null,
                            onButtonClick = {},
                            logoLetter = logoLetter
                        )
                    }
                } else {
                    itemsIndexed(favorites) { index, track ->
                        TrackItemRow(
                            track = track,
                            isPlaying = isPlaying,
                            isCurrentTrack = currentTrack?.id == track.id,
                            logoLetter = logoLetter,
                            onTrackClick = { viewModel.playTrackList(favorites, index) },
                            onToggleFavorite = { viewModel.toggleFavorite(track.id) },
                            onAddToQueue = { viewModel.addToQueue(track) },
                            onAddToPlaylist = onOpenPlaylists
                        )
                    }
                }
            }

            // Tracks Tab (Default)
            if (selectedTab == LibraryTab.TRACKS && selectedAlbumForTracks == null) {
                if (allTracks.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No music found",
                            subtitle = "Choose your music folder to scan and get started.",
                            buttonText = "CHOOSE FOLDER",
                            onButtonClick = { folderPickerLauncher.launch(null) },
                            logoLetter = logoLetter
                        )
                    }
                } else {
                    itemsIndexed(allTracks) { index, track ->
                        TrackItemRow(
                            track = track,
                            isPlaying = isPlaying,
                            isCurrentTrack = currentTrack?.id == track.id,
                            logoLetter = logoLetter,
                            onTrackClick = { viewModel.playTrackList(allTracks, index) },
                            onToggleFavorite = { viewModel.toggleFavorite(track.id) },
                            onAddToQueue = { viewModel.addToQueue(track) },
                            onAddToPlaylist = onOpenPlaylists
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTrackCard(
    track: Track,
    isPlaying: Boolean,
    logoLetter: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .size(114.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (track.albumArtUriString != null) {
                    AsyncImage(
                        model = track.albumArtUriString,
                        contentDescription = track.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    DynamicLogo(
                        letter = logoLetter,
                        size = 64.dp,
                        isPlaying = isPlaying
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = track.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) BrandGreenPrimary else TextHighEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                fontSize = 11.sp,
                color = TextMediumEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AlbumItemRow(
    album: Album,
    logoLetter: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (album.albumArtUriString != null) {
                AsyncImage(
                    model = album.albumArtUriString,
                    contentDescription = album.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                DynamicLogo(letter = logoLetter, size = 52.dp)
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${album.artist} • ${album.trackCount} tracks",
                fontSize = 13.sp,
                color = TextMediumEmphasis
            )
        }
    }
}

@Composable
fun PlaylistItemRow(
    playlist: Playlist,
    logoLetter: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BrandGreenDarkContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.QueueMusic,
                contentDescription = null,
                tint = BrandGreenPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis
            )
            Text(
                text = "${playlist.trackCount} tracks",
                fontSize = 13.sp,
                color = TextMediumEmphasis
            )
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    buttonText: String?,
    onButtonClick: () -> Unit,
    logoLetter: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DynamicLogo(letter = logoLetter, size = 80.dp)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextHighEmphasis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = TextMediumEmphasis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (buttonText != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(buttonText, color = DeepNearBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
