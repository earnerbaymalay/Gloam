package com.gloam.data.repository

import com.gloam.data.db.GloamDatabase
import com.gloam.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * SQLDelight-backed implementation of [GloamRepository] for Android.
 */
class GloamRepositoryImpl(private val database: GloamDatabase) : GloamRepository {

    private val entryQueries = database.gloamDatabaseQueries
    private val moodQueries = database.gloamDatabaseQueries
    private val promptQueries = database.gloamDatabaseQueries

    // ── Entries ───────────────────────────────────────────────────────────────

    override fun getAllEntries(): Flow<List<JournalEntry>> = flow {
        emit(entryQueries.getAllEntries().executeAsList().map { it.toModel() })
    }

    override fun getEntriesForDate(date: LocalDate): Flow<List<JournalEntry>> = flow {
        emit(entryQueries.getEntriesForDate(date.toString()).executeAsList().map { it.toModel() })
    }

    override suspend fun getEntry(date: LocalDate, type: EntryType): JournalEntry? =
        entryQueries.getEntry(date.toString(), type.name).executeAsOneOrNull()?.toModel()

    override fun getEntriesInRange(start: LocalDate, end: LocalDate): Flow<List<JournalEntry>> = flow {
        emit(entryQueries.getEntriesInRange(start.toString(), end.toString()).executeAsList().map { it.toModel() })
    }

    override suspend fun saveEntry(entry: JournalEntry): Long {
        entryQueries.insertEntry(
            date = entry.date.toString(),
            entryType = entry.entryType.name,
            moodScore = entry.moodScore.toLong(),
            prompt1 = "",
            response1 = entry.prompt1Response,
            prompt2 = "",
            response2 = entry.prompt2Response,
            prompt3 = "",
            response3 = entry.prompt3Response,
            createdAt = entry.createdAt.toString()
        )
        updateMoodRecord(entry.date)
        return entryQueries.getEntry(entry.date.toString(), entry.entryType.name)
            .executeAsOneOrNull()?.id ?: 0L
    }

    override suspend fun updateEntry(entry: JournalEntry) {
        entryQueries.updateEntry(
            moodScore = entry.moodScore.toLong(),
            response1 = entry.prompt1Response,
            response2 = entry.prompt2Response,
            response3 = entry.prompt3Response,
            id = entry.id
        )
        updateMoodRecord(entry.date)
    }

    override suspend fun deleteEntry(entry: JournalEntry) {
        entryQueries.deleteEntry(entry.id)
        updateMoodRecord(entry.date)
    }

    // ── Mood records ──────────────────────────────────────────────────────────

    override fun getAllMoodRecords(): Flow<List<MoodRecord>> = flow {
        emit(moodQueries.getAllMoodRecords().executeAsList().map { it.toModel() })
    }

    override fun getMoodRecordsInRange(start: LocalDate, end: LocalDate): Flow<List<MoodRecord>> = flow {
        emit(moodQueries.getMoodRecordsInRange(start.toString(), end.toString()).executeAsList().map { it.toModel() })
    }

    override suspend fun getMoodForDate(date: LocalDate): MoodRecord? =
        moodQueries.getMoodForDate(date.toString()).executeAsOneOrNull()?.toModel()

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

        moodQueries.upsertMoodRecord(
            date = date.toString(),
            averageMood = average,
            sunriseMood = sunriseMood?.toLong(),
            sunsetMood = sunsetMood?.toLong()
        )
    }

    // ── Prompts ───────────────────────────────────────────────────────────────

    override suspend fun getPromptsForType(type: EntryType): List<Prompt> =
        promptQueries.getPromptsForType(type.name).executeAsList().map { it.toModel() }

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
        val records = moodQueries.getMoodRecordsInRange(start.toString(), end.toString()).executeAsList()
        return if (records.isEmpty()) null else records.map { it.averageMood?.toFloat() ?: 0f }.average().toFloat()
    }

    override fun getYearMoodRecords(year: Int): Flow<List<MoodRecord>> {
        val start = LocalDate(year, 1, 1)
        val end = LocalDate(year, 12, 31)
        return getMoodRecordsInRange(start, end)
    }
}

// ── Row mappers ───────────────────────────────────────────────────────────────

private fun com.gloam.data.db.GetAllEntries.toModel(): JournalEntry {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return JournalEntry(
        id = id,
        date = kotlinx.datetime.LocalDate.parse(date),
        entryType = EntryType.valueOf(entryType),
        moodScore = moodScore.toInt(),
        prompt1Response = response1,
        prompt2Response = response2,
        prompt3Response = response3,
        createdAt = runCatching { kotlinx.datetime.LocalDateTime.parse(createdAt) }.getOrDefault(now)
    )
}

private fun com.gloam.data.db.GetEntriesForDate.toModel(): JournalEntry {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return JournalEntry(
        id = id,
        date = kotlinx.datetime.LocalDate.parse(date),
        entryType = EntryType.valueOf(entryType),
        moodScore = moodScore.toInt(),
        prompt1Response = response1,
        prompt2Response = response2,
        prompt3Response = response3,
        createdAt = runCatching { kotlinx.datetime.LocalDateTime.parse(createdAt) }.getOrDefault(now)
    )
}

private fun com.gloam.data.db.GetEntry.toModel(): JournalEntry {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return JournalEntry(
        id = id,
        date = kotlinx.datetime.LocalDate.parse(date),
        entryType = EntryType.valueOf(entryType),
        moodScore = moodScore.toInt(),
        prompt1Response = response1,
        prompt2Response = response2,
        prompt3Response = response3,
        createdAt = runCatching { kotlinx.datetime.LocalDateTime.parse(createdAt) }.getOrDefault(now)
    )
}

private fun com.gloam.data.db.GetEntriesInRange.toModel(): JournalEntry {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return JournalEntry(
        id = id,
        date = kotlinx.datetime.LocalDate.parse(date),
        entryType = EntryType.valueOf(entryType),
        moodScore = moodScore.toInt(),
        prompt1Response = response1,
        prompt2Response = response2,
        prompt3Response = response3,
        createdAt = runCatching { kotlinx.datetime.LocalDateTime.parse(createdAt) }.getOrDefault(now)
    )
}

private fun com.gloam.data.db.GetAllMoodRecords.toModel(): MoodRecord =
    MoodRecord(
        date = kotlinx.datetime.LocalDate.parse(date),
        averageMood = averageMood?.toFloat() ?: 0f,
        sunriseMood = sunriseMood?.toInt(),
        sunsetMood = sunsetMood?.toInt()
    )

private fun com.gloam.data.db.GetMoodRecordsInRange.toModel(): MoodRecord =
    MoodRecord(
        date = kotlinx.datetime.LocalDate.parse(date),
        averageMood = averageMood?.toFloat() ?: 0f,
        sunriseMood = sunriseMood?.toInt(),
        sunsetMood = sunsetMood?.toInt()
    )

private fun com.gloam.data.db.GetMoodForDate.toModel(): MoodRecord =
    MoodRecord(
        date = kotlinx.datetime.LocalDate.parse(date),
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
