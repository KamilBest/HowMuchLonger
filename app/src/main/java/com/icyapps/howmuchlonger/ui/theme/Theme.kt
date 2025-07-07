package com.icyapps.howmuchlonger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Background,
    primaryContainer = Primary, // or use a lighter/darker variant if needed
    onPrimaryContainer = Background,
    inversePrimary = SecondaryText, // or another appropriate contrast

    secondary = SecondaryText,
    onSecondary = Background,
    secondaryContainer = SecondaryText,
    onSecondaryContainer = Background,

    tertiary = Accent,
    onTertiary = Background,
    tertiaryContainer = Accent,
    onTertiaryContainer = Background,

    background = Background,
    onBackground = Primary,
    surface = Background,
    onSurface = Primary,

    surfaceVariant = EventCardBackground,
    onSurfaceVariant = Primary,

    surfaceTint = Accent,

    inverseSurface = SecondaryText,
    inverseOnSurface = Background,

    error = Red,
    onError = Background,
    errorContainer = Red,
    onErrorContainer = Red,

    outline = SecondaryText,
    outlineVariant = SecondaryText,
    scrim = Color.Black,

    surfaceBright = Background,
    surfaceDim = Background,
    surfaceContainer = Background,
    surfaceContainerHigh = Background,
    surfaceContainerHighest = Background,
    surfaceContainerLow = Background,
    surfaceContainerLowest = Background,
)

@Composable
fun HowMuchLongerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.background.toArgb()
            }

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
