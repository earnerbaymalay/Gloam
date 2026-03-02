package com.gloam.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gloam.data.db.GloamDatabase
import com.gloam.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * SQLDelight-backed implementation of [GloamRepository] for Android.
 */
class GloamRepositoryImpl(private val database: GloamDatabase) : GloamRepository {

    private val queries = database.gloamDatabaseQueries

    // ── Entries ───────────────────────────────────────────────────────────────

    override fun getAllEntries(): Flow<List<JournalEntry>> =
        queries.getAllEntries().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toModel() }
        }

    override fun getEntriesForDate(date: LocalDate): Flow<List<JournalEntry>> =
        queries.getEntriesForDate(date.toString()).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toModel() }
        }

    override suspend fun getEntry(date: LocalDate, type: EntryType): JournalEntry? =
        queries.getEntry(date.toString(), type.name).executeAsOneOrNull()?.toModel()

    override fun getEntriesInRange(start: LocalDate, end: LocalDate): Flow<List<JournalEntry>> =
        queries.getEntriesInRange(start.toString(), end.toString()).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toModel() }
        }

    override suspend fun saveEntry(entry: JournalEntry): Long {
        queries.insertEntry(
            date = entry.date.toString(),
            entryType = entry.entryType.name,
            moodScore = entry.moodScore.toLong(),
            prompt1 = "",
            response1 = entry.prompt1Response,
            prompt2 = "",
            response2 = entry.prompt2Response,
            prompt3 = "",
            response3 = entry.prompt3Response,
            createdAt = entry.createdAt.toString(),
            updatedAt = entry.updatedAt.toString()
        )
        updateMoodRecord(entry.date)
        return queries.getEntry(entry.date.toString(), entry.entryType.name)
            .executeAsOneOrNull()?.id ?: 0L
    }

    override suspend fun updateEntry(entry: JournalEntry) {
        queries.updateEntry(
            moodScore = entry.moodScore.toLong(),
            response1 = entry.prompt1Response,
            response2 = entry.prompt2Response,
            response3 = entry.prompt3Response,
            id = entry.id
        )
        updateMoodRecord(entry.date)
    }

    override suspend fun deleteEntry(entry: JournalEntry) {
        queries.deleteEntry(entry.id)
        updateMoodRecord(entry.date)
    }

    // ── Mood records ──────────────────────────────────────────────────────────

    override fun getAllMoodRecords(): Flow<List<MoodRecord>> =
        queries.getAllMoodRecords().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toModel() }
        }

    override fun getMoodRecordsInRange(start: LocalDate, end: LocalDate): Flow<List<MoodRecord>> =
        queries.getMoodRecordsInRange(start.toString(), end.toString()).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toModel() }
        }

    override suspend fun getMoodForDate(date: LocalDate): MoodRecord? =
        queries.getMoodForDate(date.toString()).executeAsOneOrNull()?.toModel()

    private suspend fun updateMoodRecord(date: LocalDate) {
        val sunriseEntry = getEntry(date, EntryType.SUNRISE)
        val sunsetEntry = getEntry(date, EntryType.SUNSET)

        val sunriseMood = sunriseEntry?.moodScore
        val sunsetMood = sunsetEntry?.moodScore

        val average = when {
            sunriseMood != null && sunsetMood != null -> (sunriseMood + sunsetMood) / 2.0
            sunriseMood != null -> sunriseMood.toDouble()
            sunsetMood != null -> sunsetMood.toDouble()
            else -> return
        }

        queries.upsertMoodRecord(
            date = date.toString(),
            averageMood = average,
            sunriseMood = sunriseMood?.toLong(),
            sunsetMood = sunsetMood?.toLong()
        )
    }

    // ── Prompts ───────────────────────────────────────────────────────────────

    override suspend fun getPromptsForType(type: EntryType): List<Prompt> =
        queries.getPromptsForType(type.name).executeAsList().map { it.toModel() }

    override suspend fun getRandomPromptsForEntry(type: EntryType): Triple<Prompt, Prompt, Prompt> {
        val prompts = getPromptsForType(type)
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

    // ── Stats ─────────────────────────────────────────────────────────────────

    override suspend fun getAverageMoodInRange(start: LocalDate, end: LocalDate): Float? {
        val records = queries.getMoodRecordsInRange(start.toString(), end.toString()).executeAsList()
        return if (records.isEmpty()) null else records.map { it.averageMood?.toFloat() ?: 0f }.average().toFloat()
    }

    override fun getYearMoodRecords(year: Int): Flow<List<MoodRecord>> {
        val start = LocalDate(year, 1, 1)
        val end = LocalDate(year, 12, 31)
        return getMoodRecordsInRange(start, end)
    }
}

// ── Shared entry row mapper ────────────────────────────────────────────────────

private fun mapToJournalEntry(
    id: Long,
    date: String,
    entryType: String,
    moodScore: Long,
    response1: String,
    response2: String,
    response3: String,
    createdAt: String,
    updatedAt: String
): JournalEntry {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return JournalEntry(
        id = id,
        date = LocalDate.parse(date),
        entryType = EntryType.valueOf(entryType),
        moodScore = moodScore.toInt(),
        prompt1Response = response1,
        prompt2Response = response2,
        prompt3Response = response3,
        createdAt = runCatching { LocalDateTime.parse(createdAt) }.getOrDefault(now),
        updatedAt = runCatching { LocalDateTime.parse(updatedAt) }.getOrDefault(now)
    )
}

// ── Row mappers ───────────────────────────────────────────────────────────────

private fun com.gloam.data.db.GetAllEntries.toModel(): JournalEntry =
    mapToJournalEntry(id, date, entryType, moodScore, response1, response2, response3, createdAt, updatedAt)

private fun com.gloam.data.db.GetEntriesForDate.toModel(): JournalEntry =
    mapToJournalEntry(id, date, entryType, moodScore, response1, response2, response3, createdAt, updatedAt)

private fun com.gloam.data.db.GetEntry.toModel(): JournalEntry =
    mapToJournalEntry(id, date, entryType, moodScore, response1, response2, response3, createdAt, updatedAt)

private fun com.gloam.data.db.GetEntriesInRange.toModel(): JournalEntry =
    mapToJournalEntry(id, date, entryType, moodScore, response1, response2, response3, createdAt, updatedAt)

private fun com.gloam.data.db.GetAllMoodRecords.toModel(): MoodRecord =
    MoodRecord(
        date = LocalDate.parse(date),
        averageMood = averageMood?.toFloat() ?: 0f,
        sunriseMood = sunriseMood?.toInt(),
        sunsetMood = sunsetMood?.toInt()
    )

private fun com.gloam.data.db.GetMoodRecordsInRange.toModel(): MoodRecord =
    MoodRecord(
        date = LocalDate.parse(date),
        averageMood = averageMood?.toFloat() ?: 0f,
        sunriseMood = sunriseMood?.toInt(),
        sunsetMood = sunsetMood?.toInt()
    )

private fun com.gloam.data.db.GetMoodForDate.toModel(): MoodRecord =
    MoodRecord(
        date = LocalDate.parse(date),
        averageMood = averageMood?.toFloat() ?: 0f,
        sunriseMood = sunriseMood?.toInt(),
        sunsetMood = sunsetMood?.toInt()
    )

private fun com.gloam.data.db.GetPromptsForType.toModel(): Prompt =
    Prompt(
        id = id,
        text = text,
        category = PromptCategory.valueOf(category),
        entryType = EntryType.valueOf(entryType)
    )
