package com.mymusic.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mymusic.app.ui.theme.BrandGreenPrimary

/**
 * Animated audio equalizer bars indicating playing state.
 */
@Composable
fun EqualizerBarsAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = BrandGreenPrimary,
    barWidth: Dp = 3.dp,
    barHeight: Dp = 16.dp
) {
    val transition = rememberInfiniteTransition(label = "eq_bars")

    val h1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isPlaying) 0.95f else 0.2f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isPlaying) 0.3f else 0.2f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 1.0f else 0.2f,
        animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = if (isPlaying) 0.25f else 0.2f,
        animationSpec = infiniteRepeatable(tween(390, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h4"
    )

    val heights = listOf(h1, h2, h3, h4)

    Canvas(
        modifier = modifier
            .width((barWidth * 4 + 8.dp))
            .height(barHeight)
    ) {
        val w = size.width
        val h = size.height
        val bw = barWidth.toPx()
        val spacing = (w - (4 * bw)) / 3f

        heights.forEachIndexed { index, scale ->
            val actualH = (h * scale).coerceAtLeast(bw)
            drawRoundRect(
                color = color,
                topLeft = Offset(index * (bw + spacing), h - actualH),
                size = Size(bw, actualH),
                cornerRadius = CornerRadius(bw / 2f, bw / 2f)
            )
        }
    }
}
