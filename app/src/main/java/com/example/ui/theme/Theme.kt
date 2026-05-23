package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DugamaPrimaryDark,
    secondary = DugamaSecondaryDark,
    tertiary = DugamaTertiaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    primaryContainer = Color(0xFF00531C),
    onPrimaryContainer = Color(0xFF90F9A5),
    surfaceVariant = Color(0xFF2C322D),
    onSurfaceVariant = Color(0xFFC2C9C3)
)

private val LightColorScheme = lightColorScheme(
    primary = DugamaPrimary,
    secondary = DugamaSecondary,
    tertiary = DugamaTertiary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnPrimary,
    onSecondary = Color.White,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    primaryContainer = Color(0xFFDCFCE7),      // Mint Green container (e.g., green-100)
    onPrimaryContainer = Color(0xFF166534),    // Dark Green text (e.g., green-800)
    surfaceVariant = Color(0xFFF1F5F9),        // Slate gray variant for borders and faint backdrops
    onSurfaceVariant = Color(0xFF64748B)       // Slate gray text
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep Dugama colors default to preserve brand identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
