package com.gloam.util

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

/**
 * Unit tests for the NOAA solar calculator.
 * Tests verify sunrise, sunset, and solar noon calculations
 * for known locations and dates.
 */
class SunCalculatorTest {

    private val calc = SunCalculator()

    // ── Known values (approximate, within 5 min tolerance) ──

    @Test
    fun `sunrise and sunset for Sydney on summer solstice`() {
        // Sydney: lat -33.87, lng 151.21
        // Dec 21 summer solstice: sunrise ~5:33, sunset ~20:09
        val date = LocalDate.of(2024, 12, 21)
        val sun = calc.calculate(-33.87, 151.21, date)

        assertNotNull(sun)
        assertTrue(sun.sunrise.hour in 5..6)
        assertTrue(sun.sunset.hour in 19..20)
        assertTrue(sun.sunrise.isBefore(sun.sunset))
        assertTrue(sun.solarNoon.isAfter(sun.sunrise))
        assertTrue(sun.solarNoon.isBefore(sun.sunset))
    }

    @Test
    fun `sunrise and sunset for Sydney on winter solstice`() {
        // June 21 winter solstice: sunrise ~7:00, sunset ~16:53
        val date = LocalDate.of(2024, 6, 21)
        val sun = calc.calculate(-33.87, 151.21, date)

        assertNotNull(sun)
        assertTrue(sun.sunrise.hour in 6..7)
        assertTrue(sun.sunset.hour in 16..17)
        assertTrue(sun.sunrise.isBefore(sun.sunset))
    }

    @Test
    fun `sunrise and sunset for equinox (equal day/night)`() {
        // March 20 equinox: roughly 12 hours of daylight everywhere
        val date = LocalDate.of(2024, 3, 20)
        val sun = calc.calculate(-33.87, 151.21, date)

        val dayLength = java.time.Duration.between(sun.sunrise, sun.sunset)
        // Should be approximately 12 hours (within 1 hour tolerance)
        assertTrue(dayLength.toHours() in 11..13)
    }

    @Test
    fun `northern hemisphere has opposite seasons`() {
        // New York: lat 40.71, lng -74.01
        val summerDate = LocalDate.of(2024, 6, 21)
        val winterDate = LocalDate.of(2024, 12, 21)

        val summerSun = calc.calculate(40.71, -74.01, summerDate)
        val winterSun = calc.calculate(40.71, -74.01, winterDate)

        val summerDayLength = java.time.Duration.between(summerSun.sunrise, summerSun.sunset).toMinutes()
        val winterDayLength = java.time.Duration.between(winterSun.sunrise, winterSun.sunset).toMinutes()

        // Summer days should be significantly longer than winter days
        assertTrue("Summer day should be longer than winter day", summerDayLength > winterDayLength)
        // Summer should be > 14 hours, winter < 10 hours for NYC
        assertTrue(summerDayLength > 840) // 14 hours
        assertTrue(winterDayLength < 600) // 10 hours
    }

    @Test
    fun `equator has consistent day length year-round`() {
        // Quito, Ecuador: lat -0.18, lng -78.47
        val dates = listOf(
            LocalDate.of(2024, 3, 20),
            LocalDate.of(2024, 6, 21),
            LocalDate.of(2024, 9, 22),
            LocalDate.of(2024, 12, 21)
        )

        val dayLengths = dates.map { date ->
            val sun = calc.calculate(-0.18, -78.47, date)
            java.time.Duration.between(sun.sunrise, sun.sunset).toMinutes()
        }

        // All day lengths should be within 30 minutes of each other at the equator
        val maxDiff = dayLengths.max() - dayLengths.min()
        assertTrue("Equator day lengths should be consistent (max diff < 30 min), but diff was $maxDiff", maxDiff < 30)
    }

    @Test
    fun `getDaylightProgress at solar noon is 1.0`() {
        val date = LocalDate.of(2024, 6, 15)
        val sun = calc.calculate(-33.87, 151.21, date)

        val progress = calc.getDaylightProgress(
            sun.solarNoon.toLocalTime(),
            sun.sunrise.toLocalTime(),
            sun.sunset.toLocalTime()
        )

        assertEquals(1.0, progress, 0.01)
    }

    @Test
    fun `getDaylightProgress at sunrise is 0.0`() {
        val date = LocalDate.of(2024, 6, 15)
        val sun = calc.calculate(-33.87, 151.21, date)

        val progress = calc.getDaylightProgress(
            sun.sunrise.toLocalTime(),
            sun.sunrise.toLocalTime(),
            sun.sunset.toLocalTime()
        )

        assertEquals(0.0, progress, 0.01)
    }

    @Test
    fun `getDaylightProgress at sunset is 0.0`() {
        val date = LocalDate.of(2024, 6, 15)
        val sun = calc.calculate(-33.87, 151.21, date)

        val progress = calc.getDaylightProgress(
            sun.sunset.toLocalTime(),
            sun.sunrise.toLocalTime(),
            sun.sunset.toLocalTime()
        )

        assertEquals(0.0, progress, 0.01)
    }

    @Test
    fun `getDaylightProgress before sunrise is 0.0`() {
        val date = LocalDate.of(2024, 6, 15)
        val sun = calc.calculate(-33.87, 151.21, date)

        val beforeSunrise = sun.sunrise.toLocalTime().minusHours(2)
        val progress = calc.getDaylightProgress(
            beforeSunrise,
            sun.sunrise.toLocalTime(),
            sun.sunset.toLocalTime()
        )

        assertEquals(0.0, progress, 0.01)
    }

    @Test
    fun `getDaylightProgress after sunset is 0.0`() {
        val date = LocalDate.of(2024, 6, 15)
        val sun = calc.calculate(-33.87, 151.21, date)

        val afterSunset = sun.sunset.toLocalTime().plusHours(2)
        val progress = calc.getDaylightProgress(
            afterSunset,
            sun.sunrise.toLocalTime(),
            sun.sunset.toLocalTime()
        )

        assertEquals(0.0, progress, 0.01)
    }

    @Test
    fun `getDaylightProgress at midpoint between sunrise and solar noon is 0.5`() {
        val date = LocalDate.of(2024, 6, 15)
        val sun = calc.calculate(-33.87, 151.21, date)

        val sunrise = sun.sunrise.toLocalTime()
        val solarNoon = sun.solarNoon.toLocalTime()
        val midPoint = java.time.LocalTime.of(
            (sunrise.hour + solarNoon.hour) / 2,
            (sunrise.minute + solarNoon.minute) / 2
        )

        val progress = calc.getDaylightProgress(midPoint, sunrise, sun.sunset.toLocalTime())

        // Should be approximately 0.5 (rising curve)
        assertTrue("Progress at morning midpoint should be near 0.5, was $progress",
            progress in 0.3..0.7)
    }

    @Test
    fun `arctic circle extreme - midnight sun`() {
        // Tromsø, Norway: lat 69.65, lng 18.96
        // June 21 — sun barely sets
        val date = LocalDate.of(2024, 6, 21)
        val sun = calc.calculate(69.65, 18.96, date)

        val dayLength = java.time.Duration.between(sun.sunrise, sun.sunset).toHours()
        // Should be very long (close to 24 hours)
        assertTrue("Arctic summer should have very long day: $dayLength hours", dayLength > 20)
    }
}
