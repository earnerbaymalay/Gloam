# 🏗️ Architecture

## Overview

Gloam is a **single-module Android application** built with Kotlin and Jetpack Compose. It follows a **Clean Architecture-inspired** pattern with clear separation between data, presentation, and UI layers.

```
┌──────────────────────────────────────────────────┐
│                    MainActivity                    │
│              (NavHost + PIN + Export)             │
├──────────────────────────────────────────────────┤
│                  GloamViewModel                   │
│        (UI state, location, sun, CRUD ops)        │
├──────────────┬───────────────────────────────────┤
│  GloamRepository                                   │
│  (CRUD, mood auto-calc, prompt randomization)     │
├──────────────┼───────────────────────────────────┤
│  JournalEntryDao  │  MoodRecordDao  │  PromptDao  │
├──────────────┼───────────────────────────────────┤
│              GloamDatabase (Room + SQLCipher)      │
├──────────────┼───────────────────────────────────┤
│  SunCalculator  │  NotificationScheduler          │
└──────────────┴───────────────────────────────────┘
```

## Package Structure

### `com.gloam` — Application Entry
- `GloamApplication` — Application class, lazy-initialized Room database singleton
- `MainActivity` — Hosts the Compose NavHost, handles PIN lock flow, location permissions, JSON export
- Navigation: 4 screens via sealed `Screen` class — Home, Calendar, Entries, Settings

### `com.gloam.data.model` — Data Models
- `JournalEntry` — A single journal entry: date, EntryType (SUNRISE/SUNSET), moodScore (1-5), 3 CBT prompt responses
- `MoodRecord` — Daily aggregated mood: date, average mood, sunrise mood, sunset mood
- `Prompt` — CBT prompt template: text, category, associated entry type
- `EntryType` — SUNRISE or SUNSET (determines which prompts to show)
- `PromptCategory` — 6 CBT categories: emotional check-in, intention, cognitive reframing, reflection, gratitude, closure

### `com.gloam.data.db` — Database
- `GloamDatabase` — Room database with SQLCipher encryption. Generates random 32-char passphrase stored in SharedPreferences. Seeds 18 default CBT prompts on first run.
- `Converters` — Room TypeConverters for `LocalDate`, `LocalDateTime`, `EntryType`, `PromptCategory`
- `JournalEntryDao` — CRUD + date-based queries + range queries
- `MoodRecordDao` — CRUD + range queries + average calculation
- `PromptDao` — Query by type, category, random selection, count

### `com.gloam.data.repository` — Repository
- `GloamRepository` — Wraps all 3 DAOs. Key logic:
  - `saveEntry` / `updateEntry` auto-update `MoodRecord` by averaging sunrise + sunset moods
  - `getRandomPromptsForEntry` picks one prompt per relevant category
  - `getYearMoodRecords` returns full year data for heatmap

### `com.gloam.viewmodel` — Presentation
- `GloamViewModel` — AndroidViewModel managing all UI state:
  - Location state (default Sydney: -33.87, 151.21)
  - Daylight progress for theme interpolation
  - Sunrise/sunset times
  - Current entry type (sunrise vs sunset based on solar noon)
  - Today's entries, prompts, year mood records
  - Selected date entries
  - CRUD operations for entries

### `com.gloam.ui.theme` — Theming
- `Color.kt` — Light palette, dark palette, mood colors (5-point scale), transition colors
- `Theme.kt` — `gloamColors()` interpolates between dark/light based on daylight progress using `lerp()`
- `Type.kt` — Full Material3 typography scale

### `com.gloam.ui.components` — Reusable UI
- `PinLock.kt` — 4-digit PIN screen with setup (enter + confirm), error states, biometric placeholder
- `Calendar.kt` — Year-in-pixels grid + traditional month calendar
- `MoodSelector.kt` — 5-emoji animated mood picker with scale + color animations

### `com.gloam.ui.screens` — Screen Composables
- `home/HomeScreen.kt` — Solar header, journal entry card with mood + prompts, completed entry view
- `calendar/CalendarScreen.kt` — Year selector, mood stats, mood legend, year-in-pixels
- `entries/EntriesScreen.kt` — Chronological list grouped by date
- `entries/EntryDetailScreen.kt` — Full entry view with edit, mood change, delete
- `settings/SettingsScreen.kt` — PIN toggle, JSON export, about

### `com.gloam.util` — Utilities
- `SunCalculator.kt` — NOAA solar algorithm: calculates sunrise, sunset, solar noon for any lat/lng/date
- `NotificationUtils.kt` — AlarmManager-based sunrise/sunset reminders, boot recovery, notification channels

## Data Flow

### New Journal Entry
```
User opens Home screen
        │
        ▼
GloamViewModel.loadPromptsForType(entryType)
        │
        ▼
Repository.getRandomPromptsForType(entryType)
        │
        ├──► Random 1 prompt per category
        │
        ▼
HomeScreen renders mood selector + 3 prompt fields
        │
        ▼
User fills in → taps Save
        │
        ▼
GloamViewModel.saveEntry(entry)
        │
        ├──► Repository.saveEntry()
        │       ├──► JournalEntryDao.insert()
        │       └──► MoodRecord update (recalculate average)
        │
        ▼
HomeScreen shows completed entry card
```

### Solar Theme Calculation
```
GloamViewModel initializes
        │
        ▼
Gets location (GPS → default Sydney)
        │
        ▼
SunCalculator.calculate(lat, lng, today)
        │
        ├──► sunrise: 06:32
        ├──► sunset: 17:45
        └──► solarNoon: 12:08
        │
        ▼
getDaylightProgress(currentTime, sunrise, sunset)
        │
        ├──► 0.0 = midnight (dark)
        ├──► 0.5 = solar noon (bright)
        └──► 1.0 = midnight (dark)
        │
        ▼
gloamColors(daylightProgress)
        │
        ├──► lerp(darkColors, lightColors, progress)
        │
        ▼
GloamTheme applies to Material3
```

### Mood Record Calculation
```
User saves sunrise entry (mood: 4/5)
        │
        ▼
Repository.saveEntry()
        │
        ├──► Insert JournalEntry
        │
        ▼
Check if MoodRecord exists for today
        │
        ├──► No: create new MoodRecord with sunriseMood = 4
        │
        ▼
User saves_sunset entry (mood: 3/5)
        │
        ▼
Repository.updateEntry()
        │
        ├──► Update JournalEntry
        │
        ▼
Update MoodRecord: sunsetMood = 3, averageMood = (4+3)/2 = 3.5
```

## Security Model

| Layer | Mechanism | Purpose |
|---|---|---|
| **Database** | SQLCipher (AES-256) | Encrypts entire Room database file |
| **Passphrase** | Random 32 chars in SharedPreferences | Derives SQLCipher key |
| **PIN** | SHA-256 hash stored in SharedPreferences | App-level authentication |
| **Backups** | `allowBackup="false"`, XML exclusions | Prevents data leakage via Android backup |
| **Location** | In-memory only (not persisted) | Sun calculation only, never stored |

## Design Decisions

### Why No DI Framework?
Gloam uses manual dependency passing (Application → MainActivity → ViewModel → Repository → DAOs). For a single-module app of this size, Koin/Hilt would add complexity without meaningful benefit. If the app grows to multi-module, DI will be added.

### Why SharedPreferences for SQLCipher Passphrase?
The passphrase is randomly generated on first run and stored in SharedPreferences. While this means the passphrase is accessible on a rooted device, the SQLCipher encryption still protects against:
- Accidental data exposure (file sharing, backups)
- Forensic analysis of the raw database file
- Cross-device data leakage (passphrase is device-specific)

For higher security, Android Keystore integration is planned (Phase 6).

### Why Manual Location (Default Sydney)?
The app defaults to Sydney coordinates (-33.87, 151.21) if location permission is denied. This ensures the app is fully functional without location access — just with approximate sun times.

### Why No WorkManager?
WorkManager is declared as a dependency (for future use) and its default initializer is removed from the manifest. Currently, sunrise/sunset reminders use `AlarmManager.setRepeating()` directly. WorkManager will be adopted when background task complexity increases.

---

*Gloam: the sun sets, but your thoughts remain.*
