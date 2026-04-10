package com.gloam.data.db

import com.gloam.data.repository.GloamRepository
import android.content.Context
import androidx.room.*
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gloam.data.model.*
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

// ── Room Entities (Android-specific) ──

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "date") val dateStr: String,
    @ColumnInfo(name = "entry_type") val entryType: String,
    @ColumnInfo(name = "mood_score") val moodScore: Int,
    @ColumnInfo(name = "prompt1_response") val prompt1Response: String,
    @ColumnInfo(name = "prompt2_response") val prompt2Response: String,
    @ColumnInfo(name = "prompt3_response") val prompt3Response: String,
    @ColumnInfo(name = "created_at") val createdAtStr: String,
    @ColumnInfo(name = "updated_at") val updatedAtStr: String
) {
    fun toModel(): JournalEntry = JournalEntry(
        id = id,
        date = LocalDate.parse(dateStr),
        entryType = EntryType.valueOf(entryType),
        moodScore = moodScore,
        prompt1Response = prompt1Response,
        prompt2Response = prompt2Response,
        prompt3Response = prompt3Response,
        createdAt = LocalDateTime.parse(createdAtStr),
        updatedAt = LocalDateTime.parse(updatedAtStr)
    )

    companion object {
        fun fromModel(entry: JournalEntry): JournalEntryEntity = JournalEntryEntity(
            id = entry.id,
            dateStr = entry.date.toString(),
            entryType = entry.entryType.name,
            moodScore = entry.moodScore,
            prompt1Response = entry.prompt1Response,
            prompt2Response = entry.prompt2Response,
            prompt3Response = entry.prompt3Response,
            createdAtStr = entry.createdAt.toString(),
            updatedAtStr = entry.updatedAt.toString()
        )
    }
}

@Entity(tableName = "mood_records")
data class MoodRecordEntity(
    @PrimaryKey val dateStr: String,
    @ColumnInfo(name = "average_mood") val averageMood: Float?,
    @ColumnInfo(name = "sunrise_mood") val sunriseMood: Int?,
    @ColumnInfo(name = "sunset_mood") val sunsetMood: Int?
) {
    fun toModel(): MoodRecord = MoodRecord(
        date = LocalDate.parse(dateStr),
        averageMood = averageMood,
        sunriseMood = sunriseMood,
        sunsetMood = sunsetMood
    )

    companion object {
        fun fromModel(record: MoodRecord): MoodRecordEntity = MoodRecordEntity(
            dateStr = record.date.toString(),
            averageMood = record.averageMood,
            sunriseMood = record.sunriseMood,
            sunsetMood = record.sunsetMood
        )
    }
}

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "entry_type") val entryType: String,
    @ColumnInfo(name = "is_active") val isActive: Boolean
) {
    fun toModel(): Prompt = Prompt(
        id = id,
        text = text,
        category = PromptCategory.valueOf(category),
        entryType = EntryType.valueOf(entryType),
        isActive = isActive
    )

    companion object {
        fun fromModel(prompt: Prompt): PromptEntity = PromptEntity(
            id = prompt.id,
            text = prompt.text,
            category = prompt.category.name,
            entryType = prompt.entryType.name,
            isActive = prompt.isActive
        )
    }
}

// ── DAOs ──

@Dao
interface RoomJournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllFlow(): kotlinx.coroutines.flow.Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE date = :dateStr")
    suspend fun getByDate(dateStr: String): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    suspend fun getInRange(start: String, end: String): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getById(id: String): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: JournalEntryEntity)

    @Update
    suspend fun update(entity: JournalEntryEntity)

    @Delete
    suspend fun delete(entity: JournalEntryEntity)
}

@Dao
interface RoomMoodRecordDao {
    @Query("SELECT * FROM mood_records ORDER BY dateStr DESC")
    fun getAllFlow(): kotlinx.coroutines.flow.Flow<List<MoodRecordEntity>>

    @Query("SELECT * FROM mood_records WHERE dateStr BETWEEN :start AND :end")
    suspend fun getInRange(start: String, end: String): List<MoodRecordEntity>

    @Query("SELECT * FROM mood_records WHERE dateStr = :dateStr")
    suspend fun getByDate(dateStr: String): MoodRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MoodRecordEntity)
}

@Dao
interface RoomPromptDao {
    @Query("SELECT * FROM prompts WHERE entry_type = :type AND is_active = 1")
    suspend fun getByType(type: String): List<PromptEntity>

    @Query("SELECT * FROM prompts WHERE category = :cat AND entry_type = :type")
    suspend fun getByCategory(cat: String, type: String): List<PromptEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: PromptEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<PromptEntity>)

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun getCount(): Int
}

// ── Database ──

@Database(
    entities = [JournalEntryEntity::class, MoodRecordEntity::class, PromptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AndroidGloamDatabase : RoomDatabase() {
    abstract fun journalEntryDao(): RoomJournalEntryDao
    abstract fun moodRecordDao(): RoomMoodRecordDao
    abstract fun promptDao(): RoomPromptDao

    companion object {
        private const val DB_NAME = "gloam.db"
        @Volatile private var INSTANCE: AndroidGloamDatabase? = null

        fun getDatabase(context: Context, passphrase: ByteArray): AndroidGloamDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val factory = SupportFactory(passphrase)
                    Room.databaseBuilder(
                        context.applicationContext,
                        AndroidGloamDatabase::class.java,
                        DB_NAME
                    )
                    .openHelperFactory(factory)
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                    .also { INSTANCE = it }
                }
            }
    }
}

class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Seed default CBT prompts directly via SQL (no recursive getDatabase call)
        val defaultPrompts = listOf(
            arrayOf("s1", "What emotions am I carrying right now?", "EMOTIONAL_CHECKIN", "SUNRISE", 1),
            arrayOf("s2", "What would make today feel meaningful?", "INTENTION", "SUNRISE", 1),
            arrayOf("s3", "What thought pattern could I reframe today?", "CBT_REFRAME", "SUNRISE", 1),
            arrayOf("s4", "What challenged me most today?", "REFLECTION", "SUNSET", 1),
            arrayOf("s5", "What three things am I grateful for?", "GRATITUDE", "SUNSET", 1),
            arrayOf("s6", "How can I release today's tension before sleep?", "CBT_CLOSURE", "SUNSET", 1),
        )
        for (p in defaultPrompts) {
            db.execSQL(
                "INSERT OR IGNORE INTO prompts (id, text, category, entry_type, is_active) VALUES (?,?,?,?,?)",
                arrayOf(p[0], p[1], p[2], p[3], p[4])
            )
        }
    }
}

// ── Adapter implementations ──

class AndroidJournalEntryDao(private val roomDao: RoomJournalEntryDao) : JournalEntryDao {
    override fun getAllEntries(): kotlinx.coroutines.flow.Flow<List<JournalEntry>> =
        roomDao.getAllFlow().map { it.map { e -> e.toModel() } }

    override suspend fun getEntriesForDate(date: LocalDate): List<JournalEntry> =
        roomDao.getByDate(date.toString()).map { it.toModel() }

    override suspend fun getEntry(id: String): JournalEntry? =
        roomDao.getById(id)?.toModel()

    override suspend fun getEntriesInRange(start: LocalDate, end: LocalDate): List<JournalEntry> =
        roomDao.getInRange(start.toString(), end.toString()).map { it.toModel() }

    override suspend fun insert(entry: JournalEntry) =
        roomDao.insert(JournalEntryEntity.fromModel(entry))

    override suspend fun update(entry: JournalEntry) =
        roomDao.update(JournalEntryEntity.fromModel(entry))

    override suspend fun delete(entry: JournalEntry) =
        roomDao.delete(JournalEntryEntity.fromModel(entry))
}

class AndroidMoodRecordDao(private val roomDao: RoomMoodRecordDao) : MoodRecordDao {
    override fun getAllMoodRecords(): kotlinx.coroutines.flow.Flow<List<MoodRecord>> =
        roomDao.getAllFlow().map { it.map { e -> e.toModel() } }

    override suspend fun getMoodRecordsInRange(start: LocalDate, end: LocalDate): List<MoodRecord> =
        roomDao.getInRange(start.toString(), end.toString()).map { it.toModel() }

    override suspend fun getMoodForDate(date: LocalDate): MoodRecord? =
        roomDao.getByDate(date.toString())?.toModel()

    override suspend fun insert(record: MoodRecord) =
        roomDao.insert(MoodRecordEntity.fromModel(record))

    override suspend fun getAverageMoodInRange(start: LocalDate, end: LocalDate): Float? {
        val records = roomDao.getInRange(start.toString(), end.toString())
        val averages = records.mapNotNull { it.averageMood }
        return if (averages.isEmpty()) null else averages.average().toFloat()
    }
}

class AndroidPromptDao(private val roomDao: RoomPromptDao) : PromptDao {
    override suspend fun getPromptsForType(type: EntryType): List<Prompt> =
        roomDao.getByType(type.name).map { it.toModel() }

    override suspend fun getPromptsByCategory(category: PromptCategory, type: EntryType): List<Prompt> =
        roomDao.getByCategory(category.name, type.name).map { it.toModel() }

    override suspend fun insert(prompt: Prompt) =
        roomDao.insert(PromptEntity.fromModel(prompt))

    override suspend fun insertAll(prompts: List<Prompt>) =
        roomDao.insertAll(prompts.map { PromptEntity.fromModel(it) })

    override suspend fun getPromptCount(): Int = roomDao.getCount()
}

class AndroidGloamDatabaseAdapter(private val db: AndroidGloamDatabase) : GloamDatabase {
    private val _journalEntryDao = AndroidJournalEntryDao(db.journalEntryDao())
    private val _moodRecordDao = AndroidMoodRecordDao(db.moodRecordDao())
    private val _promptDao = AndroidPromptDao(db.promptDao())
    override val repository = GloamRepository(this)

    override val journalEntryDao: JournalEntryDao get() = _journalEntryDao
    override val moodRecordDao: MoodRecordDao get() = _moodRecordDao
    override val promptDao: PromptDao get() = _promptDao

    override suspend fun close() {
        // Room doesn't need explicit close in most cases
    }
}
