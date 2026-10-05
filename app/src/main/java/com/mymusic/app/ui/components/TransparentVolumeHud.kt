package com.mymusic.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeMute
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.HudBackgroundTranslucent
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.TextHighEmphasis

/**
 * Transparent Volume HUD shown during vertical gesture swipe.
 */
@Composable
fun TransparentVolumeHud(
    visible: Boolean,
    volumePercent: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .shadow(16.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black)
                .clip(RoundedCornerShape(20.dp))
                .background(HudBackgroundTranslucent)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            BrandGreenPrimary.copy(alpha = 0.35f),
                            Color(0x1AFFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val icon = when {
                    volumePercent == 0 -> Icons.Rounded.VolumeMute
                    volumePercent < 50 -> Icons.Rounded.VolumeDown
                    else -> Icons.Rounded.VolumeUp
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Volume",
                    tint = BrandGreenPrimary,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "$volumePercent%",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mini horizontal progress line
                LinearProgressIndicator(
                    progress = { volumePercent / 100f },
                    modifier = Modifier
                        .width(80.dp)
                        .height(4.dp)
                        .clip(CircleShape),
                    color = BrandGreenPrimary,
                    trackColor = Color(0x33FFFFFF)
                )
            }
        }
    }
}
