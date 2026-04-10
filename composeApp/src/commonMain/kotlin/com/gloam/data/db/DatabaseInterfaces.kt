package com.gloam.data.db

import com.gloam.data.model.*
import com.gloam.data.repository.GloamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Platform-agnostic database interface.
 * Implemented by Room (Android) and SQLite (Desktop).
 */
interface GloamDatabase {
    val journalEntryDao: JournalEntryDao
    val moodRecordDao: MoodRecordDao
    val promptDao: PromptDao
    val repository: GloamRepository

    suspend fun close()
}

interface JournalEntryDao {
    fun getAllEntries(): Flow<List<JournalEntry>>
    suspend fun getEntriesForDate(date: LocalDate): List<JournalEntry>
    suspend fun getEntry(id: String): JournalEntry?
    suspend fun getEntriesInRange(start: LocalDate, end: LocalDate): List<JournalEntry>
    suspend fun insert(entry: JournalEntry)
    suspend fun update(entry: JournalEntry)
    suspend fun delete(entry: JournalEntry)
}

interface MoodRecordDao {
    fun getAllMoodRecords(): Flow<List<MoodRecord>>
    suspend fun getMoodRecordsInRange(start: LocalDate, end: LocalDate): List<MoodRecord>
    suspend fun getMoodForDate(date: LocalDate): MoodRecord?
    suspend fun insert(record: MoodRecord)
    suspend fun getAverageMoodInRange(start: LocalDate, end: LocalDate): Float?
}

interface PromptDao {
    suspend fun getPromptsForType(type: EntryType): List<Prompt>
    suspend fun getPromptsByCategory(category: PromptCategory, type: EntryType): List<Prompt>
    suspend fun insert(prompt: Prompt)
    suspend fun insertAll(prompts: List<Prompt>)
    suspend fun getPromptCount(): Int
}
