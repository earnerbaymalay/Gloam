package com.gloam.data.repository

import com.gloam.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Platform-agnostic repository interface.
 * Concrete implementations are provided per-platform and wire up SQLDelight queries.
 */
interface GloamRepository {

    // Entries
    fun getAllEntries(): Flow<List<JournalEntry>>

    fun getEntriesForDate(date: LocalDate): Flow<List<JournalEntry>>

    suspend fun getEntry(date: LocalDate, type: EntryType): JournalEntry?

    fun getEntriesInRange(start: LocalDate, end: LocalDate): Flow<List<JournalEntry>>

    suspend fun saveEntry(entry: JournalEntry): Long

    suspend fun updateEntry(entry: JournalEntry)

    suspend fun deleteEntry(entry: JournalEntry)

    // Mood records
    fun getAllMoodRecords(): Flow<List<MoodRecord>>

    fun getMoodRecordsInRange(start: LocalDate, end: LocalDate): Flow<List<MoodRecord>>

    suspend fun getMoodForDate(date: LocalDate): MoodRecord?

    // Prompts
    suspend fun getPromptsForType(type: EntryType): List<Prompt>

    suspend fun getRandomPromptsForEntry(type: EntryType): Triple<Prompt, Prompt, Prompt>

    // Stats
    suspend fun getAverageMoodInRange(start: LocalDate, end: LocalDate): Float?

    fun getYearMoodRecords(year: Int): Flow<List<MoodRecord>>
}
