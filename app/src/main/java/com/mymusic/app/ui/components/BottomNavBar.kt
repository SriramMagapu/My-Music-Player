package com.mymusic.app.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mymusic.app.model.Track
import com.mymusic.app.ui.theme.BrandGreenGlow
import com.mymusic.app.ui.theme.BrandGreenPrimary
import com.mymusic.app.ui.theme.DeepNearBlack
import com.mymusic.app.ui.theme.SurfaceDark
import com.mymusic.app.ui.theme.SurfaceElevated
import com.mymusic.app.ui.theme.TextHighEmphasis
import com.mymusic.app.ui.theme.TextLowEmphasis

enum class NavDestination {
    HOME,
    PROFILE
}

/**
 * Three destinations brand bottom navigation:
 * HOME | CENTER NOW PLAYING ORB | PROFILE
 */
@Composable
fun BottomNavBar(
    selectedDestination: NavDestination,
    currentTrack: Track?,
    isPlaying: Boolean,
    logoLetter: String,
    onNavigate: (NavDestination) -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Base Navigation Surface
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(SurfaceDark)
                .border(
                    width = 1.dp,
                    color = Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Destination 1: HOME
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigate(NavDestination.HOME) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = if (selectedDestination == NavDestination.HOME) BrandGreenPrimary else TextLowEmphasis,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Home",
                    fontSize = 11.sp,
                    fontWeight = if (selectedDestination == NavDestination.HOME) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedDestination == NavDestination.HOME) BrandGreenPrimary else TextLowEmphasis
                )
            }

            // Space reserved for center orb
            Spacer(modifier = Modifier.size(60.dp))

            // Destination 3: PROFILE
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigate(NavDestination.PROFILE) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = if (selectedDestination == NavDestination.PROFILE) BrandGreenPrimary else TextLowEmphasis,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Profile",
                    fontSize = 11.sp,
                    fontWeight = if (selectedDestination == NavDestination.PROFILE) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedDestination == NavDestination.PROFILE) BrandGreenPrimary else TextLowEmphasis
                )
            }
        }

        // Center Now Playing Orb (Elevated above bar for 1-hand thumb access)
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(62.dp)
                .scale(pulseScale)
                .shadow(
                    elevation = if (isPlaying) 12.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = if (isPlaying) BrandGreenPrimary else Color.Black,
                    spotColor = if (isPlaying) BrandGreenPrimary else Color.Black
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SurfaceElevated,
                            DeepNearBlack
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        if (isPlaying) listOf(BrandGreenPrimary, Color(0xFF69F0AE))
                        else listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
                    ),
                    shape = CircleShape
                )
                .clickable(onClick = onOpenNowPlaying),
            contentAlignment = Alignment.Center
        ) {
            if (currentTrack?.albumArtUriString != null) {
                AsyncImage(
                    model = currentTrack.albumArtUriString,
                    contentDescription = "Current Artwork",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                DynamicLogo(
                    letter = logoLetter,
                    size = 62.dp,
                    isPlaying = isPlaying
                )
            }

            // Green active playing indicator dot
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(BrandGreenPrimary)
                        .border(1.5.dp, DeepNearBlack, CircleShape)
                )
            }
        }
    }
}
