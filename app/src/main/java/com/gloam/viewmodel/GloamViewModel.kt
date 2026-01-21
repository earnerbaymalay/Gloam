package com.gloam.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gloam.GloamApplication
import com.gloam.data.model.*
import com.gloam.data.repository.GloamRepository
import com.gloam.util.SunCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class GloamViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = GloamRepository((application as GloamApplication).database)
    
    // Location state (default to Sydney, Australia)
    private val _latitude = MutableStateFlow(-33.8688)
    private val _longitude = MutableStateFlow(151.2093)
    
    val latitude: StateFlow<Double> = _latitude.asStateFlow()
    val longitude: StateFlow<Double> = _longitude.asStateFlow()
    
    fun updateLocation(lat: Double, lon: Double) {
        _latitude.value = lat
        _longitude.value = lon
    }
    
    // Daylight progress for theme
    val daylightProgress: StateFlow<Float> = combine(_latitude, _longitude) { lat, lon ->
        SunCalculator.getDaylightProgress(lat, lon)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.7f)
    
    // Sun times
    val sunTimes: StateFlow<SunCalculator.SunTimes> = combine(_latitude, _longitude) { lat, lon ->
        SunCalculator.calculate(lat, lon)
    }.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5000),
        SunCalculator.SunTimes(
            sunrise = java.time.LocalTime.of(6, 0),
            sunset = java.time.LocalTime.of(18, 0),
            solarNoon = java.time.LocalTime.of(12, 0)
        )
    )
    
    // Current entry type based on time
    val currentEntryType: StateFlow<EntryType> = combine(_latitude, _longitude) { lat, lon ->
        val times = SunCalculator.calculate(lat, lon)
        val now = java.time.LocalTime.now()
        if (now.isBefore(times.solarNoon)) EntryType.SUNRISE else EntryType.SUNSET
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EntryType.SUNRISE)
    
    // Today's entries
    private val _todayEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val todayEntries: StateFlow<List<JournalEntry>> = _todayEntries.asStateFlow()
    
    // Current prompts
    private val _currentPrompts = MutableStateFlow<Triple<Prompt, Prompt, Prompt>?>(null)
    val currentPrompts: StateFlow<Triple<Prompt, Prompt, Prompt>?> = _currentPrompts.asStateFlow()
    
    // All entries for list view
    val allEntries: Flow<List<JournalEntry>> = repository.getAllEntries()
    
    // Year mood records for calendar
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()
    
    val yearMoodRecords: Flow<List<MoodRecord>> = _selectedYear.flatMapLatest { year ->
        repository.getYearMoodRecords(year)
    }
    
    // Selected date for viewing entries
    private val _selectedDate = MutableStateFlow(LocalDate.now())
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
    
    init {
        loadTodayEntries()
    }
    
    fun loadTodayEntries() {
        viewModelScope.launch {
            repository.getEntriesForDate(LocalDate.now()).collect { entries ->
                _todayEntries.value = entries
            }
        }
    }
    
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
            
            val entry = existingEntry?.copy(
                moodScore = moodScore,
                prompt1Response = response1,
                prompt2Response = response2,
                prompt3Response = response3,
                updatedAt = LocalDateTime.now()
            ) ?: JournalEntry(
                date = LocalDate.now(),
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
            
            loadTodayEntries()
            _isLoading.value = false
        }
    }
    
    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            loadTodayEntries()
        }
    }
    
    fun hasEntryForToday(type: EntryType): Boolean {
        return _todayEntries.value.any { it.entryType == type }
    }
    
    fun getTodayEntry(type: EntryType): JournalEntry? {
        return _todayEntries.value.find { it.entryType == type }
    }
}
