package com.gloam.data.db

import com.gloam.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import java.sql.*
import java.util.*

/**
 * Desktop SQLite implementation of GloamDatabase.
 * Uses sqlite-jdbc for cross-platform SQLite access.
 */
class DesktopGloamDatabase(dbPath: String) : GloamDatabase {

    private val dbUrl = "jdbc:sqlite:$dbPath"
    private var connection: Connection? = null

    init {
        initializeDatabase()
    }

    private fun getConnection(): Connection {
        if (connection?.isClosed != false) {
            connection = DriverManager.getConnection(dbUrl)
            connection?.autoCommit = true
        }
        return connection!!
    }

    private fun initializeDatabase() {
        val conn = getConnection()
        conn.createStatement().use { stmt ->
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS journal_entries (
                    id TEXT PRIMARY KEY,
                    date TEXT NOT NULL,
                    entry_type TEXT NOT NULL,
                    mood_score INTEGER NOT NULL,
                    prompt1_response TEXT NOT NULL DEFAULT '',
                    prompt2_response TEXT NOT NULL DEFAULT '',
                    prompt3_response TEXT NOT NULL DEFAULT '',
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            """)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mood_records (
                    date TEXT PRIMARY KEY,
                    average_mood REAL,
                    sunrise_mood INTEGER,
                    sunset_mood INTEGER
                )
            """)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prompts (
                    id TEXT PRIMARY KEY,
                    text TEXT NOT NULL,
                    category TEXT NOT NULL,
                    entry_type TEXT NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1
                )
            """)

            // Seed prompts if empty
            val rs = stmt.executeQuery("SELECT COUNT(*) FROM prompts")
            if (rs.next() && rs.getInt(1) == 0) {
                seedDefaultPrompts(stmt)
            }
        }
    }

    private fun seedDefaultPrompts(stmt: Statement) {
        val prompts = listOf(
            listOf("s1", "What emotions am I carrying right now?", "EMOTIONAL_CHECKIN", "SUNRISE"),
            listOf("s2", "What would make today feel meaningful?", "INTENTION", "SUNRISE"),
            listOf("s3", "What thought pattern could I reframe today?", "CBT_REFRAME", "SUNRISE"),
            listOf("s4", "What challenged me most today?", "REFLECTION", "SUNSET"),
            listOf("s5", "What three things am I grateful for?", "GRATITUDE", "SUNSET"),
            listOf("s6", "How can I release today's tension before sleep?", "CBT_CLOSURE", "SUNSET"),
        )
        for (p in prompts) {
            stmt.execute("INSERT OR IGNORE INTO prompts (id, text, category, entry_type, is_active) VALUES ('${p[0]}', '${p[1]}', '${p[2]}', '${p[3]}', 1)")
        }
    }

    // ── DAOs ──

    override val journalEntryDao = object : JournalEntryDao {
        override fun getAllEntries(): Flow<List<JournalEntry>> = callbackFlow {
            val conn = getConnection()
            trySend(fetchEntries("SELECT * FROM journal_entries ORDER BY date DESC"))
            channel.close()
        }

        override suspend fun getEntriesForDate(date: LocalDate): List<JournalEntry> {
            val conn = getConnection()
            return conn.prepareStatement("SELECT * FROM journal_entries WHERE date = ?").use { ps ->
                ps.setString(1, date.toString())
                ps.executeQuery().mapToEntries()
            }
        }

        override suspend fun getEntry(id: String): JournalEntry? {
            val conn = getConnection()
            return conn.prepareStatement("SELECT * FROM journal_entries WHERE id = ?").use { ps ->
                ps.setString(1, id)
                ps.executeQuery().toFirstEntry()
            }
        }

        override suspend fun getEntriesInRange(start: LocalDate, end: LocalDate): List<JournalEntry> {
            val conn = getConnection()
            return conn.prepareStatement("SELECT * FROM journal_entries WHERE date BETWEEN ? AND ? ORDER BY date DESC").use { ps ->
                ps.setString(1, start.toString())
                ps.setString(2, end.toString())
                ps.executeQuery().mapToEntries()
            }
        }

        override suspend fun insert(entry: JournalEntry) {
            val conn = getConnection()
            conn.prepareStatement(
                "INSERT OR REPLACE INTO journal_entries (id, date, entry_type, mood_score, prompt1_response, prompt2_response, prompt3_response, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            ).use { ps ->
                ps.setString(1, entry.id)
                ps.setString(2, entry.date.toString())
                ps.setString(3, entry.entryType.name)
                ps.setInt(4, entry.moodScore)
                ps.setString(5, entry.prompt1Response)
                ps.setString(6, entry.prompt2Response)
                ps.setString(7, entry.prompt3Response)
                ps.setString(8, entry.createdAt.toString())
                ps.setString(9, entry.updatedAt.toString())
                ps.executeUpdate()
            }
        }

        override suspend fun update(entry: JournalEntry) = insert(entry)

        override suspend fun delete(entry: JournalEntry) {
            val conn = getConnection()
            conn.prepareStatement("DELETE FROM journal_entries WHERE id = ?").use { ps ->
                ps.setString(1, entry.id)
                ps.executeUpdate()
            }
        }
    }

    override val moodRecordDao = object : MoodRecordDao {
        override fun getAllMoodRecords(): Flow<List<MoodRecord>> = callbackFlow {
            val conn = getConnection()
            trySend(fetchMoodRecords("SELECT * FROM mood_records ORDER BY date DESC"))
            channel.close()
        }

        override suspend fun getMoodRecordsInRange(start: LocalDate, end: LocalDate): List<MoodRecord> {
            val conn = getConnection()
            return conn.prepareStatement("SELECT * FROM mood_records WHERE date BETWEEN ? AND ?").use { ps ->
                ps.setString(1, start.toString())
                ps.setString(2, end.toString())
                ps.executeQuery().mapToMoodRecords()
            }
        }

        override suspend fun getMoodForDate(date: LocalDate): MoodRecord? {
            val conn = getConnection()
            return conn.prepareStatement("SELECT * FROM mood_records WHERE date = ?").use { ps ->
                ps.setString(1, date.toString())
                ps.executeQuery().toFirstMoodRecord()
            }
        }

        override suspend fun insert(record: MoodRecord) {
            val conn = getConnection()
            conn.prepareStatement(
                "INSERT OR REPLACE INTO mood_records (date, average_mood, sunrise_mood, sunset_mood) VALUES (?, ?, ?, ?)"
            ).use { ps ->
                ps.setString(1, record.date.toString())
                if (record.averageMood != null) ps.setFloat(2, record.averageMood) else ps.setNull(2, Types.REAL)
                if (record.sunriseMood != null) ps.setInt(3, record.sunriseMood) else ps.setNull(3, Types.INTEGER)
                if (record.sunsetMood != null) ps.setInt(4, record.sunsetMood) else ps.setNull(4, Types.INTEGER)
                ps.executeUpdate()
            }
        }

        override suspend fun getAverageMoodInRange(start: LocalDate, end: LocalDate): Float? {
            val records = getMoodRecordsInRange(start, end)
            return records.mapNotNull { it.averageMood }.average().toFloatOrNull()
        }
    }

    override val promptDao = object : PromptDao {
        override suspend fun getPromptsForType(type: EntryType): List<Prompt> {
            val conn = getConnection()
            return conn.prepareStatement("SELECT * FROM prompts WHERE entry_type = ? AND is_active = 1").use { ps ->
                ps.setString(1, type.name)
                ps.executeQuery().mapToPrompts()
            }
        }

        override suspend fun getPromptsByCategory(category: PromptCategory, type: EntryType): List<Prompt> {
            val conn = getConnection()
            return conn.prepareStatement("SELECT * FROM prompts WHERE category = ? AND entry_type = ?").use { ps ->
                ps.setString(1, category.name)
                ps.setString(2, type.name)
                ps.executeQuery().mapToPrompts()
            }
        }

        override suspend fun insert(prompt: Prompt) {
            val conn = getConnection()
            conn.prepareStatement(
                "INSERT OR IGNORE INTO prompts (id, text, category, entry_type, is_active) VALUES (?, ?, ?, ?, ?)"
            ).use { ps ->
                ps.setString(1, prompt.id)
                ps.setString(2, prompt.text)
                ps.setString(3, prompt.category.name)
                ps.setString(4, prompt.entryType.name)
                ps.setInt(5, if (prompt.isActive) 1 else 0)
                ps.executeUpdate()
            }
        }

        override suspend fun insertAll(prompts: List<Prompt>) {
            prompts.forEach { insert(it) }
        }

        override suspend fun getPromptCount(): Int {
            val conn = getConnection()
            return conn.prepareStatement("SELECT COUNT(*) FROM prompts").use { ps ->
                val rs = ps.executeQuery()
                if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    override suspend fun close() {
        connection?.close()
    }

    // ── Helpers ──

    private fun fetchEntries(sql: String): List<JournalEntry> {
        val conn = getConnection()
        return conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).mapToEntries()
        }
    }

    private fun fetchMoodRecords(sql: String): List<MoodRecord> {
        val conn = getConnection()
        return conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).mapToMoodRecords()
        }
    }

    private fun ResultSet.mapToEntries(): List<JournalEntry> {
        val entries = mutableListOf<JournalEntry>()
        while (next()) {
            entries.add(
                JournalEntry(
                    id = getString("id"),
                    date = LocalDate.parse(getString("date")),
                    entryType = EntryType.valueOf(getString("entry_type")),
                    moodScore = getInt("mood_score"),
                    prompt1Response = getString("prompt1_response"),
                    prompt2Response = getString("prompt2_response"),
                    prompt3Response = getString("prompt3_response"),
                    createdAt = LocalDateTime.parse(getString("created_at")),
                    updatedAt = LocalDateTime.parse(getString("updated_at"))
                )
            )
        }
        return entries
    }

    private fun ResultSet.toFirstEntry(): JournalEntry? = if (next()) mapToEntries().first() else null

    private fun ResultSet.mapToMoodRecords(): List<MoodRecord> {
        val records = mutableListOf<MoodRecord>()
        while (next()) {
            records.add(
                MoodRecord(
                    date = LocalDate.parse(getString("date")),
                    averageMood = getFloat("average_mood").takeIf { it != 0f && !wasNull() },
                    sunriseMood = getInt("sunrise_mood").takeIf { it != 0 && !wasNull() },
                    sunsetMood = getInt("sunset_mood").takeIf { it != 0 && !wasNull() }
                )
            )
        }
        return records
    }

    private fun ResultSet.toFirstMoodRecord(): MoodRecord? = if (next()) mapToMoodRecords().first() else null

    private fun ResultSet.mapToPrompts(): List<Prompt> {
        val prompts = mutableListOf<Prompt>()
        while (next()) {
            prompts.add(
                Prompt(
                    id = getString("id"),
                    text = getString("text"),
                    category = PromptCategory.valueOf(getString("category")),
                    entryType = EntryType.valueOf(getString("entry_type")),
                    isActive = getInt("is_active") == 1
                )
            )
        }
        return prompts
    }
}
