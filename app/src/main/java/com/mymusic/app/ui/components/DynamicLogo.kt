package com.mymusic.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymusic.app.ui.theme.BrandGreenAccent
import com.mymusic.app.ui.theme.BrandGreenDarkContainer
import com.mymusic.app.ui.theme.BrandGreenGlow
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.painterResource
import com.mymusic.app.R

/**
 * Dynamic App Logo showing either the official embossed 3D logo or a user's custom monogram
 * with integrated sound wave visual elements and animated glow.
 */
@Composable
fun DynamicLogo(
    letter: String = "M",
    size: Dp = 56.dp,
    isPlaying: Boolean = false,
    useOfficialLogo: Boolean = letter.trim().uppercase().let { it == "M" || it.isEmpty() },
    modifier: Modifier = Modifier
) {
    val displayLetter = letter.trim().take(1).uppercase().ifEmpty { "M" }
    val isLarge = size >= 64.dp

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isPlaying) 0.85f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isPlaying) 10.dp else 4.dp,
                shape = CircleShape,
                ambientColor = BrandGreenPrimary.copy(alpha = glowAlpha),
                spotColor = BrandGreenPrimary.copy(alpha = glowAlpha)
            )
            .clip(CircleShape)
            .background(DeepNearBlack)
            .border(
                width = if (isLarge) 2.dp else 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        BrandGreenPrimary.copy(alpha = if (isPlaying) 0.9f else 0.6f),
                        BrandGreenDarkContainer.copy(alpha = 0.4f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (useOfficialLogo) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_circle),
                contentDescription = "MY MUSIC Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (size < 40.dp) 0.dp else 2.dp)
            )
        } else {
            // Sound wave curves in the background
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = this.size.width
                val h = this.size.height
                val barW = w * 0.06f
                val spacing = w * 0.04f
                val bars = listOf(0.35f, 0.65f, 0.90f, 0.50f, 0.75f)

                val startX = (w - (bars.size * (barW + spacing) - spacing)) / 2f
                bars.forEachIndexed { i, factor ->
                    val barH = h * 0.25f * factor
                    drawRoundRect(
                        color = BrandGreenPrimary.copy(alpha = 0.18f),
                        topLeft = Offset(startX + i * (barW + spacing), h * 0.75f - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                    )
                }
            }

            // Central Letter
            Text(
                text = displayLetter,
                fontSize = (size.value * 0.52f).sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = BrandGreenPrimary,
                letterSpacing = (-1).sp
            )
        }
    }
}
