package com.gloam.data.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * A single journal entry — platform-agnostic data class.
 * No Room annotations; those live in platform-specific source sets.
 */
data class JournalEntry(
    val id: String,
    val date: LocalDate,
    val entryType: EntryType,
    val moodScore: Int,
    val prompt1Response: String,
    val prompt2Response: String,
    val prompt3Response: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class EntryType {
    SUNRISE, SUNSET
}

data class MoodRecord(
    val date: LocalDate,
    val averageMood: Float?,
    val sunriseMood: Int?,
    val sunsetMood: Int?
)

data class Prompt(
    val id: String,
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
