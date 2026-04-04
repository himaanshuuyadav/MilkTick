package com.prantiux.milktick.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Material 3 Expressive Dark Color Scheme
private fun getDarkColorScheme(accentColor: androidx.compose.ui.graphics.Color) = darkColorScheme(
    primary = accentColor,
    onPrimary = MilkOnPrimary,
    primaryContainer = accentColor.copy(alpha = 0.5f),
    onPrimaryContainer = accentColor,
    secondary = MilkSecondary,
    onSecondary = MilkOnSecondary,
    secondaryContainer = MilkSecondaryDark,
    onSecondaryContainer = MilkSecondaryLight,
    tertiary = accentColor.copy(alpha = 0.8f),
    onTertiary = MilkOnSecondary,
    tertiaryContainer = accentColor.copy(alpha = 0.3f),
    onTertiaryContainer = accentColor,
    error = MilkError,
    onError = MilkOnPrimary,
    errorContainer = MilkErrorContainer,
    onErrorContainer = MilkOnErrorContainer,
    background = MilkBackgroundDark,
    onBackground = MilkOnBackgroundDark,
    surface = MilkSurfaceDark,
    onSurface = MilkOnSurfaceDark,
    surfaceVariant = MilkSurfaceVariantDark,
    onSurfaceVariant = MilkOnSurfaceVariantDark,
    outline = MilkOutlineDark,
    outlineVariant = MilkOutlineVariantDark,
)

// Material 3 Expressive Light Color Scheme
private fun getLightColorScheme(accentColor: androidx.compose.ui.graphics.Color) = lightColorScheme(
    primary = accentColor,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = accentColor.copy(alpha = 0.12f),
    onPrimaryContainer = accentColor,
    secondary = Color(0xFFF8F9FA), // Very light gray for headers in light theme
    onSecondary = Color(0xFF000000), // Black text
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = accentColor.copy(alpha = 0.7f),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = accentColor.copy(alpha = 0.15f),
    onTertiaryContainer = Color(0xFF000000),
    error = MilkError,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Color(0xFFFFFFFF), // Pure white background
    onBackground = Color(0xFF1C1C1C), // Almost black text
    surface = Color(0xFFFAFAFA), // Very light gray for cards
    onSurface = Color(0xFF1C1C1C), // Almost black text
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFD0D0D0), // Light gray outline
    outlineVariant = Color(0xFFE8E8E8), // Even lighter gray
)

@Composable
fun MilkTickTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    accentColor: androidx.compose.ui.graphics.Color = Color(0xFF146EBE), // Azure default
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Determine if dark theme should be used
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.AUTO -> systemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    // Get the appropriate color scheme
    val colorScheme = if (darkTheme) {
        getDarkColorScheme(accentColor)
    } else {
        getLightColorScheme(accentColor)
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            if (darkTheme) {
                window.statusBarColor = colorScheme.secondary.toArgb() // Use #121212 for dark
                window.navigationBarColor = colorScheme.secondary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            } else {
                window.statusBarColor = colorScheme.surface.toArgb() // Light gray for light
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
