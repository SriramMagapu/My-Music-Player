package com.mymusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymusic.app.ui.theme.BrandGreenDarkContainer
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextMediumEmphasis
import com.mymusic.app.ui.viewmodel.MusicViewModel

@Composable
fun EqualizerDialog(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val soundSettings by viewModel.soundSettings.collectAsState()
    val presets = viewModel.getEqualizerPresetNames()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Equalizer, contentDescription = null, tint = BrandGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sound & Equalizer", color = TextHighEmphasis, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Preset Pills
                Text("EQUALIZER PRESETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGreenPrimary, letterSpacing = 1.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    itemsIndexed(presets) { index, presetName ->
                        val isSelected = soundSettings.selectedPresetIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandGreenPrimary else SurfaceDark)
                                .border(1.dp, if (isSelected) BrandGreenPrimary else Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                                .clickable { viewModel.setEqualizerPreset(index) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = presetName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) DeepNearBlack else TextHighEmphasis
                            )
                        }
                    }
                }

                // Bass Boost
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bass Boost", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextHighEmphasis)
                        Switch(
                            checked = soundSettings.isBassBoostEnabled,
                            onCheckedChange = { viewModel.setBassBoost(it, soundSettings.bassBoostStrength) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandGreenPrimary,
                                checkedTrackColor = BrandGreenDarkContainer
                            )
                        )
                    }
                    if (soundSettings.isBassBoostEnabled) {
                        Slider(
                            value = soundSettings.bassBoostStrength.toFloat(),
                            onValueChange = { viewModel.setBassBoost(true, it.toInt()) },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandGreenPrimary,
                                activeTrackColor = BrandGreenPrimary
                            )
                        )
                    }
                }

                // Safe Volume Boost
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Safe Volume Boost", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextHighEmphasis)
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
                        Slider(
                            value = soundSettings.volumeBoostPercent.toFloat(),
                            onValueChange = { viewModel.setVolumeBoost(true, it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandGreenPrimary,
                                activeTrackColor = BrandGreenPrimary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
            ) {
                Text("Done", color = DeepNearBlack, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SleepTimerDialog(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val remainingMs by viewModel.sleepTimerRemainingMs.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        title = {
            Text("Sleep Timer", color = TextHighEmphasis, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (remainingMs > 0) {
                    val minutesLeft = remainingMs / 60000
                    val secondsLeft = (remainingMs % 60000) / 1000
                    Text(
                        text = "Active: %d min %02d sec remaining".format(minutesLeft, secondsLeft),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenPrimary
                    )
                } else {
                    Text(
                        text = "Music will automatically fade out and stop when the timer finishes.",
                        fontSize = 13.sp,
                        color = TextMediumEmphasis
                    )
                }

                val options = listOf(15, 30, 45, 60, 90)
                options.forEach { mins ->
                    Button(
                        onClick = {
                            viewModel.setSleepTimer(mins)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("$mins Minutes", color = TextHighEmphasis, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (remainingMs > 0) {
                    Button(
                        onClick = {
                            viewModel.cancelSleepTimer()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF5252)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Turn Off Timer", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMediumEmphasis)
            }
        }
    )
}
