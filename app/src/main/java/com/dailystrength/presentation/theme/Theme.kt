package com.dailystrength.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Flame,
    onPrimary = OnBackground,
    secondary = Ember,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onSurface = OnBackground,
    onSurfaceVariant = OnSurfaceMuted,
    outline = Outline,
    error = AtRisk,
)

private val LightColors = lightColorScheme(
    primary = FlameDeep,
    onPrimary = LightSurface,
    secondary = Ember,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    error = AtRisk,
)

@Composable
fun DailyStrengthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
