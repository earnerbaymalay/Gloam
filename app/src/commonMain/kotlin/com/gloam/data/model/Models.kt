package com.gloam.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class JournalEntry(
    val id: Long = 0,
    val date: LocalDate,
    val entryType: EntryType, // SUNRISE or SUNSET
    val moodScore: Int, // 1-5
    val prompt1Response: String,
    val prompt2Response: String,
    val prompt3Response: String,
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val updatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)

enum class EntryType {
    SUNRISE, SUNSET
}

data class MoodRecord(
    val date: LocalDate,
    val averageMood: Float, // Average of sunrise + sunset
    val sunriseMood: Int?,
    val sunsetMood: Int?
)

data class Prompt(
    val id: Long = 0,
    val text: String,
    val category: PromptCategory,
    val entryType: EntryType,
    val isActive: Boolean = true
)

enum class PromptCategory {
    EMOTIONAL_CHECKIN,
    INTENTION,
    CBT_REFRAME,
    REFLECTION,
    GRATITUDE,
    CBT_CLOSURE
}
