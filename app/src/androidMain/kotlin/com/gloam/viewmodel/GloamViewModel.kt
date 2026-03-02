package com.gloam.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gloam.GloamApplication
import com.gloam.data.model.*
import com.gloam.util.SunCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GloamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as GloamApplication).repository

    // Location state (default to Sydney, Australia)
    private val _latitude = MutableStateFlow(-33.8688)
    private val _longitude = MutableStateFlow(151.2093)

    val latitude: StateFlow<Double> = _latitude.asStateFlow()
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    fun updateLocation(lat: Double, lon: Double) {
        _latitude.value = lat
        _longitude.value = lon
    }

    // Sun times — single source of truth computed once per location change
    val sunTimes: StateFlow<SunCalculator.SunTimes> = combine(_latitude, _longitude) { lat, lon ->
        SunCalculator.calculate(lat, lon)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SunCalculator.SunTimes(
            sunrise = LocalTime(6, 0),
            sunset = LocalTime(18, 0),
            solarNoon = LocalTime(12, 0)
        )
    )

    // Daylight progress derived from sunTimes — no extra SunCalculator.calculate() call
    val daylightProgress: StateFlow<Float> = sunTimes.map { times ->
        val nowTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        val currentMinutes = nowTime.hour * 60 + nowTime.minute
        val sunriseMinutes = times.sunrise.hour * 60 + times.sunrise.minute
        val sunsetMinutes = times.sunset.hour * 60 + times.sunset.minute
        val noonMinutes = times.solarNoon.hour * 60 + times.solarNoon.minute
        when {
            currentMinutes < sunriseMinutes ->
                (currentMinutes.toFloat() / sunriseMinutes) * 0.1f
            currentMinutes < noonMinutes -> {
                val progress = (currentMinutes - sunriseMinutes).toFloat() / (noonMinutes - sunriseMinutes)
                0.1f + progress * 0.9f
            }
            currentMinutes < sunsetMinutes -> {
                val progress = (currentMinutes - noonMinutes).toFloat() / (sunsetMinutes - noonMinutes)
                1.0f - progress * 0.9f
            }
            else -> {
                val remaining = 1440 - currentMinutes
                val afterSunset = 1440 - sunsetMinutes
                (remaining.toFloat() / afterSunset) * 0.1f
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.7f)

    // Current entry type derived from sunTimes — no extra SunCalculator.calculate() call
    val currentEntryType: StateFlow<EntryType> = sunTimes.map { times ->
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        if (now < times.solarNoon) EntryType.SUNRISE else EntryType.SUNSET
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EntryType.SUNRISE)

    // Today's date — update to re-trigger todayEntries reactive collection
    private val _today = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )

    // Today's entries — reactive StateFlow, no manual collect/leak
    val todayEntries: StateFlow<List<JournalEntry>> = _today.flatMapLatest { date ->
        repository.getEntriesForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current prompts
    private val _currentPrompts = MutableStateFlow<Triple<Prompt, Prompt, Prompt>?>(null)
    val currentPrompts: StateFlow<Triple<Prompt, Prompt, Prompt>?> = _currentPrompts.asStateFlow()

    // All entries for list view
    val allEntries: Flow<List<JournalEntry>> = repository.getAllEntries()

    // Year mood records for calendar
    private val _selectedYear = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.year
    )
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    val yearMoodRecords: Flow<List<MoodRecord>> = _selectedYear.flatMapLatest { year ->
        repository.getYearMoodRecords(year)
    }

    // Selected date for viewing entries
    private val _selectedDate = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val selectedDateEntries: Flow<List<JournalEntry>> = _selectedDate.flatMapLatest { date ->
        repository.getEntriesForDate(date)
    }

    // Entry being edited
    private val _editingEntry = MutableStateFlow<JournalEntry?>(null)
    val editingEntry: StateFlow<JournalEntry?> = _editingEntry.asStateFlow()

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPromptsForType(type: EntryType) {
        viewModelScope.launch {
            _currentPrompts.value = repository.getRandomPromptsForEntry(type)
        }
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setEditingEntry(entry: JournalEntry?) {
        _editingEntry.value = entry
    }

    fun saveEntry(
        entryType: EntryType,
        moodScore: Int,
        response1: String,
        response2: String,
        response3: String,
        existingEntry: JournalEntry? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val today = now.date

            val entry = existingEntry?.copy(
                moodScore = moodScore,
                prompt1Response = response1,
                prompt2Response = response2,
                prompt3Response = response3,
                updatedAt = now
            ) ?: JournalEntry(
                date = today,
                entryType = entryType,
                moodScore = moodScore,
                prompt1Response = response1,
                prompt2Response = response2,
                prompt3Response = response3
            )

            if (existingEntry != null) {
                repository.updateEntry(entry)
            } else {
                repository.saveEntry(entry)
            }

            // Refresh today's reactive flow by re-emitting today's date
            _today.value = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            _isLoading.value = false
        }
    }

    fun updateEntry(entry: JournalEntry) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateEntry(entry)
            _today.value = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            _isLoading.value = false
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            _today.value = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
    }

    fun hasEntryForToday(type: EntryType): Boolean {
        return todayEntries.value.any { it.entryType == type }
    }

    fun getTodayEntry(type: EntryType): JournalEntry? {
        return todayEntries.value.find { it.entryType == type }
    }
}
