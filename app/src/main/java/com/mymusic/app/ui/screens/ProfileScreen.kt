package com.mymusic.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymusic.app.ui.components.DynamicLogo
import com.mymusic.app.ui.theme.BrandGreenDarkContainer
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextLowEmphasis
import com.mymusic.app.ui.theme.TextMediumEmphasis
import com.mymusic.app.ui.viewmodel.MusicViewModel

@Composable
fun ProfileScreen(
    viewModel: MusicViewModel,
    onOpenVoiceCommands: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onShowGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTracks by viewModel.filteredTracks.collectAsState()
    val favorites by viewModel.filteredFavorites.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val logoLetter by viewModel.logoLetter.collectAsState()
    val selectedFolder by viewModel.selectedMusicFolder.collectAsState()
    val soundSettings by viewModel.soundSettings.collectAsState()
    val sleepTimerRemainingMs by viewModel.sleepTimerRemainingMs.collectAsState()

    var showLetterDialog by remember { mutableStateOf(false) }
    var inputLetter by remember { mutableStateOf(logoLetter) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setMusicFolder(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNearBlack)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DynamicLogo(
                        letter = logoLetter,
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MY MUSIC",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextHighEmphasis
                        )
                        Text(
                            text = "Sriram's Personal Library",
                            fontSize = 13.sp,
                            color = BrandGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${allTracks.size} Tracks • ${albums.size} Albums",
                            fontSize = 12.sp,
                            color = TextMediumEmphasis
                        )
                    }
                }
            }
        }

        // Section 1: LIBRARY
        item {
            SectionHeader(title = "LIBRARY")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingItemRow(
                        icon = Icons.Rounded.MusicNote,
                        title = "Local Tracks",
                        subtitle = "${allTracks.size} songs ready to play",
                        onClick = {}
                    )
                    SettingDivider()
                    SettingItemRow(
                        icon = Icons.Rounded.Album,
                        title = "Albums",
                        subtitle = "${albums.size} albums cataloged",
                        onClick = {}
                    )
                    SettingDivider()
                    SettingItemRow(
                        icon = Icons.Rounded.QueueMusic,
                        title = "Playlists",
                        subtitle = "${playlists.size} custom playlists",
                        onClick = {}
                    )
                    SettingDivider()
                    SettingItemRow(
                        icon = Icons.Rounded.Favorite,
                        title = "Favorites",
                        subtitle = "${favorites.size} loved songs",
                        onClick = {}
                    )
                    SettingDivider()
                    SettingItemRow(
                        icon = Icons.Rounded.Folder,
                        title = "Music Folder",
                        subtitle = selectedFolder ?: "Default MediaStore Library",
                        trailing = {
                            Text("Change", color = BrandGreenPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        },
                        onClick = { folderPickerLauncher.launch(null) }
                    )
                }
            }
        }

        // Section 2: PLAYBACK & SOUND
        item {
            SectionHeader(title = "PLAYBACK & SOUND")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingItemRow(
                        icon = Icons.Rounded.Timer,
                        title = "Sleep Timer",
                        subtitle = if (sleepTimerRemainingMs > 0) "Active: %d min remaining".format(sleepTimerRemainingMs / 60000) else "Off",
                        onClick = onOpenSleepTimer
                    )
                    SettingDivider()
                    SettingItemRow(
                        icon = Icons.Rounded.Equalizer,
                        title = "Equalizer",
                        subtitle = "Audio preset tuning & bass boost",
                        onClick = onOpenEqualizer
                    )
                    SettingDivider()
                    // Safe Volume Boost row with toggle & slider
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = BrandGreenPrimary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Safe Volume Boost", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextHighEmphasis)
                                    Text("Enhances quiet local recordings", fontSize = 12.sp, color = TextMediumEmphasis)
                                }
                            }
                            Switch(
                                checked = soundSettings.isVolumeBoostEnabled,
                                onCheckedChange = { viewModel.setVolumeBoost(it, soundSettings.volumeBoostPercent) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BrandGreenPrimary,
                                    checkedTrackColor = BrandGreenDarkContainer
                                )
                            )
                        }

                        if (soundSettings.isVolumeBoostEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Boost Level", fontSize = 12.sp, color = TextMediumEmphasis, modifier = Modifier.width(80.dp))
                                Slider(
                                    value = soundSettings.volumeBoostPercent.toFloat(),
                                    onValueChange = { viewModel.setVolumeBoost(true, it.toInt()) },
                                    valueRange = 0f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = BrandGreenPrimary,
                                        activeTrackColor = BrandGreenPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Text("${soundSettings.volumeBoostPercent}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandGreenPrimary, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 3: VOICE COMMANDS
        item {
            SectionHeader(title = "VOICE CONTROL")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingItemRow(
                        icon = Icons.Rounded.Mic,
                        title = "Voice Commands & Training",
                        subtitle = "Speak commands or teach custom phrases",
                        onClick = onOpenVoiceCommands
                    )
                }
            }
        }

        // Section 4: BRAND IDENTITY
        item {
            SectionHeader(title = "APP IDENTITY")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingItemRow(
                        icon = Icons.Rounded.Palette,
                        title = "Dynamic Logo Letter",
                        subtitle = "Current monogram initial: \"$logoLetter\"",
                        trailing = {
                            DynamicLogo(letter = logoLetter, size = 32.dp)
                        },
                        onClick = {
                            inputLetter = logoLetter
                            showLetterDialog = true
                        }
                    )
                    SettingDivider()
                    SettingItemRow(
                        icon = Icons.Rounded.Info,
                        title = "First-Time App Tour",
                        subtitle = "Review interface guide & gesture tutorial",
                        onClick = onShowGuide
                    )
                }
            }
        }

        // Section 5: PRIVACY & PERMISSIONS
        item {
            SectionHeader(title = "PRIVACY & LOCAL STORAGE")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = BrandGreenPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("100% Offline & Private", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextHighEmphasis)
                    }
                    Text(
                        text = "My Music never uploads your audio, tracks, or speech. All media indexing and speech recognition occur strictly on-device with zero analytics, no logins, and zero advertising.",
                        fontSize = 12.sp,
                        color = TextMediumEmphasis,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // About Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MY MUSIC",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Version 1.0.0 (Build 1) • Android 13+",
                    fontSize = 12.sp,
                    color = TextLowEmphasis
                )
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dynamic Logo Letter Dialog
    if (showLetterDialog) {
        AlertDialog(
            onDismissRequest = { showLetterDialog = false },
            containerColor = SurfaceElevated,
            title = {
                Text("Customize Brand Letter", color = TextHighEmphasis, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose your desired initial for the dynamic My Music logo. It will adapt across your app.",
                        fontSize = 13.sp,
                        color = TextMediumEmphasis
                    )
                    OutlinedTextField(
                        value = inputLetter,
                        onValueChange = { inputLetter = it.take(1).uppercase() },
                        label = { Text("Initial Letter", color = BrandGreenPrimary) },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputLetter.isNotBlank()) {
                            viewModel.setLogoLetter(inputLetter)
                            showLetterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                ) {
                    Text("Apply", color = DeepNearBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLetterDialog = false }) {
                    Text("Cancel", color = TextMediumEmphasis)
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = BrandGreenPrimary,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
fun SettingItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandGreenPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextHighEmphasis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMediumEmphasis,
                    maxLines = 1
                )
            }
        }

        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextLowEmphasis,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0x0DFFFFFF))
    )
}
