package com.gloam.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gloam.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [JournalEntry::class, MoodRecord::class, Prompt::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GloamDatabase : RoomDatabase() {
    
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun moodRecordDao(): MoodRecordDao
    abstract fun promptDao(): PromptDao
    
    companion object {
        @Volatile
        private var INSTANCE: GloamDatabase? = null
        
        fun getDatabase(context: Context): GloamDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = getOrCreatePassphrase(context)
                val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase))
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GloamDatabase::class.java,
                    "gloam_encrypted.db"
                )
                    .openHelperFactory(factory)
                    .addCallback(DatabaseCallback(context))
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        private fun getOrCreatePassphrase(context: Context): CharArray {
            val prefs = context.getSharedPreferences("gloam_secure", Context.MODE_PRIVATE)
            val existing = prefs.getString("db_key", null)
            
            return if (existing != null) {
                existing.toCharArray()
            } else {
                val newKey = generateSecureKey()
                prefs.edit().putString("db_key", newKey).apply()
                newKey.toCharArray()
            }
        }
        
        private fun generateSecureKey(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
            return (1..32).map { chars.random() }.joinToString("")
        }
    }
    
    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultPrompts(database.promptDao())
                }
            }
        }
        
        private suspend fun populateDefaultPrompts(promptDao: PromptDao) {
            val defaultPrompts = listOf(
                // Sunrise prompts
                Prompt(text = "How are you feeling this morning?", category = PromptCategory.EMOTIONAL_CHECKIN, entryType = EntryType.SUNRISE),
                Prompt(text = "What emotions are present as you start your day?", category = PromptCategory.EMOTIONAL_CHECKIN, entryType = EntryType.SUNRISE),
                Prompt(text = "Check in with your body — where are you holding tension?", category = PromptCategory.EMOTIONAL_CHECKIN, entryType = EntryType.SUNRISE),
                
                Prompt(text = "What would make today meaningful?", category = PromptCategory.INTENTION, entryType = EntryType.SUNRISE),
                Prompt(text = "What's one thing you want to accomplish today?", category = PromptCategory.INTENTION, entryType = EntryType.SUNRISE),
                Prompt(text = "How do you want to feel at the end of today?", category = PromptCategory.INTENTION, entryType = EntryType.SUNRISE),
                
                Prompt(text = "What's one thought you want to challenge today?", category = PromptCategory.CBT_REFRAME, entryType = EntryType.SUNRISE),
                Prompt(text = "What unhelpful thinking pattern might show up today?", category = PromptCategory.CBT_REFRAME, entryType = EntryType.SUNRISE),
                Prompt(text = "What would you tell a friend who had your worries?", category = PromptCategory.CBT_REFRAME, entryType = EntryType.SUNRISE),
                
                // Sunset prompts
                Prompt(text = "What happened today that affected your mood?", category = PromptCategory.REFLECTION, entryType = EntryType.SUNSET),
                Prompt(text = "What moments stood out today, good or difficult?", category = PromptCategory.REFLECTION, entryType = EntryType.SUNSET),
                Prompt(text = "How did your day compare to your morning intentions?", category = PromptCategory.REFLECTION, entryType = EntryType.SUNSET),
                
                Prompt(text = "Name one thing you're grateful for today.", category = PromptCategory.GRATITUDE, entryType = EntryType.SUNSET),
                Prompt(text = "What small pleasure did you notice today?", category = PromptCategory.GRATITUDE, entryType = EntryType.SUNSET),
                Prompt(text = "Who or what made today a little better?", category = PromptCategory.GRATITUDE, entryType = EntryType.SUNSET),
                
                Prompt(text = "What did you learn about yourself today?", category = PromptCategory.CBT_CLOSURE, entryType = EntryType.SUNSET),
                Prompt(text = "What thought patterns showed up today? Were they helpful?", category = PromptCategory.CBT_CLOSURE, entryType = EntryType.SUNSET),
                Prompt(text = "How could you be kinder to yourself tomorrow?", category = PromptCategory.CBT_CLOSURE, entryType = EntryType.SUNSET)
            )
            promptDao.insertAll(defaultPrompts)
        }
    }
}
