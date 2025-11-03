package com.calisthenia.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.calisthenia.core.ui.LocalSpacing
import com.calisthenia.core.ui.calisteniaSpacing

private val LightColors = lightColorScheme(
    primary = ColorPalette.Primary,
    onPrimary = ColorPalette.OnPrimary,
    secondary = ColorPalette.Secondary,
    onSecondary = ColorPalette.OnSecondary,
    background = ColorPalette.Background,
    onBackground = ColorPalette.OnBackground,
    surface = ColorPalette.Surface,
    onSurface = ColorPalette.OnSurface,
)

private val DarkColors = darkColorScheme(
    primary = ColorPalette.PrimaryDark,
    onPrimary = ColorPalette.OnPrimaryDark,
    secondary = ColorPalette.SecondaryDark,
    onSecondary = ColorPalette.OnSecondaryDark,
    background = ColorPalette.BackgroundDark,
    onBackground = ColorPalette.OnBackgroundDark,
    surface = ColorPalette.SurfaceDark,
    onSurface = ColorPalette.OnSurfaceDark,
)

@Composable
fun CalisteniaTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalSpacing provides calisteniaSpacing()) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
