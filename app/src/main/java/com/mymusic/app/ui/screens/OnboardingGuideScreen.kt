package com.mymusic.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymusic.app.ui.components.DynamicLogo
import com.mymusic.app.ui.theme.BrandGreenAccent
import com.mymusic.app.ui.theme.BrandGreenDarkContainer
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.OverlayDim
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextLowEmphasis
import com.mymusic.app.ui.theme.TextMediumEmphasis

data class GuideStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val highlightTarget: String
)

@Composable
fun OnboardingGuideScreen(
    logoLetter: String,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        GuideStep(
            title = "Welcome to MY MUSIC",
            description = "A private, 100% offline local music player designed for modern Android 13+ devices.",
            icon = Icons.Rounded.MusicNote,
            highlightTarget = "BRAND"
        ),
        GuideStep(
            title = "Local Music Folder",
            description = "Select your music directory using Android's safe Storage Access Framework. No files are uploaded or moved.",
            icon = Icons.Rounded.FolderOpen,
            highlightTarget = "FOLDER"
        ),
        GuideStep(
            title = "Now Playing Center Orb",
            description = "Tap the circular artwork orb at the bottom anytime to expand the immersive Now Playing player.",
            icon = Icons.Rounded.MusicNote,
            highlightTarget = "NOW_PLAYING_ORB"
        ),
        GuideStep(
            title = "Left-Side Volume Gesture",
            description = "Swipe upward or downward on the left half of Now Playing to adjust volume smoothly with a transparent glass HUD.",
            icon = Icons.Rounded.Gesture,
            highlightTarget = "VOLUME_GESTURE"
        ),
        GuideStep(
            title = "Voice Commands",
            description = "Tap the green microphone and speak naturally to control playback, or teach custom trigger phrases.",
            icon = Icons.Rounded.Mic,
            highlightTarget = "VOICE_MIC"
        ),
        GuideStep(
            title = "Sound & Equalizer Boost",
            description = "Tune frequency presets, increase bass, and safely boost quiet local recordings.",
            icon = Icons.Rounded.Equalizer,
            highlightTarget = "EQUALIZER"
        ),
        GuideStep(
            title = "You're ready.",
            description = "Enjoy pure, high-fidelity local music playback with zero interruptions.",
            icon = Icons.Rounded.CheckCircle,
            highlightTarget = "READY"
        )
    )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OverlayDim)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceElevated)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            BrandGreenPrimary.copy(alpha = 0.5f),
                            Color(0x1AFFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dynamic App Monogram or Feature Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(BrandGreenDarkContainer)
                    .border(2.dp, BrandGreenPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (currentStepIndex == 0) {
                    DynamicLogo(letter = logoLetter, size = 80.dp)
                } else {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = step.title,
                        tint = BrandGreenPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                },
                label = "guideTextAnimation"
            ) { targetStep ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = targetStep.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = targetStep.description,
                        fontSize = 14.sp,
                        color = TextMediumEmphasis,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Step Progress Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (idx == currentStepIndex) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == currentStepIndex) BrandGreenPrimary else Color(0x33FFFFFF)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons (SKIP / NEXT / GET STARTED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastStep) {
                    TextButton(onClick = onComplete) {
                        Text("SKIP", color = TextLowEmphasis, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (isLastStep) {
                            onComplete()
                        } else {
                            currentStepIndex++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (isLastStep) "LET'S PLAY" else "NEXT",
                        color = DeepNearBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
