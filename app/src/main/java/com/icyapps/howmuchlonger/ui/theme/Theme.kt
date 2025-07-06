package com.icyapps.howmuchlonger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondaryText,
    tertiary = DarkAccent,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = DarkPrimary,
    onSurface = DarkPrimary,
    surfaceVariant = DarkEventCardBackground
)

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
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
