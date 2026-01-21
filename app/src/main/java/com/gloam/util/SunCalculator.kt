package com.gloam.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.*

/**
 * Calculates sunrise and sunset times based on latitude/longitude
 * Uses the NOAA Solar Calculator algorithm
 */
object SunCalculator {
    
    data class SunTimes(
        val sunrise: LocalTime,
        val sunset: LocalTime,
        val solarNoon: LocalTime
    )
    
    fun calculate(latitude: Double, longitude: Double, date: LocalDate = LocalDate.now()): SunTimes {
        val dayOfYear = date.dayOfYear
        val timezone = ZoneId.systemDefault().rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0
        
        // Fractional year (radians)
        val gamma = 2 * PI / 365 * (dayOfYear - 1 + 0.5)
        
        // Equation of time (minutes)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) - 
                              0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
        
        // Solar declination (radians)
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 
                   0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) - 
                   0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)
        
        val latRad = Math.toRadians(latitude)
        
        // Hour angle for sunrise/sunset
        val zenith = Math.toRadians(90.833) // Official sunrise/sunset
        val cosHa = (cos(zenith) / (cos(latRad) * cos(decl)) - tan(latRad) * tan(decl))
            .coerceIn(-1.0, 1.0)
        val ha = Math.toDegrees(acos(cosHa))
        
        // Solar noon (minutes from midnight)
        val solarNoonMin = (720 - 4 * longitude - eqTime + timezone * 60)
        
        // Sunrise and sunset (minutes from midnight)
        val sunriseMin = solarNoonMin - ha * 4
        val sunsetMin = solarNoonMin + ha * 4
        
        return SunTimes(
            sunrise = minutesToLocalTime(sunriseMin),
            sunset = minutesToLocalTime(sunsetMin),
            solarNoon = minutesToLocalTime(solarNoonMin)
        )
    }
    
    private fun minutesToLocalTime(minutes: Double): LocalTime {
        val totalMinutes = minutes.roundToInt().coerceIn(0, 1439)
        return LocalTime.of(totalMinutes / 60, totalMinutes % 60)
    }
    
    /**
     * Returns a value between 0.0 (midnight) and 1.0 (noon) and back to 0.0 (midnight)
     * for smooth day/night theme transitions
     */
    fun getDaylightProgress(latitude: Double, longitude: Double): Float {
        val now = LocalTime.now()
        val sunTimes = calculate(latitude, longitude)
        
        val currentMinutes = now.hour * 60 + now.minute
        val sunriseMinutes = sunTimes.sunrise.hour * 60 + sunTimes.sunrise.minute
        val sunsetMinutes = sunTimes.sunset.hour * 60 + sunTimes.sunset.minute
        val noonMinutes = sunTimes.solarNoon.hour * 60 + sunTimes.solarNoon.minute
        
        return when {
            currentMinutes < sunriseMinutes -> {
                // Before sunrise: transition from 0 to small value
                (currentMinutes.toFloat() / sunriseMinutes) * 0.1f
            }
            currentMinutes < noonMinutes -> {
                // Sunrise to noon: 0.1 to 1.0
                val progress = (currentMinutes - sunriseMinutes).toFloat() / (noonMinutes - sunriseMinutes)
                0.1f + progress * 0.9f
            }
            currentMinutes < sunsetMinutes -> {
                // Noon to sunset: 1.0 to 0.1
                val progress = (currentMinutes - noonMinutes).toFloat() / (sunsetMinutes - noonMinutes)
                1.0f - progress * 0.9f
            }
            else -> {
                // After sunset: transition to 0
                val remaining = 1440 - currentMinutes
                val afterSunset = 1440 - sunsetMinutes
                (remaining.toFloat() / afterSunset) * 0.1f
            }
        }
    }
    
    fun isNearSunrise(latitude: Double, longitude: Double, windowMinutes: Int = 30): Boolean {
        val now = LocalTime.now()
        val sunTimes = calculate(latitude, longitude)
        val diffMinutes = abs(
            (now.hour * 60 + now.minute) - (sunTimes.sunrise.hour * 60 + sunTimes.sunrise.minute)
        )
        return diffMinutes <= windowMinutes
    }
    
    fun isNearSunset(latitude: Double, longitude: Double, windowMinutes: Int = 30): Boolean {
        val now = LocalTime.now()
        val sunTimes = calculate(latitude, longitude)
        val diffMinutes = abs(
            (now.hour * 60 + now.minute) - (sunTimes.sunset.hour * 60 + sunTimes.sunset.minute)
        )
        return diffMinutes <= windowMinutes
    }
}
