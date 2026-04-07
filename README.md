<div align="center">

# 🌗 G L O A M
### *Journal with the Sun.*

[![Status](https://img.shields.io/badge/Status-Polished-50fa7b?style=for-the-badge)]()
[![Platform](https://img.shields.io/badge/Platform-Android_26%2B-4c566a?style=for-the-badge&logo=android)]()
[![Language](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin)]()
[![License](https://img.shields.io/badge/License-MIT-f1fa8c?style=for-the-badge)]()
[![Privacy](https://img.shields.io/badge/Privacy-100%25_Local-bd93f9?style=for-the-badge)]()

[**⚡ Quick Start**](#-build--run-in-30-seconds) • [**📖 Usage Guide**](docs/USAGE.md) • [**🏗️ Architecture**](docs/ARCHITECTURE.md) • [**🗺️ Roadmap**](docs/ROADMAP.md)

---

### 🤔 What is this?

Gloam is a **solar-timed journaling app** for Android. It automatically transitions between light and dark themes based on your local sunrise and sunset — because your mood should flow with the day.

It combines **Cognitive Behavioral Therapy (CBT) prompts**, **mood tracking**, and a **calendar-based visualization** engine into one private, offline-first experience.

> **No accounts. No cloud sync. No ads. No tracking.** Your journal lives on your device, encrypted at rest with SQLCipher, accessible only behind a PIN you set.

### 🔥 Why does this matter?

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

### 👋 Who should use this?

> **Anyone who wants to journal — without their data becoming someone else's product.**

| If you are... | Gloam gives you |
|---|---|
| 🟢 **Mindfulness practitioner** | CBT-timed prompts that adapt to sunrise reflection and sunset gratitude |
| 🟡 **Mood tracker** | 5-point mood scale with emoji, automatic daily averages, year-in-pixels heatmap |
| 🔴 **Privacy-conscious user** | SQLCipher-encrypted database, SHA-256 PIN lock, zero cloud dependency |
| 💻 **Android developer** | Clean architecture with Room + SQLCipher, Compose UI, solar algorithm implementation |
| 🧠 **CBT student** | Built-in prompts from 6 therapeutic categories: emotional check-in, intention, cognitive reframing, reflection, gratitude, closure |

---

</div>

## 🚀 Build & Run in 30 Seconds

**Prerequisites:** Android Studio (Arctic Fox+), JDK 17, Android SDK 26-33.

```bash
# 1. Clone
git clone https://github.com/earnerbaymalay/gloam.git
cd gloam

# 2. Open in Android Studio
# File → Open → select the gloam directory

# 3. Sync Gradle and run
# Min SDK: 26 | Target SDK: 33 | Java: 17
```

That's it. The app is **fully functional on first launch** — no setup wizard, no account creation.

---

## 🧠 What You Get

### Four Screens

| Screen | What It Does |
|---|---|
| 🏠 **Home** | Solar times header (sunrise → now → sunset), journal entry with mood selector + 3 CBT prompts, or view today's completed entry |
| 📅 **Calendar** | Year-in-pixels heatmap, mood statistics (average, consistency, distribution), month-by-month navigation |
| 📝 **Entries** | Chronological list of all journal entries, grouped by date, tap to view/edit/delete |
| ⚙️ **Settings** | PIN lock toggle, JSON export of all data, about dialog |

### The Solar Theme Engine

Gloam calculates your local sunrise and sunset using the **NOAA solar algorithm** — the same mathematics used by weather services worldwide. The theme engine interpolates between light and dark color palettes based on daylight progress:

```
Night (0%) ──dawn──→ Sunrise (50%) ──day──→ Sunset (50%) ──dusk──→ Night (0%)
```

The result: your app's appearance tracks the actual sky.

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
gloam/
├── app/src/main/java/com/gloam/
│   ├── GloamApplication      ← Application class, DB singleton
│   ├── MainActivity          ← NavHost, PIN lock, JSON export, permission handling
│   │
│   ├── data/
│   │   ├── model/Models.kt   ← Room entities: JournalEntry, MoodRecord, Prompt
│   │   ├── db/
│   │   │   ├── GloamDatabase  ← SQLCipher-encrypted Room DB
│   │   │   ├── Converters.kt  ← TypeConverters (LocalDate, enums)
│   │   │   └── Daos.kt        ← 3 DAOs: JournalEntry, MoodRecord, Prompt
│   │   └── repository/
│   │       └── GloamRepository ← CRUD + mood auto-calculation + prompt randomization
│   │
│   ├── viewmodel/
│   │   └── GloamViewModel      ← All UI state flows, location, sun, CRUD
│   │
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Color.kt        ← Light/dark palettes, mood colors, transition colors
│   │   │   ├── Theme.kt        ← Solar-interpolated theme + Material3 wrapper
│   │   │   └── Type.kt         ← Full Material3 typography
│   │   ├── components/
│   │   │   ├── PinLock.kt      ← 4-digit PIN with setup + verify
│   │   │   ├── Calendar.kt     ← Year-in-pixels + month calendar
│   │   │   └── MoodSelector.kt ← 5-emoji animated mood picker
│   │   └── screens/
│   │       ├── home/HomeScreen.kt
│   │       ├── calendar/CalendarScreen.kt
│   │       ├── entries/EntriesScreen.kt + EntryDetailScreen.kt
│   │       └── settings/SettingsScreen.kt
│   │
│   └── util/
│       ├── SunCalculator.kt    ← NOAA solar algorithm
│       └── NotificationUtils.kt ← Sunrise/sunset reminders
│
└── design/gloam-ui.html        ← Full HTML/CSS/JS interactive UI prototype
```

---

## 🗺️ Roadmap

| Phase | Status | What's Done |
|---|---|---|
| **Phase 1: Core Journal** | ✅ Complete | Entries, CBT prompts, mood tracking, SQLCipher DB |
| **Phase 2: Solar Theme** | ✅ Complete | NOAA calculator, theme interpolation, location-based sun times |
| **Phase 3: Visualization** | ✅ Complete | Year-in-pixels, mood stats, calendar view, entries list |
| **Phase 4: Security** | ✅ Complete | SHA-256 PIN lock, encrypted database, backup exclusion |
| **Phase 5: Notifications** | ✅ Complete | Sunrise/sunset reminders, boot recovery, notification channels |
| **Phase 6: Polish** | 🔄 In Progress | Biometric unlock, dawn/dusk transition themes, version catalog |
| **Phase 7: Export & Import** | 🔮 Planned | JSON export (working), import, cloud backup (encrypted) |
| **Phase 8: Widgets** | 🔮 Planned | Home screen widget showing today's mood + quick journal |

See [docs/ROADMAP.md](docs/ROADMAP.md) for details.

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

- 🐛 Found a bug? [Open an issue](https://github.com/earnerbaymalay/gloam/issues)
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
