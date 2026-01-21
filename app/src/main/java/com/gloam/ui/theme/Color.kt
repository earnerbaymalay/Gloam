package com.gloam.ui.theme

import androidx.compose.ui.graphics.Color

// Mood Colors (5-point scale)
object MoodColors {
    val Struggling = Color(0xFFE74C3C)  // 1 - Red
    val Low = Color(0xFFF39C12)         // 2 - Orange  
    val Neutral = Color(0xFF95A5A6)     // 3 - Gray
    val Good = Color(0xFF58D68D)        // 4 - Light Green
    val Great = Color(0xFF27AE60)       // 5 - Green
    
    fun forScore(score: Int): Color = when(score) {
        1 -> Struggling
        2 -> Low
        3 -> Neutral
        4 -> Good
        5 -> Great
        else -> Neutral
    }
    
    val emoji = mapOf(
        1 to "😢",
        2 to "😕", 
        3 to "😐",
        4 to "😊",
        5 to "😁"
    )
    
    val labels = mapOf(
        1 to "Struggling",
        2 to "Low",
        3 to "Neutral", 
        4 to "Good",
        5 to "Great"
    )
}

// Light theme palette (daytime)
object LightColors {
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F5F5)
    val Primary = Color(0xFF5B7C99)       // Muted blue-gray
    val PrimaryContainer = Color(0xFFD4E4F1)
    val Secondary = Color(0xFFE8A87C)     // Warm sunrise orange
    val SecondaryContainer = Color(0xFFFFF0E6)
    val OnBackground = Color(0xFF1A1A1A)
    val OnSurface = Color(0xFF2D2D2D)
    val OnSurfaceVariant = Color(0xFF666666)
    val Outline = Color(0xFFE0E0E0)
}

// Dark theme palette (nighttime)
object DarkColors {
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val Primary = Color(0xFF7EB8DA)       // Soft blue
    val PrimaryContainer = Color(0xFF2D4A5E)
    val Secondary = Color(0xFFD4A574)     // Warm sunset
    val SecondaryContainer = Color(0xFF3D2E1E)
    val OnBackground = Color(0xFFE6EDF3)
    val OnSurface = Color(0xFFC9D1D9)
    val OnSurfaceVariant = Color(0xFF8B949E)
    val Outline = Color(0xFF30363D)
}

// Transitional colors for gradual day/night shift
object TransitionColors {
    // Dawn colors (sunrise transition)
    val DawnSky = Color(0xFFFFF5E6)
    val DawnAccent = Color(0xFFFFB366)
    
    // Dusk colors (sunset transition) 
    val DuskSky = Color(0xFF2D1B4E)
    val DuskAccent = Color(0xFFE07B53)
    
    // Golden hour
    val GoldenHour = Color(0xFFFFD700)
}
