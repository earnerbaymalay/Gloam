package com.gloam.data.repository

import com.gloam.data.db.GloamDatabase
import com.gloam.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GloamRepository(private val database: GloamDatabase) {
    
    private val entryDao = database.journalEntryDao()
    private val moodDao = database.moodRecordDao()
    private val promptDao = database.promptDao()
    
    // Entries
    fun getAllEntries(): Flow<List<JournalEntry>> = entryDao.getAllEntries()
    
    fun getEntriesForDate(date: LocalDate): Flow<List<JournalEntry>> = 
        entryDao.getEntriesForDate(date)
    
    suspend fun getEntry(date: LocalDate, type: EntryType): JournalEntry? =
        entryDao.getEntry(date, type)
    
    fun getEntriesInRange(start: LocalDate, end: LocalDate): Flow<List<JournalEntry>> =
        entryDao.getEntriesInRange(start, end)
    
    suspend fun saveEntry(entry: JournalEntry): Long {
        val id = entryDao.insert(entry)
        updateMoodRecord(entry.date)
        return id
    }
    
    suspend fun updateEntry(entry: JournalEntry) {
        entryDao.update(entry)
        updateMoodRecord(entry.date)
    }
    
    suspend fun deleteEntry(entry: JournalEntry) {
        entryDao.delete(entry)
        updateMoodRecord(entry.date)
    }
    
    // Mood records
    fun getAllMoodRecords(): Flow<List<MoodRecord>> = moodDao.getAllMoodRecords()
    
    fun getMoodRecordsInRange(start: LocalDate, end: LocalDate): Flow<List<MoodRecord>> =
        moodDao.getMoodRecordsInRange(start, end)
    
    suspend fun getMoodForDate(date: LocalDate): MoodRecord? = moodDao.getMoodForDate(date)
    
    private suspend fun updateMoodRecord(date: LocalDate) {
        val sunriseEntry = entryDao.getEntry(date, EntryType.SUNRISE)
        val sunsetEntry = entryDao.getEntry(date, EntryType.SUNSET)
        
        val sunriseMood = sunriseEntry?.moodScore
        val sunsetMood = sunsetEntry?.moodScore
        
        val average = when {
            sunriseMood != null && sunsetMood != null -> (sunriseMood + sunsetMood) / 2f
            sunriseMood != null -> sunriseMood.toFloat()
            sunsetMood != null -> sunsetMood.toFloat()
            else -> return
        }
        
        moodDao.insert(MoodRecord(date, average, sunriseMood, sunsetMood))
    }
    
    // Prompts
    suspend fun getPromptsForType(type: EntryType): List<Prompt> = 
        promptDao.getPromptsForType(type)
    
    suspend fun getRandomPromptsForEntry(type: EntryType): Triple<Prompt, Prompt, Prompt> {
        val prompts = promptDao.getPromptsForType(type)
        val categories = when (type) {
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
        
        val prompt1 = prompts.filter { it.category == categories[0] }.randomOrNull()
            ?: Prompt(text = "How are you feeling?", category = categories[0], entryType = type)
        val prompt2 = prompts.filter { it.category == categories[1] }.randomOrNull()
            ?: Prompt(text = "What's on your mind?", category = categories[1], entryType = type)
        val prompt3 = prompts.filter { it.category == categories[2] }.randomOrNull()
            ?: Prompt(text = "Any thoughts to reflect on?", category = categories[2], entryType = type)
        
        return Triple(prompt1, prompt2, prompt3)
    }
    
    // Stats
    suspend fun getAverageMoodInRange(start: LocalDate, end: LocalDate): Float? =
        moodDao.getAverageMoodInRange(start, end)
    
    fun getYearMoodRecords(year: Int): Flow<List<MoodRecord>> {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        return moodDao.getMoodRecordsInRange(start, end)
    }
}
