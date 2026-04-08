# Gloam

Solar-timed journaling with CBT prompts and mood tracking. Themes shift from light to dark based on your local sunrise and sunset.

[![Status](https://img.shields.io/badge/Status-Polished-50fa7b?style=for-the-badge)]()
[![Platform](https://img.shields.io/badge/Platform-Android_%7C_Desktop-4c566a?style=for-the-badge&logo=android)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin)]()
[![License](https://img.shields.io/badge/license-MIT-f1fa8c?style=for-the-badge)](LICENSE)

[Quick Start](#build-and-run) · [Usage Guide](docs/USAGE.md) · [Architecture](docs/ARCHITECTURE.md) · [Roadmap](docs/ROADMAP.md)

---

## What It Does

Gloam uses a NOAA solar calculator to determine exact sunrise and sunset times for your location. The interface transitions through warm morning tones, bright daytime themes, golden sunset colors, and deep dark night themes. Each transition brings contextually timed CBT prompts from a pool of 18 prompts across 6 categories.

You select your mood on a 5-point scale. Gloam tracks daily averages and displays a year-in-pixels heatmap. All data is encrypted with SQLCipher and locked behind a PIN.

## Build and Run

Android Studio Arctic Fox or later, JDK 17, Android SDK 26-33.

```bash
git clone https://github.com/earnerbaymalay/Gloam.git
cd Gloam
```

Open in Android Studio, sync Gradle, run. Min SDK 26, target SDK 33.

### Desktop (Compose Multiplatform)

```bash
./gradlew composeApp:assemble          # Build for current OS
./gradlew composeApp:packageDmg        # macOS
./gradlew composeApp:packageMsi        # Windows
./gradlew composeApp:packageDeb        # Linux
```

## Features

**Four screens:** Home (journal with mood selector and CBT prompts), Calendar (year-in-pixels heatmap, mood statistics), Entries (chronological list), Settings (PIN lock, JSON export).

**CBT prompts:** 18 prompts across 6 categories. Each session pulls 3 random prompts so you never get the same combination twice in a row.

**Mood tracking:** 5-point scale with emoji, daily averages, year-in-pixels heatmap, consistency percentage.

**Solar themes:** NOAA calculator for your location. Themes shift automatically throughout the day.

**Security:** SQLCipher-encrypted database, SHA-256 PIN lock, zero cloud dependency.

## Roadmap

| Phase | Status |
|-------|--------|
| Core journal, CBT prompts, mood tracking | Done |
| Solar theme with NOAA calculator | Done |
| Year-in-pixels, mood stats, calendar view | Done |
| PIN lock, encrypted database | Done |
| Sunrise/sunset notifications | Done |
| Compose Multiplatform desktop | Done |
| PWA with IndexedDB | Planned |
| E2EE sync across devices | Planned |

---

[MIT License](LICENSE)
