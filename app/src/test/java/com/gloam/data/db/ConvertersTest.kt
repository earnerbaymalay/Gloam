package com.gloam.data.db

import com.gloam.data.model.EntryType
import com.gloam.data.model.PromptCategory
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for Room TypeConverters.
 * These are pure Java/Kotlin conversions — no Android dependencies needed.
 */
class ConvertersTest {

    private val converters = Converters()

    // ── LocalDate ──

    @Test
    fun `LocalDate to Long and back`() {
        val date = LocalDate.of(2024, 6, 15)
        val timestamp = converters.fromLocalDate(date)
        val recovered = converters.toLocalDate(timestamp)

        assertEquals(date, recovered)
    }

    @Test
    fun `LocalDate epoch is consistent`() {
        val date = LocalDate.of(2024, 1, 1)
        val timestamp = converters.fromLocalDate(date)

        // Should be a valid epoch millisecond
        assertTrue(timestamp > 0)
        assertTrue(timestamp < 2000000000000L) // Before year 2033
    }

    // ── LocalDateTime ──

    @Test
    fun `LocalDateTime to Long and back`() {
        val dateTime = LocalDateTime.of(2024, 6, 15, 14, 30)
        val timestamp = converters.fromLocalDateTime(dateTime)
        val recovered = converters.toLocalDateTime(timestamp)

        assertEquals(dateTime, recovered)
    }

    @Test
    fun `LocalDateTime preserves time components`() {
        val dateTime = LocalDateTime.of(2024, 12, 25, 8, 15, 30)
        val timestamp = converters.fromLocalDateTime(dateTime)
        val recovered = converters.toLocalDateTime(timestamp)

        assertEquals(2024, recovered.year)
        assertEquals(12, recovered.monthValue)
        assertEquals(25, recovered.dayOfMonth)
        assertEquals(8, recovered.hour)
        assertEquals(15, recovered.minute)
    }

    // ── EntryType ──

    @Test
    fun `EntryType SUNRISE to Int and back`() {
        val type = EntryType.SUNRISE
        val intVal = converters.fromEntryType(type)
        val recovered = converters.toEntryType(intVal)

        assertEquals(type, recovered)
    }

    @Test
    fun `EntryType SUNSET to Int and back`() {
        val type = EntryType.SUNSET
        val intVal = converters.fromEntryType(type)
        val recovered = converters.toEntryType(intVal)

        assertEquals(type, recovered)
    }

    @Test
    fun `EntryType integer values are distinct`() {
        val sunriseInt = converters.fromEntryType(EntryType.SUNRISE)
        val sunsetInt = converters.fromEntryType(EntryType.SUNSET)

        assertNotEquals(sunriseInt, sunsetInt)
    }

    // ── PromptCategory ──

    @Test
    fun `PromptCategory to Int and back - all categories`() {
        for (category in PromptCategory.values()) {
            val intVal = converters.fromPromptCategory(category)
            val recovered = converters.toPromptCategory(intVal)
            assertEquals(category, recovered)
        }
    }

    @Test
    fun `PromptCategory integer values are unique`() {
        val intValues = PromptCategory.values().map { converters.fromPromptCategory(it) }
        assertEquals(intValues.size, intValues.toSet().size)
    }

    @Test
    fun `PromptCategory count matches expected`() {
        assertEquals(6, PromptCategory.values().size)
    }
}
