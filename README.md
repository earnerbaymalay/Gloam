<div align="center">

# 🌗 G L O A M
### *Journal with the Sun.*

<p align="center">
  <img src="https://raw.githubusercontent.com/earnerbaymalay/sideload/main/assets/gloam-hero.svg" alt="Gloam Solar Journal" width="700"/>
</p>

[![Status](https://img.shields.io/badge/Status-Polished-50fa7b?style=for-the-badge)](https://github.com/earnerbaymalay/Gloam)
[![Platform](https://img.shields.io/badge/Platform-Android_%7C_Desktop-4c566a?style=for-the-badge&logo=android)](https://github.com/earnerbaymalay/Gloam)
[![Language](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-f1fa8c?style=for-the-badge)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25_Local-bd93f9?style=for-the-badge)](#-why-does-this-matter)

[**⚡ Quick Start**](#-build--run-in-30-seconds) · [**📖 Usage Guide**](docs/USAGE.md) · [**🏗️ Architecture**](docs/ARCHITECTURE.md) · [**🗺️ Roadmap**](docs/ROADMAP.md)

> 📲 **Install on any device — Android, Mac, Windows, or Linux — from one place:** [**sideload**](https://earnerbaymalay.github.io/sideload/)

</div>

---

## 🧬 The Mission

> *Picture this:* You wake up. Your phone knows it's sunrise. Your journal app's theme has already shifted to warm, soft tones. It asks you three questions — not generic ones, but prompts designed by cognitive behavioral therapists: *"What emotions am I carrying right now?" "What would make today feel meaningful?" "What thought pattern could I reframe today?"* You answer in 30 seconds. The sun sets. Your app shifts again to deep dark tones. It asks: *"What challenged me most today?" "What three things am I grateful for?"* You answer. Your mood for the day is automatically calculated and colored on a year-long heatmap.

That's Gloam.

It's a **solar-timed journaling app** for Android and Desktop that automatically transitions between light and dark themes based on your local sunrise and sunset — because your mood should flow with the day.

**No accounts. No cloud sync. No ads. No tracking.** Your journal lives on your device, encrypted at rest with SQLCipher, accessible only behind a PIN you set.

---

## 🔥 Why Does This Matter?

Most journaling apps are just text boxes in a cloud database. Gloam is different:

| | **Typical Journal Apps** | **Gloam** |
|---|---|---|
| 🎨 Theme | Manual toggle or fixed dark mode | **Automatic — synced to your local sunrise/sunset** |
| 🧠 Prompts | None or generic | **18 CBT prompts across 6 categories, contextually timed** |
| 📊 Mood tracking | Optional add-on | **Core feature — automatic averages, year-in-pixels visualization** |
| 🔒 Privacy | Stored on someone's server | **SQLCipher-encrypted, PIN-locked, 100% local** |
| 💰 Business model | Free → premium paywall | **Free, open source, no paywall** |
| 📡 Internet | Required for sync | **Never needed** |

### The Sun Dictates the Mood

Gloam uses a **NOAA solar calculator** to determine exact sunrise/sunset times for your GPS location. As the day progresses:

- 🌅 **Sunrise** — warm, soft theme. Prompts focus on intention and emotional check-in
- ☀️ **Daytime** — bright, clear theme. Journal freely
- 🌇 **Sunset** — golden, warm transition. Prompts shift to reflection and gratitude
- 🌙 **Night** — deep dark theme. Quiet, contemplative

Your phone's wallpaper changes with the sky outside.

---

## 👋 Who Should Use This?

> **Anyone who wants to journal — without their data becoming someone else's product.**

| If you are… | Gloam gives you |
|---|---|
| 🟢 **Mindfulness practitioner** | CBT-timed prompts that adapt to sunrise reflection and sunset gratitude |
| 🟡 **Mood tracker** | 5-point mood scale with emoji, automatic daily averages, year-in-pixels heatmap |
| 🔴 **Privacy-conscious user** | SQLCipher-encrypted database, SHA-256 PIN lock, zero cloud dependency |
| 💻 **Android developer** | Clean architecture with Room + SQLCipher, Compose Multiplatform UI, solar algorithm |

---

## 🚀 Build & Run in 30 Seconds

**Prerequisites:** Android Studio (Arctic Fox+), JDK 17, Android SDK 26-33.

```bash
# 1. Clone
git clone https://github.com/earnerbaymalay/Gloam.git
cd Gloam

# 2. Open in Android Studio
# File → Open → select the Gloam directory

# 3. Sync Gradle and run
# Min SDK: 26 | Target SDK: 33 | Java: 17
```

That's it. The app is **fully functional on first launch** — no setup wizard, no account creation.

### Desktop (Compose Multiplatform)

Gloam now supports **native desktop builds** via Compose Multiplatform:

```bash
# Build for your current OS
./gradlew composeApp:assemble

# Build distributable packages
./gradlew composeApp:packageDmg   # macOS
./gradlew composeApp:packageMsi   # Windows
./gradlew composeApp:packageDeb   # Linux
```

---

## 🧠 What You Get

### Four Screens

| Screen | What It Does |
|---|---|
| 🏠 **Home** | Solar times header (sunrise → now → sunset), journal entry with mood selector + 3 CBT prompts, or view today's completed entry |
| 📅 **Calendar** | Year-in-pixels heatmap, mood statistics (average, consistency, distribution), month-by-month navigation |
| 📝 **Entries** | Chronological list of all journal entries, grouped by date, tap to view/edit/delete |
| ⚙️ **Settings** | PIN lock toggle, JSON export of all data, about dialog |

### CBT Prompt System

18 prompts across 6 therapeutic categories:

| Category | Sunrise Prompts | Sunset Prompts |
|---|---|---|
| Emotional Check-in | "What emotions am I carrying right now?" | — |
| Intention | "What would make today feel meaningful?" | — |
| Cognitive Reframing | "What thought pattern could I reframe today?" | "Did I catch any cognitive distortions today?" |
| Reflection | — | "What challenged me most today?" |
| Gratitude | — | "What three things am I grateful for?" |
| Closure | — | "How can I release today's tension before sleep?" |

Each journaling session pulls 3 random prompts (one per relevant category). You never get the same combination twice in a row.

### Mood Tracking

- 5-point scale: 😞 😐 🙂 😄 😊
- Automatic daily average (sunrise + sunset entries)
- Year-in-pixels heatmap (12 × 31 grid)
- Mood statistics: days logged, average mood, consistency percentage
- Color-coded calendar visualization

---

## 🏗️ Architecture at a Glance

```
Gloam/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/         ← Shared code (platform-agnostic)
│   │   │   ├── data/
│   │   │   │   ├── model/       ← Models (no Room annotations)
│   │   │   │   ├── db/          ← Database interfaces
│   │   │   │   └── repository/  ← GloamRepository (pure logic)
│   │   │   ├── ui/
│   │   │   │   ├── theme/       ← Color, Typography, Theme
│   │   │   │   ├── components/  ← PinLock, Calendar, MoodSelector
│   │   │   │   └── screens/     ← Home, Calendar, Entries, Settings
│   │   │   ├── util/            ← SunCalculator (NOAA algorithm)
│   │   │   └── viewmodel/       ← GloamViewModel
│   │   │
│   │   ├── androidMain/        ← Android-specific implementations
│   │   │   └── data/db/         ← Room + SQLCipher database
│   │   │
│   │   └── desktopMain/        ← Desktop-specific implementations
│   │       └── data/db/         ← SQLite JDBC database
│   │
│   └── build.gradle.kts        ← Compose Multiplatform config
│
└── design/gloam-ui.html        ← Full HTML/CSS/JS interactive UI prototype
```

### Data Flow

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

---

## 📲 Available Everywhere

| Platform | Format | Install |
|---|---|---|
| 📱 **Android** | APK | Build from source or [download from Sideload](https://earnerbaymalay.github.io/sideload/) |
| 🖥️ **macOS** | DMG | `./gradlew composeApp:packageDmg` |
| 🖥️ **Windows** | MSI | `./gradlew composeApp:packageMsi` |
| 🖥️ **Linux** | deb | `./gradlew composeApp:packageDeb` |

### 📲 All Apps, One Place

All our apps are available through **[Sideload](https://earnerbaymalay.github.io/sideload/)** — our central distribution hub for local-first apps.

---

## 🗺️ Roadmap

| Phase | Status | What's Done |
|---|---|---|
| **Phase 1: Core Journal** | ✅ Complete | Entries, CBT prompts, mood tracking, SQLCipher DB |
| **Phase 2: Solar Theme** | ✅ Complete | NOAA calculator, theme interpolation, location-based sun times |
| **Phase 3: Visualization** | ✅ Complete | Year-in-pixels, mood stats, calendar view, entries list |
| **Phase 4: Security** | ✅ Complete | SHA-256 PIN lock, encrypted database, backup exclusion |
| **Phase 5: Notifications** | ✅ Complete | Sunrise/sunset reminders, boot recovery, notification channels |
| **Phase 6: Cross-Platform** | ✅ Complete | Compose Multiplatform — Android + Desktop (macOS/Windows/Linux) |
| **Phase 7: PWA** | 🔮 Planned | Browser-based installable app with IndexedDB |
| **Phase 8: Sync** | 🔮 Planned | E2EE sync across devices (encrypted cloud backup) |

See [docs/ROADMAP.md](docs/ROADMAP.md) for full details.

---

## 📚 Documentation

| Document | What It Covers |
|---|---|
| [**Usage Guide**](docs/USAGE.md) | First launch, journaling, mood tracking, calendar, PIN setup, export |
| [**Architecture**](docs/ARCHITECTURE.md) | Module design, data flow, solar algorithm, security model |
| [**Roadmap**](docs/ROADMAP.md) | Development phases, what's done, what's next |
| [**Contributing**](CONTRIBUTING.md) | How to contribute, coding standards |
| [**Security Policy**](SECURITY.md) | Vulnerability disclosure |

---

## 🤝 Contributing

Gloam is open source because **wellness tools should be auditable**. Every line of code is open for inspection.

- 🐛 Found a bug? [Open an issue](https://github.com/earnerbaymalay/Gloam/issues)
- 💡 Want to help? Read [CONTRIBUTING.md](CONTRIBUTING.md)
- 🧠 Know CBT? Suggest better prompts
- 🎨 Love design? Refine the glassmorphic aesthetic

---

## 📜 License

[MIT License](LICENSE) — Use it. Modify it. Share it.

**Wellness tools should be open. Period.**

---

<div align="center">

### 🌗 *The sun rises. Your journal follows.*

**⭐ If mindful journaling matters to you, star this repo — it helps more people discover it.**

</div>
