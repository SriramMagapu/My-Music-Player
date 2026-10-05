package com.mymusic.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mymusic.app.model.Playlist
import com.mymusic.app.ui.components.BottomNavBar
import com.mymusic.app.ui.components.MiniPlayerBar
import com.mymusic.app.ui.components.NavDestination
import com.mymusic.app.ui.screens.EqualizerDialog
import com.mymusic.app.ui.screens.HomeScreen
import com.mymusic.app.ui.screens.NowPlayingScreen
import com.mymusic.app.ui.screens.OnboardingGuideScreen
import com.mymusic.app.ui.screens.PlaylistDetailScreen
import com.mymusic.app.ui.screens.PlaylistsScreen
import com.mymusic.app.ui.screens.ProfileScreen
import com.mymusic.app.ui.screens.SleepTimerDialog
import com.mymusic.app.ui.screens.VoiceCommandsScreen
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.viewmodel.MusicViewModel

enum class AppSubScreen {
    MAIN,
    PLAYLISTS,
    PLAYLIST_DETAIL,
    VOICE_COMMANDS
}

@Composable
fun MainAppScaffold(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val logoLetter by viewModel.logoLetter.collectAsState()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()

    var selectedNav by remember { mutableStateOf(NavDestination.HOME) }
    var subScreen by remember { mutableStateOf(AppSubScreen.MAIN) }
    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }
    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showOnboardingGuide by remember { mutableStateOf(!hasCompletedOnboarding) }

    // System Back Handler
    BackHandler(enabled = isNowPlayingExpanded || subScreen != AppSubScreen.MAIN || selectedNav != NavDestination.HOME) {
        if (isNowPlayingExpanded) {
            isNowPlayingExpanded = false
        } else if (subScreen != AppSubScreen.MAIN) {
            subScreen = AppSubScreen.MAIN
        } else if (selectedNav != NavDestination.HOME) {
            selectedNav = NavDestination.HOME
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNearBlack)
    ) {
        // Main Screen View (Home or Profile or Sub-screens)
        when (subScreen) {
            AppSubScreen.MAIN -> {
                when (selectedNav) {
                    NavDestination.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onOpenVoiceCommands = { subScreen = AppSubScreen.VOICE_COMMANDS },
                            onOpenPlaylists = { subScreen = AppSubScreen.PLAYLISTS },
                            onOpenPlaylistDetail = { playlist ->
                                selectedPlaylistForDetail = playlist
                                subScreen = AppSubScreen.PLAYLIST_DETAIL
                            }
                        )
                    }
                    NavDestination.PROFILE -> {
                        ProfileScreen(
                            viewModel = viewModel,
                            onOpenVoiceCommands = { subScreen = AppSubScreen.VOICE_COMMANDS },
                            onOpenEqualizer = { showEqualizerDialog = true },
                            onOpenSleepTimer = { showSleepTimerDialog = true },
                            onShowGuide = { showOnboardingGuide = true }
                        )
                    }
                }
            }
            AppSubScreen.PLAYLISTS -> {
                PlaylistsScreen(
                    viewModel = viewModel,
                    onBack = { subScreen = AppSubScreen.MAIN },
                    onOpenPlaylistDetail = { playlist ->
                        selectedPlaylistForDetail = playlist
                        subScreen = AppSubScreen.PLAYLIST_DETAIL
                    }
                )
            }
            AppSubScreen.PLAYLIST_DETAIL -> {
                selectedPlaylistForDetail?.let { playlist ->
                    PlaylistDetailScreen(
                        playlist = playlist,
                        viewModel = viewModel,
                        onBack = { subScreen = AppSubScreen.PLAYLISTS }
                    )
                } ?: run {
                    subScreen = AppSubScreen.MAIN
                }
            }
            AppSubScreen.VOICE_COMMANDS -> {
                VoiceCommandsScreen(
                    viewModel = viewModel,
                    onBack = { subScreen = AppSubScreen.MAIN }
                )
            }
        }

        // Floating Mini Player and Bottom Nav Bar
        if (!isNowPlayingExpanded) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (currentTrack != null) {
                    MiniPlayerBar(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        progressMs = currentPositionMs,
                        durationMs = durationMs,
                        logoLetter = logoLetter,
                        onOpenNowPlaying = { isNowPlayingExpanded = true },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.nextTrack() }
                    )
                }

                BottomNavBar(
                    selectedDestination = selectedNav,
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    logoLetter = logoLetter,
                    onNavigate = {
                        subScreen = AppSubScreen.MAIN
                        selectedNav = it
                    },
                    onOpenNowPlaying = {
                        if (currentTrack != null) {
                            isNowPlayingExpanded = true
                        } else {
                            val all = viewModel.filteredTracks.value
                            if (all.isNotEmpty()) {
                                viewModel.playSingleTrack(all.first())
                                isNowPlayingExpanded = true
                            }
                        }
                    }
                )
            }
        }

        // Fullscreen Now Playing Modal Overlay
        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                viewModel = viewModel,
                onCollapse = { isNowPlayingExpanded = false },
                onOpenEqualizer = { showEqualizerDialog = true },
                onOpenSleepTimer = { showSleepTimerDialog = true }
            )
        }

        // Onboarding Guide Overlay
        if (showOnboardingGuide) {
            OnboardingGuideScreen(
                logoLetter = logoLetter,
                onComplete = {
                    showOnboardingGuide = false
                    viewModel.completeOnboarding()
                }
            )
        }

        // Equalizer Dialog
        if (showEqualizerDialog) {
            EqualizerDialog(
                viewModel = viewModel,
                onDismiss = { showEqualizerDialog = false }
            )
        }

        // Sleep Timer Dialog
        if (showSleepTimerDialog) {
            SleepTimerDialog(
                viewModel = viewModel,
                onDismiss = { showSleepTimerDialog = false }
            )
        }
    }
}
