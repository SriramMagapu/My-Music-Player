package com.mymusic.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymusic.app.model.Playlist
import com.mymusic.app.model.Track
import com.mymusic.app.ui.components.TrackItemRow
import com.mymusic.app.ui.theme.BrandGreenDarkContainer
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.ErrorRed
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextLowEmphasis
import com.mymusic.app.ui.theme.TextMediumEmphasis
import com.mymusic.app.ui.viewmodel.MusicViewModel

@Composable
fun PlaylistsScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    onOpenPlaylistDetail: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val logoLetter by viewModel.logoLetter.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNearBlack)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextHighEmphasis)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Playlists",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = DeepNearBlack, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", color = DeepNearBlack, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistItemRow(
                    playlist = playlist,
                    logoLetter = logoLetter,
                    onClick = { onOpenPlaylistDetail(playlist) }
                )
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SurfaceElevated,
            title = {
                Text("Create Playlist", color = TextHighEmphasis, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name", color = BrandGreenPrimary) },
                    placeholder = { Text("e.g. Night Vibes", color = TextLowEmphasis) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = BrandGreenPrimary,
                        unfocusedBorderColor = Color(0x1AFFFFFF),
                        cursorColor = BrandGreenPrimary,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                ) {
                    Text("Create", color = DeepNearBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextMediumEmphasis)
                }
            }
        )
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val logoLetter by viewModel.logoLetter.collectAsState()

    LaunchedEffect(playlist.id) {
        tracks = viewModel.getPlaylistTracks(playlist.id)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNearBlack)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextHighEmphasis)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = playlist.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis
                    )
                    Text(
                        text = "${tracks.size} tracks",
                        fontSize = 12.sp,
                        color = TextMediumEmphasis
                    )
                }
            }

            Row {
                if (tracks.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.playTrackList(tracks, 0) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = DeepNearBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play All", color = DeepNearBlack, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.deletePlaylist(playlist.id)
                        onBack()
                    }
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete Playlist", tint = ErrorRed)
                }
            }
        }

        if (tracks.isEmpty()) {
            EmptyStateView(
                title = "Playlist is empty",
                subtitle = "Add songs to this playlist from any song's three-dot menu.",
                buttonText = null,
                onButtonClick = {},
                logoLetter = logoLetter
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tracks) { index, track ->
                    TrackItemRow(
                        track = track,
                        isPlaying = isPlaying,
                        isCurrentTrack = currentTrack?.id == track.id,
                        logoLetter = logoLetter,
                        onTrackClick = { viewModel.playTrackList(tracks, index) },
                        onToggleFavorite = { viewModel.toggleFavorite(track.id) },
                        onAddToQueue = { viewModel.addToQueue(track) },
                        onAddToPlaylist = {}
                    )
                }
            }
        }
    }
}
