package com.gloam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val entryType: EntryType, // SUNRISE or SUNSET
    val moodScore: Int, // 1-5
    val prompt1Response: String,
    val prompt2Response: String,
    val prompt3Response: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class EntryType {
    SUNRISE, SUNSET
}

@Entity(tableName = "mood_records")
data class MoodRecord(
    @PrimaryKey
    val date: LocalDate,
    val averageMood: Float, // Average of sunrise + sunset
    val sunriseMood: Int?,
    val sunsetMood: Int?
)

@Entity(tableName = "prompts")
data class Prompt(
    @PrimaryKey(autoGenerate = true)
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
