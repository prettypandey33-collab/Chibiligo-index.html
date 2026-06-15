package com.example.ui.theme

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
    primary = ChibiPink,
    secondary = ChibiLavender,
    tertiary = ChibiBlue,
    background = ChibiBgDark,
    surface = ChibiCardDark,
    onPrimary = Color(0xFF1A1A2E),
    onSecondary = Color(0xFF1A1A2E),
    onTertiary = Color(0xFF1A1A2E),
    onBackground = ChibiTextDarkTheme,
    onSurface = ChibiTextDarkTheme
)

private val LightColorScheme = lightColorScheme(
    primary = ChibiPinkDark,
    secondary = ChibiLavender,
    tertiary = ChibiBlue,
    background = ChibiBgLight,
    surface = ChibiWhite,
    onPrimary = Color.White,
    onSecondary = ChibiTextDark,
    onTertiary = ChibiTextDark,
    onBackground = ChibiTextDark,
    onSurface = ChibiTextDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamic color disabled so our custom branded pastel anime aesthetic is ALWAYS shown in its full glory!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
