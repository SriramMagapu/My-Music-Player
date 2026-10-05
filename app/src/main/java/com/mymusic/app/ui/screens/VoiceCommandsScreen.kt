package com.mymusic.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mymusic.app.model.CustomVoiceCommand
import com.mymusic.app.model.VoiceAction
import com.mymusic.app.model.VoiceState
import com.mymusic.app.ui.components.DynamicLogo
import com.mymusic.app.ui.theme.BrandGreenAccent
import com.mymusic.app.ui.theme.BrandGreenDarkContainer
import com.mymusic.app.ui.theme.BrandGreenGlow
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.ErrorRed
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextLowEmphasis
import com.mymusic.app.ui.theme.TextMediumEmphasis
import com.mymusic.app.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandsScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceState by viewModel.voiceState.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val voiceCommands by viewModel.voiceCommands.collectAsState()
    val logoLetter by viewModel.logoLetter.collectAsState()

    var showTeachDialog by remember { mutableStateOf(false) }
    var selectedActionForTraining by remember { mutableStateOf(VoiceAction.PLAY) }
    var trainingPhrase by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startVoiceListening()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "listeningPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNearBlack)
            .statusBarsPadding()
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextHighEmphasis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Voice Control",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis
                    )
                    Text(
                        text = "Speak to control your music",
                        fontSize = 12.sp,
                        color = BrandGreenPrimary
                    )
                }
            }

            Button(
                onClick = { showTeachDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenDarkContainer),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Rounded.School, contentDescription = null, tint = BrandGreenPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Teach App", color = BrandGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Animated Voice State Center
                val isListening = voiceState is VoiceState.Listening
                val isProcessing = voiceState is VoiceState.Processing

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(if (isListening) pulseScale else 1.0f)
                        .shadow(
                            elevation = if (isListening) 24.dp else 8.dp,
                            shape = CircleShape,
                            ambientColor = if (isListening) BrandGreenPrimary else Color.Black,
                            spotColor = if (isListening) BrandGreenPrimary else Color.Black
                        )
                        .clip(CircleShape)
                        .background(
                            if (isListening) BrandGreenPrimary else SurfaceElevated
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                if (isListening) listOf(BrandGreenPrimary, BrandGreenAccent)
                                else listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
                            ),
                            shape = CircleShape
                        )
                        .clickable {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                if (isListening) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    viewModel.startVoiceListening()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = "Voice mic",
                        tint = if (isListening) DeepNearBlack else BrandGreenPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Voice Status Message with strict visual states
                when (val state = voiceState) {
                    is VoiceState.Idle -> {
                        Text(
                            text = "Tap the microphone and say your command.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextHighEmphasis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Try: \"Play\", \"Next song\", \"Volume up\", \"Play favorites\"",
                            fontSize = 12.sp,
                            color = TextLowEmphasis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    is VoiceState.Listening -> {
                        Text(
                            text = state.message,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGreenPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Speak naturally...",
                            fontSize = 13.sp,
                            color = TextMediumEmphasis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    is VoiceState.Processing -> {
                        Text(
                            text = "Understanding your command...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGreenAccent,
                            textAlign = TextAlign.Center
                        )
                    }
                    is VoiceState.Saved -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BrandGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Command saved: \"${state.commandText}\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreenPrimary
                            )
                        }
                    }
                    is VoiceState.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BrandGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.executedActionText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreenPrimary
                            )
                        }
                    }
                    is VoiceState.Error -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.errorMessage,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ErrorRed,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is VoiceState.Unavailable -> {
                        Text(
                            text = state.reason,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextLowEmphasis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Saved / Available Voice Commands Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Learned Commands (${voiceCommands.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            if (voiceCommands.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Teach My Music how you want to control your music.",
                                fontSize = 14.sp,
                                color = TextMediumEmphasis,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showTeachDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("CREATE COMMAND", color = DeepNearBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(voiceCommands) { command ->
                    VoiceCommandRow(
                        command = command,
                        onDelete = { viewModel.deleteVoiceCommand(command.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Teach / Add Custom Voice Command Dialog
    if (showTeachDialog) {
        AlertDialog(
            onDismissRequest = { showTeachDialog = false },
            containerColor = SurfaceElevated,
            title = {
                Text("Teach Voice Command", color = TextHighEmphasis, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Choose the action you want to trigger, then enter your personalized phrase.",
                        fontSize = 13.sp,
                        color = TextMediumEmphasis
                    )

                    // Action Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedActionForTraining.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Trigger Action", color = BrandGreenPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedBorderColor = BrandGreenPrimary,
                                unfocusedBorderColor = Color(0x1AFFFFFF),
                                focusedTextColor = TextHighEmphasis,
                                unfocusedTextColor = TextHighEmphasis
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceElevated)
                        ) {
                            VoiceAction.values().forEach { action ->
                                DropdownMenuItem(
                                    text = { Text(action.displayName, color = TextHighEmphasis) },
                                    onClick = {
                                        selectedActionForTraining = action
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Custom Phrase input
                    OutlinedTextField(
                        value = trainingPhrase,
                        onValueChange = { trainingPhrase = it },
                        label = { Text("Your Custom Phrase", color = BrandGreenPrimary) },
                        placeholder = { Text("e.g. \"play my bangers\"", color = TextLowEmphasis) },
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
                        if (trainingPhrase.isNotBlank()) {
                            viewModel.saveVoiceCommand(trainingPhrase, selectedActionForTraining)
                            trainingPhrase = ""
                            showTeachDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                ) {
                    Text("Save Command", color = DeepNearBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTeachDialog = false }) {
                    Text("Cancel", color = TextMediumEmphasis)
                }
            }
        )
    }
}

@Composable
fun VoiceCommandRow(
    command: CustomVoiceCommand,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "\"${command.triggerPhrase}\"",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandGreenPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Action: ${command.action.displayName}",
                fontSize = 12.sp,
                color = TextMediumEmphasis
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Delete",
                tint = TextLowEmphasis,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
