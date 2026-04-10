package com.gloam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gloam.data.model.*
import com.gloam.data.repository.GloamRepository
import com.gloam.util.SunCalculator
import com.gloam.util.SunTimes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class GloamUiState(
    val sunTimes: SunTimes? = null,
    val daylightProgress: Float = 0f,
    val currentEntryType: EntryType = EntryType.SUNRISE,
    val todayEntries: List<JournalEntry> = emptyList(),
    val currentPrompts: Triple<Prompt, Prompt, Prompt>? = null,
    val yearMoodRecords: List<MoodRecord> = emptyList(),
    val selectedDateEntries: List<JournalEntry> = emptyList(),
    val editingEntry: JournalEntry? = null,
    val isLoading: Boolean = true,
    val latitude: Double = -33.87,
    val longitude: Double = 151.21,
    val selectedYear: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
)

class GloamViewModel(
    private val repository: GloamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GloamUiState())
    val uiState: StateFlow<GloamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            calculateSunTimes()
            loadTodayEntries()
            loadYearMoodRecords()
        }
    }

    // ── Location ──

    fun updateLocation(lat: Double, lon: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lon) }
        viewModelScope.launch {
            calculateSunTimes()
        }
    }

    // ── Sun Calculation ──

    private suspend fun calculateSunTimes() {
        val state = _uiState.value
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val sunTimes = SunCalculator.calculate(state.latitude, state.longitude, now.date)
        val progress = SunCalculator.getDaylightProgress(now.time, sunTimes.sunrise.time, sunTimes.sunset.time).toFloat()
        val entryType = if (now.time < sunTimes.solarNoon.time) EntryType.SUNRISE else EntryType.SUNSET

        _uiState.update {
            it.copy(
                sunTimes = sunTimes,
                daylightProgress = progress,
                currentEntryType = entryType
            )
        }

        loadPromptsForType(entryType)
    }

    // ── Entries ──

    fun loadTodayEntries() {
        viewModelScope.launch {
            val state = _uiState.value
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val entries = repository.getEntriesForDate(now.date)
            _uiState.update { it.copy(todayEntries = entries, isLoading = false) }
        }
    }

    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            val entries = repository.getEntriesForDate(date)
            _uiState.update { it.copy(selectedDateEntries = entries) }
        }
    }

    fun selectYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
        loadYearMoodRecords()
    }

    // ── Prompts ──

    fun loadPromptsForType(type: EntryType) {
        viewModelScope.launch {
            val prompts = repository.getRandomPromptsForType(type)
            _uiState.update { it.copy(currentPrompts = prompts) }
        }
    }

    // ── CRUD ──

    fun setEditingEntry(entry: JournalEntry?) {
        _uiState.update { it.copy(editingEntry = entry) }
    }

    fun saveEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.saveEntry(entry)
            loadTodayEntries()
            _uiState.update { it.copy(editingEntry = null) }
        }
    }

    fun updateEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry)
            loadTodayEntries()
            _uiState.update { it.copy(editingEntry = null) }
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            loadTodayEntries()
            _uiState.update { it.copy(editingEntry = null) }
        }
    }

    fun hasEntryForToday(): Boolean {
        return _uiState.value.todayEntries.isNotEmpty()
    }

    fun getTodayEntry(): JournalEntry? {
        return _uiState.value.todayEntries.firstOrNull()
    }

    // ── Mood Records ──

    private fun loadYearMoodRecords() {
        viewModelScope.launch {
            val records = repository.getYearMoodRecords(_uiState.value.selectedYear)
            _uiState.update { it.copy(yearMoodRecords = records) }
        }
    }
}
