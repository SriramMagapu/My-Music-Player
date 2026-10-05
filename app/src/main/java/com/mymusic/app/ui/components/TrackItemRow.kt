package com.mymusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mymusic.app.model.Track
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextLowEmphasis
import com.mymusic.app.ui.theme.TextMediumEmphasis

@Composable
fun TrackItemRow(
    track: Track,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    logoLetter: String = "M",
    onTrackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrentTrack) SurfaceElevated else SurfaceDark.copy(alpha = 0.5f))
            .clickable(onClick = onTrackClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
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
                    size = 48.dp,
                    isPlaying = isCurrentTrack && isPlaying
                )
            }

            if (isCurrentTrack) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(SurfaceDark.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    EqualizerBarsAnimation(
                        isPlaying = isPlaying,
                        barHeight = 16.dp,
                        barWidth = 3.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title & Artist Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                fontSize = 15.sp,
                fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrentTrack) BrandGreenPrimary else TextHighEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.artist,
                fontSize = 13.sp,
                color = TextMediumEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(
            text = track.durationFormatted,
            fontSize = 12.sp,
            color = TextLowEmphasis
        )

        // 3-Dot Overflow Menu
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More actions",
                    tint = TextMediumEmphasis,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(SurfaceElevated)
            ) {
                DropdownMenuItem(
                    text = { Text("Add to Queue", color = TextHighEmphasis) },
                    leadingIcon = {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = BrandGreenPrimary)
                    },
                    onClick = {
                        menuExpanded = false
                        onAddToQueue()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = TextHighEmphasis) },
                    leadingIcon = {
                        Icon(Icons.Rounded.PlaylistAdd, contentDescription = null, tint = BrandGreenPrimary)
                    },
                    onClick = {
                        menuExpanded = false
                        onAddToPlaylist()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            if (track.isFavorite) "Remove Favorite" else "Add to Favorites",
                            color = TextHighEmphasis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = BrandGreenPrimary
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onToggleFavorite()
                    }
                )
            }
        }
    }
}
