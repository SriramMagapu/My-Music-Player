package com.mymusic.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode as ComposeRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mymusic.app.model.RepeatMode as AppRepeatMode
import com.mymusic.app.ui.components.DynamicLogo
import com.mymusic.app.ui.components.TrackItemRow
import com.mymusic.app.ui.components.TransparentVolumeHud
import com.mymusic.app.ui.theme.BrandGreenAccent
import com.mymusic.app.ui.theme.BrandGreenDarkContainer
import com.mymusic.app.ui.theme.BrandGreenGlow
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextLowEmphasis
import com.mymusic.app.ui.theme.TextMediumEmphasis
import com.mymusic.app.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: MusicViewModel,
    onCollapse: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val isShuffleOn by viewModel.isShuffleOn.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val logoLetter by viewModel.logoLetter.collectAsState()
    val isVolumeHudVisible by viewModel.isVolumeHudVisible.collectAsState()
    val volumePercent by viewModel.volumePercent.collectAsState()
    val sleepTimerMs by viewModel.sleepTimerRemainingMs.collectAsState()

    var isQueueSheetVisible by remember { mutableStateOf(false) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }

    val track = currentTrack

    // Subtle gentle breathing animation on artwork when playing
    val infiniteTransition = rememberInfiniteTransition(label = "artBreathing")
    val artScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.025f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = ComposeRepeatMode.Reverse
        ),
        label = "artScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceElevated,
                        DeepNearBlack,
                        DeepNearBlack
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            // Vertical Drag Gesture on the Left Half of Screen for Media Player Volume HUD Control
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (change.position.x < size.width * 0.5f) {
                        change.consume()
                        // Drag up: negative amount -> volume up
                        val deltaRatio = -dragAmount / size.height
                        viewModel.adjustVolumeDelta(deltaRatio * 8f)
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextHighEmphasis,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM LIBRARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLowEmphasis,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = track?.album ?: "My Music",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandGreenPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    // Sleep Timer Trigger
                    IconButton(
                        onClick = onOpenSleepTimer,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (sleepTimerMs > 0) BrandGreenDarkContainer else SurfaceDark)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerMs > 0) BrandGreenPrimary else TextMediumEmphasis,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Equalizer Trigger
                    IconButton(
                        onClick = onOpenEqualizer,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Equalizer,
                            contentDescription = "Equalizer",
                            tint = TextHighEmphasis,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Dominant Album Artwork with Ambient Green Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .aspectRatio(1f)
                    .scale(artScale)
                    .shadow(
                        elevation = if (isPlaying) 28.dp else 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = if (isPlaying) BrandGreenPrimary.copy(alpha = 0.5f) else Color.Black,
                        spotColor = if (isPlaying) BrandGreenPrimary.copy(alpha = 0.5f) else Color.Black
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceElevated)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            if (isPlaying) listOf(BrandGreenPrimary.copy(alpha = 0.6f), Color(0x22FFFFFF))
                            else listOf(Color(0x22FFFFFF), Color(0x08FFFFFF))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (track?.albumArtUriString != null) {
                    AsyncImage(
                        model = track.albumArtUriString,
                        contentDescription = track.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    DynamicLogo(
                        letter = logoLetter,
                        size = 140.dp,
                        isPlaying = isPlaying
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Song Title & Artist + Favorite Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "No song playing",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track?.artist ?: "Select a song to start",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMediumEmphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { track?.let { viewModel.toggleFavorite(it.id) } },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (track?.isFavorite == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track?.isFavorite == true) BrandGreenPrimary else TextMediumEmphasis,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Slider
            val effectiveDuration = if (durationMs > 0) durationMs.toFloat() else 1f
            val currentSliderValue = if (isDraggingSlider) sliderDragPosition else currentPositionMs.toFloat().coerceIn(0f, effectiveDuration)

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = currentSliderValue,
                    onValueChange = {
                        isDraggingSlider = true
                        sliderDragPosition = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        viewModel.seekTo(sliderDragPosition.toLong())
                    },
                    valueRange = 0f..effectiveDuration,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandGreenPrimary,
                        activeTrackColor = BrandGreenPrimary,
                        inactiveTrackColor = Color(0x2AFFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentSec = (currentSliderValue / 1000).toLong()
                    val totalSec = (effectiveDuration / 1000).toLong()

                    Text(
                        text = "%d:%02d".format(currentSec / 60, currentSec % 60),
                        fontSize = 12.sp,
                        color = TextMediumEmphasis
                    )
                    Text(
                        text = "%d:%02d".format(totalSec / 60, totalSec % 60),
                        fontSize = 12.sp,
                        color = TextMediumEmphasis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Playback Controls: Previous, Big Play/Pause, Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Shuffle
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleOn) BrandGreenPrimary else TextLowEmphasis,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = { viewModel.previousTrack() },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextHighEmphasis,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Play / Pause Large Glow Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            ambientColor = BrandGreenPrimary,
                            spotColor = BrandGreenPrimary
                        )
                        .clip(CircleShape)
                        .background(BrandGreenPrimary)
                        .border(2.dp, Color(0x44FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.matchParentSize()
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = DeepNearBlack,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = { viewModel.nextTrack() },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = TextHighEmphasis,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Repeat
                IconButton(
                    onClick = { viewModel.cycleRepeatMode() },
                    modifier = Modifier.size(44.dp)
                ) {
                    val repeatIcon = when (repeatMode) {
                        AppRepeatMode.ONE -> Icons.Rounded.RepeatOne
                        else -> Icons.Rounded.Repeat
                    }
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != AppRepeatMode.OFF) BrandGreenPrimary else TextLowEmphasis,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Queue Sheet Trigger
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isQueueSheetVisible = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.QueueMusic,
                    contentDescription = "Queue",
                    tint = BrandGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UP NEXT (${queue.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Floating Transparent Volume HUD on Left Side Swipe Gesture
        TransparentVolumeHud(
            visible = isVolumeHudVisible,
            volumePercent = volumePercent,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 28.dp)
        )

        // Queue Modal Bottom Sheet
        if (isQueueSheetVisible) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { isQueueSheetVisible = false },
                sheetState = sheetState,
                containerColor = SurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Playback Queue (${queue.size} tracks)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    queue.forEachIndexed { index, qTrack ->
                        TrackItemRow(
                            track = qTrack,
                            isPlaying = isPlaying && currentTrack?.id == qTrack.id,
                            isCurrentTrack = currentTrack?.id == qTrack.id,
                            logoLetter = logoLetter,
                            onTrackClick = {
                                viewModel.playTrackList(queue, index)
                                isQueueSheetVisible = false
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(qTrack.id) },
                            onAddToQueue = {},
                            onAddToPlaylist = {}
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
