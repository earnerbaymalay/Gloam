package com.gloam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

data class GloamColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val isDark: Boolean
)

/**
 * Interpolates between light and dark themes based on daylight progress
 * @param daylightProgress 0.0 = full dark (midnight), 1.0 = full light (noon)
 */
@Composable
fun gloamColors(daylightProgress: Float): GloamColors {
    val progress = daylightProgress.coerceIn(0f, 1f)
    
    return GloamColors(
        background = lerp(DarkColors.Background, LightColors.Background, progress),
        surface = lerp(DarkColors.Surface, LightColors.Surface, progress),
        surfaceVariant = lerp(DarkColors.SurfaceVariant, LightColors.SurfaceVariant, progress),
        primary = lerp(DarkColors.Primary, LightColors.Primary, progress),
        primaryContainer = lerp(DarkColors.PrimaryContainer, LightColors.PrimaryContainer, progress),
        secondary = lerp(DarkColors.Secondary, LightColors.Secondary, progress),
        secondaryContainer = lerp(DarkColors.SecondaryContainer, LightColors.SecondaryContainer, progress),
        onBackground = lerp(DarkColors.OnBackground, LightColors.OnBackground, progress),
        onSurface = lerp(DarkColors.OnSurface, LightColors.OnSurface, progress),
        onSurfaceVariant = lerp(DarkColors.OnSurfaceVariant, LightColors.OnSurfaceVariant, progress),
        outline = lerp(DarkColors.Outline, LightColors.Outline, progress),
        isDark = progress < 0.5f
    )
}

private fun GloamColors.toMaterial3ColorScheme(): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            background = background,
            surface = surface,
            surfaceVariant = surfaceVariant,
            primary = primary,
            primaryContainer = primaryContainer,
            secondary = secondary,
            secondaryContainer = secondaryContainer,
            onBackground = onBackground,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline
        )
    } else {
        lightColorScheme(
            background = background,
            surface = surface,
            surfaceVariant = surfaceVariant,
            primary = primary,
            primaryContainer = primaryContainer,
            secondary = secondary,
            secondaryContainer = secondaryContainer,
            onBackground = onBackground,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline
        )
    }
}

val LocalGloamColors = staticCompositionLocalOf { 
    GloamColors(
        background = LightColors.Background,
        surface = LightColors.Surface,
        surfaceVariant = LightColors.SurfaceVariant,
        primary = LightColors.Primary,
        primaryContainer = LightColors.PrimaryContainer,
        secondary = LightColors.Secondary,
        secondaryContainer = LightColors.SecondaryContainer,
        onBackground = LightColors.OnBackground,
        onSurface = LightColors.OnSurface,
        onSurfaceVariant = LightColors.OnSurfaceVariant,
        outline = LightColors.Outline,
        isDark = false
    )
}

@Composable
fun GloamTheme(
    daylightProgress: Float = 0.7f,
    content: @Composable () -> Unit
) {
    val colors = gloamColors(daylightProgress)
    val colorScheme = colors.toMaterial3ColorScheme()
    
    CompositionLocalProvider(LocalGloamColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GloamTypography,
            content = content
        )
    }
}

object GloamTheme {
    val colors: GloamColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGloamColors.current
}
