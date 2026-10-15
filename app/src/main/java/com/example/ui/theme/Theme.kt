package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TacticalDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = TacticalDeepDark,
    primaryContainer = TacticalCardElevated,
    onPrimaryContainer = NeonCyan,
    secondary = NeonEmerald,
    onSecondary = TacticalDeepDark,
    secondaryContainer = TacticalCardBg,
    onSecondaryContainer = NeonEmerald,
    tertiary = AmberWarning,
    onTertiary = TacticalDeepDark,
    error = RedDanger,
    onError = SlateTextPrimary,
    background = TacticalDarkBg,
    onBackground = SlateTextPrimary,
    surface = TacticalCardBg,
    onSurface = SlateTextPrimary,
    surfaceVariant = TacticalCardBorder,
    onSurfaceVariant = SlateTextSecondary,
    outline = TacticalCardBorderLight
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = Typography,
        content = content
    )
}
