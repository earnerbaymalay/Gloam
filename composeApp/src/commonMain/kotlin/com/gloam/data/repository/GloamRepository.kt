package com.gloam.data.repository

import com.gloam.data.db.GloamDatabase
import com.gloam.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.random.Random

class GloamRepository(private val db: GloamDatabase) {

    // ── Journal Entries ──

    fun getAllEntries(): Flow<List<JournalEntry>> = db.journalEntryDao.getAllEntries()

    suspend fun getEntriesForDate(date: LocalDate): List<JournalEntry> =
        db.journalEntryDao.getEntriesForDate(date)

    suspend fun getEntry(id: String): JournalEntry? = db.journalEntryDao.getEntry(id)

    suspend fun getEntriesInRange(start: LocalDate, end: LocalDate): List<JournalEntry> =
        db.journalEntryDao.getEntriesInRange(start, end)

    suspend fun saveEntry(entry: JournalEntry) {
        db.journalEntryDao.insert(entry)
        updateMoodRecord(entry.date)
    }

    suspend fun updateEntry(entry: JournalEntry) {
        db.journalEntryDao.update(entry)
        updateMoodRecord(entry.date)
    }

    suspend fun deleteEntry(entry: JournalEntry) {
        db.journalEntryDao.delete(entry)
        updateMoodRecord(entry.date)
    }

    // ── Mood Records ──

    fun getAllMoodRecords(): Flow<List<MoodRecord>> = db.moodRecordDao.getAllMoodRecords()

    suspend fun getMoodRecordsInRange(start: LocalDate, end: LocalDate): List<MoodRecord> =
        db.moodRecordDao.getMoodRecordsInRange(start, end)

    suspend fun getMoodForDate(date: LocalDate): MoodRecord? =
        db.moodRecordDao.getMoodForDate(date)

    suspend fun getAverageMoodInRange(start: LocalDate, end: LocalDate): Float? =
        db.moodRecordDao.getAverageMoodInRange(start, end)

    private suspend fun updateMoodRecord(date: LocalDate) {
        val entries = db.journalEntryDao.getEntriesForDate(date)
        val sunriseMood = entries.find { it.entryType == EntryType.SUNRISE }?.moodScore
        val sunsetMood = entries.find { it.entryType == EntryType.SUNSET }?.moodScore
        val averageMood = listOfNotNull(sunriseMood, sunsetMood).map { it.toFloat() }.average().toFloatOrNull()

        val existing = db.moodRecordDao.getMoodForDate(date)
        val record = MoodRecord(
            date = date,
            averageMood = averageMood,
            sunriseMood = sunriseMood,
            sunsetMood = sunsetMood
        )

        if (existing != null) {
            db.moodRecordDao.insert(record) // Replace
        } else {
            db.moodRecordDao.insert(record)
        }
    }

    // ── Prompts ──

    suspend fun getPromptsForType(type: EntryType): List<Prompt> =
        db.promptDao.getPromptsForType(type)

    suspend fun getRandomPromptsForType(type: EntryType): Triple<Prompt, Prompt, Prompt> {
        val prompts = db.promptDao.getPromptsForType(type)
        if (prompts.size < 3) {
            return getFallbackPrompts(type)
        }

        // Pick one per category
        val byCategory = prompts.groupBy { it.category }
        val relevantCategories = when (type) {
            EntryType.SUNRISE -> listOf(
                PromptCategory.EMOTIONAL_CHECKIN,
                PromptCategory.INTENTION,
                PromptCategory.CBT_REFRAME
            )
            EntryType.SUNSET -> listOf(
                PromptCategory.REFLECTION,
                PromptCategory.GRATITUDE,
                PromptCategory.CBT_CLOSURE
            )
        }

        val selected = relevantCategories.mapNotNull { category ->
            byCategory[category]?.let { it[Random.nextInt(it.size)] }
        }

        return if (selected.size >= 3) {
            Triple(selected[0], selected[1], selected[2])
        } else {
            getFallbackPrompts(type)
        }
    }

    private fun getFallbackPrompts(type: EntryType): Triple<Prompt, Prompt, Prompt> {
        val fallbacks = when (type) {
            EntryType.SUNRISE -> Triple(
                Prompt("fb1", "What emotions am I carrying right now?", PromptCategory.EMOTIONAL_CHECKIN, EntryType.SUNRISE),
                Prompt("fb2", "What would make today feel meaningful?", PromptCategory.INTENTION, EntryType.SUNRISE),
                Prompt("fb3", "What thought pattern could I reframe today?", PromptCategory.CBT_REFRAME, EntryType.SUNRISE)
            )
            EntryType.SUNSET -> Triple(
                Prompt("fb4", "What challenged me most today?", PromptCategory.REFLECTION, EntryType.SUNSET),
                Prompt("fb5", "What three things am I grateful for?", PromptCategory.GRATITUDE, EntryType.SUNSET),
                Prompt("fb6", "How can I release today's tension before sleep?", PromptCategory.CBT_CLOSURE, EntryType.SUNSET)
            )
        }
        return fallbacks
    }

    suspend fun getYearMoodRecords(year: Int): List<MoodRecord> {
        val start = LocalDate(year, 1, 1)
        val end = LocalDate(year, 12, 31)
        return db.moodRecordDao.getMoodRecordsInRange(start, end)
    }
}
