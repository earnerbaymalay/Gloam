package com.gloam.data.db

import androidx.room.*
import com.gloam.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM entries ORDER BY date DESC, entryType ASC")
    fun getAllEntries(): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM entries WHERE date = :date")
    fun getEntriesForDate(date: LocalDate): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM entries WHERE date = :date AND entryType = :type LIMIT 1")
    suspend fun getEntry(date: LocalDate, type: EntryType): JournalEntry?
    
    @Query("SELECT * FROM entries WHERE date BETWEEN :startDate AND :endDate")
    fun getEntriesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<JournalEntry>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry): Long
    
    @Update
    suspend fun update(entry: JournalEntry)
    
    @Delete
    suspend fun delete(entry: JournalEntry)
}

@Dao
interface MoodRecordDao {
    @Query("SELECT * FROM mood_records ORDER BY date DESC")
    fun getAllMoodRecords(): Flow<List<MoodRecord>>
    
    @Query("SELECT * FROM mood_records WHERE date BETWEEN :startDate AND :endDate")
    fun getMoodRecordsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<MoodRecord>>
    
    @Query("SELECT * FROM mood_records WHERE date = :date LIMIT 1")
    suspend fun getMoodForDate(date: LocalDate): MoodRecord?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MoodRecord)
    
    @Query("SELECT AVG(averageMood) FROM mood_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageMoodInRange(startDate: LocalDate, endDate: LocalDate): Float?
}

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts WHERE entryType = :type AND isActive = 1")
    suspend fun getPromptsForType(type: EntryType): List<Prompt>
    
    @Query("SELECT * FROM prompts WHERE category = :category AND entryType = :type AND isActive = 1")
    suspend fun getPromptsByCategory(category: PromptCategory, type: EntryType): List<Prompt>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prompt: Prompt)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prompts: List<Prompt>)
    
    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun getPromptCount(): Int
}
