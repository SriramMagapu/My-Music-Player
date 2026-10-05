package com.mymusic.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandGreenPrimary,
    onPrimary = DeepNearBlack,
    primaryContainer = BrandGreenDarkContainer,
    onPrimaryContainer = BrandGreenAccent,
    secondary = BrandGreenAccent,
    onSecondary = DeepNearBlack,
    background = DeepNearBlack,
    onBackground = TextHighEmphasis,
    surface = SurfaceDark,
    onSurface = TextHighEmphasis,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextMediumEmphasis,
    outline = SurfaceHighlight
)

@Composable
fun MyMusicTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepNearBlack.toArgb()
            window.navigationBarColor = DeepNearBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
