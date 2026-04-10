<div align="center">

# ⬡ G L O A M
### *Solar-Timed Journaling for Android & Desktop.*

[![Status](https://img.shields.io/badge/Status-Polished-50fa7b?style=for-the-badge)]()
[![Platform](https://img.shields.io/badge/Platform-Android_%7C_Desktop-4c566a?style=for-the-badge&logo=android)]()
[![License](https://img.shields.io/badge/license-MIT-f1fa8c?style=for-the-badge)](LICENSE)

**[📲 Download APK](https://github.com/earnerbaymalay/Gloam/releases/latest/download/gloam.apk)**

</div>

---

## Functions

Gloam uses a NOAA solar calculator to determine precise sunrise and sunset times for your location. The interface transitions through morning, daytime, sunset, and nighttime themes. Each transition introduces contextually timed CBT prompts selected from a pool of 18 prompts across 6 categories.

You select your mood on a 5-point scale. Gloam tracks daily averages and displays a year-in-pixels heatmap. All data is encrypted with SQLCipher and secured with a PIN.

---

## Features

-   **Four screens:** Home (journal with mood selector and CBT prompts), Calendar (year-in-pixels heatmap, mood statistics), Entries (chronological list), Settings (PIN lock, JSON export).
-   **CBT prompts:** 18 prompts across 6 categories. Each session provides 3 random prompts to prevent repetition.
-   **Mood tracking:** A 5-point scale with emoji, daily averages, a year-in-pixels heatmap, and consistency percentage.
-   **Solar themes:** Uses a NOAA calculator for your location to automatically shift themes throughout the day.
-   **Security:** SQLCipher-encrypted database, SHA-256 PIN lock, and no cloud dependency.

---

## Build and run

Requires Android Studio Arctic Fox or later, JDK 17, and Android SDK 26-33.

```bash
git clone https://github.com/earnerbaymalay/Gloam.git
cd Gloam
```

Open the project in Android Studio, sync Gradle, then run. Minimum SDK is 26, target SDK is 33.

### Desktop (Compose Multiplatform)

```bash
./gradlew composeApp:assemble         # Builds for the current OS
./gradlew composeApp:packageDmg       # Packages for macOS
./gradlew composeApp:packageMsi       # Packages for Windows
./gradlew composeApp:packageDeb       # Packages for Linux
```

---

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