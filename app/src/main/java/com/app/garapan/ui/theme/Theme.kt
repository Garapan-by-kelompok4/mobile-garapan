package com.app.garapan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GarapanColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = OnPrimary,
    secondary = AccentBlue,
    onSecondary = OnPrimary,
    background = White,
    onBackground = PrimaryText,
    surface = Surface,
    onSurface = PrimaryText,
    surfaceVariant = LightGray,
    onSurfaceVariant = SecondaryText,
    outline = BorderColor,
    error = ErrorRed,
    onError = OnPrimary,
)

@Composable
fun GarapanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GarapanColorScheme,
        typography = Typography,
        content = content
    )
}
