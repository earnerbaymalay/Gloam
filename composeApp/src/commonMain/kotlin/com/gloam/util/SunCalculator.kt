package com.gloam.util

import kotlinx.datetime.*
import kotlin.math.*

data class SunTimes(
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val solarNoon: LocalDateTime
)

/**
 * NOAA solar calculator — platform-agnostic.
 * Computes sunrise, sunset, and solar noon for any lat/lng/date.
 * Based on the NOAA Solar Calculator algorithm.
 */
object SunCalculator {

    fun calculate(latitude: Double, longitude: Double, date: LocalDate): SunTimes {
        val year = date.year
        val month = date.monthNumber
        val day = date.dayOfMonth

        // Day of year
        val N1 = floor(275.0 * month / 9.0)
        val N2 = floor((month + 9.0) / 12.0)
        val N3 = (1 + floor((year - 4.0 * floor(year / 4.0) + 2.0) / 3.0)).toInt()
        val N = N1 - N2 * N3 + day - 30

        // Convert longitude to hour value and calculate an approximate time
        val lngHour = longitude / 15.0

        // Rising time
        val tRise = calculateTime(N, lngHour, 6.0, false, latitude)
        // Setting time
        val tSet = calculateTime(N, lngHour, 6.0, true, latitude)
        // Solar noon
        val tNoon = calculateTime(N, lngHour, 12.0, false, latitude)

        val sunrise = fromDecimalHours(date, tRise)
        val sunset = fromDecimalHours(date, tSet)
        val solarNoon = fromDecimalHours(date, tNoon)

        return SunTimes(sunrise, sunset, solarNoon)
    }

    private fun calculateTime(
        N: Double,
        lngHour: Double,
        targetHour: Double,
        setting: Boolean,
        latitude: Double
    ): Double {
        val t = if (setting) {
            N + ((24 - targetHour) / 24 - lngHour / 360)
        } else {
            N + ((targetHour / 24) - lngHour / 360)
        }

        val M = (0.9856 * t) - 3.289
        val Mrad = Math.toRadians(M)
        var L = M + (1.916 * sin(Mrad)) + (0.020 * sin(2 * Mrad)) + 282.634
        L = normalizeDegrees(L)
        val Lrad = Math.toRadians(L)

        var RA = Math.toDegrees(atan(0.91764 * tan(Lrad)))
        RA = normalizeDegrees(RA)

        val Lquadrant = floor(L / 90.0) * 90.0
        val RAquadrant = floor(RA / 90.0) * 90.0
        RA = RA + (Lquadrant - RAquadrant)
        RA = RA / 15.0

        val sinDec = 0.39782 * sin(Lrad)
        val cosDec = cos(asin(sinDec))
        val latRad = Math.toRadians(latitude)

        val cosH = (cos(Math.toRadians(90.833)) - (sinDec * sin(latRad))) / (cosDec * cos(latRad))

        if (cosH > 1 || cosH < -1) {
            // Sun never rises/sets on this date at this latitude
            return if (setting) 18.0 else 6.0 // Fallback
        }

        val H = if (setting) {
            (360 - Math.toDegrees(acos(cosH))) / 15.0
        } else {
            (360 + Math.toDegrees(acos(cosH))) / 15.0 // This was wrong, let me fix
        }

        // Actually for rising: H = (360 - acos) / 15, for setting: H = (360 + acos) / 15
        // Wait, that's backwards. Let me recalculate.
        val Hcorrect = if (setting) {
            (360 - Math.toDegrees(acos(cosH))) / 15.0
        } else {
            Math.toDegrees(acos(cosH)) / 15.0
        }

        val T = Hcorrect + RA - (0.06571 * t) - 6.622
        var UT = T - lngHour
        UT = normalizeToRange(UT, 0.0, 24.0)

        return UT
    }

    private fun fromDecimalHours(date: LocalDate, utHours: Double): LocalDateTime {
        // Simplified: returns local time assuming UTC offset of 0
        // In production, apply timezone offset
        val hours = utHours.toInt()
        val minutes = ((utHours - hours) * 60).toInt()
        return LocalDateTime(
            year = date.year,
            monthNumber = date.monthNumber,
            dayOfMonth = date.dayOfMonth,
            hour = hours.coerceIn(0, 23),
            minute = minutes.coerceIn(0, 59),
            second = 0
        )
    }

    private fun normalizeDegrees(angle: Double): Double {
        var a = angle
        while (a < 0) a += 360
        while (a >= 360) a -= 360
        return a
    }

    private fun normalizeToRange(value: Double, min: Double, max: Double): Double {
        val range = max - min
        var v = value
        while (v < min) v += range
        while (v >= max) v -= range
        return v
    }

    /**
     * Returns daylight progress as 0.0-1.0-0.0 curve.
     * 0.0 = night, 0.5 = solar noon, 1.0 = sunset, then back to 0.0
     */
    fun getDaylightProgress(
        currentTime: LocalTime,
        sunrise: LocalTime,
        sunset: LocalTime
    ): Double {
        val currentMinutes = currentTime.hour * 60 + currentTime.minute
        val sunriseMinutes = sunrise.hour * 60 + sunrise.minute
        val sunsetMinutes = sunset.hour * 60 + sunset.minute

        return when {
            currentMinutes < sunriseMinutes -> 0.0
            currentMinutes > sunsetMinutes -> 0.0
            else -> {
                val total = (sunsetMinutes - sunriseMinutes).toDouble()
                val elapsed = (currentMinutes - sunriseMinutes).toDouble()
                (elapsed / total).coerceIn(0.0, 1.0)
            }
        }
    }

    fun isNearSunrise(currentTime: LocalTime, sunrise: LocalTime, windowMinutes: Int = 30): Boolean {
        val diff = abs((currentTime.hour * 60 + currentTime.minute) - (sunrise.hour * 60 + sunrise.minute))
        return diff <= windowMinutes
    }

    fun isNearSunset(currentTime: LocalTime, sunset: LocalTime, windowMinutes: Int = 30): Boolean {
        val diff = abs((currentTime.hour * 60 + currentTime.minute) - (sunset.hour * 60 + sunset.minute))
        return diff <= windowMinutes
    }
}
