# 🗺️ Roadmap

## Phase 1: Core Journal ✅ Complete
- [x] Journal entry creation (sunrise/sunset types)
- [x] CBT prompt system (18 prompts, 6 categories)
- [x] Mood tracking (5-point scale)
- [x] SQLCipher-encrypted Room database
- [x] Random prompt selection per session
- [x] Auto mood record calculation

## Phase 2: Solar Theme ✅ Complete
- [x] NOAA solar calculator (sunrise, sunset, solar noon)
- [x] Location-based sun times (GPS → lat/lng)
- [x] Fallback coordinates (Sydney default)
- [x] Theme interpolation (dark ↔ light based on daylight progress)
- [x] Light and dark color palettes

## Phase 3: Visualization ✅ Complete
- [x] Year-in-pixels heatmap
- [x] Month calendar with mood coloring
- [x] Mood statistics (average, consistency, distribution)
- [x] Entries list (chronological, grouped by date)
- [x] Entry detail view (full entry with all responses)
- [x] Entry editing and deletion

## Phase 4: Security ✅ Complete
- [x] SHA-256 PIN lock system
- [x] PIN setup flow (enter + confirm)
- [x] SQLCipher database encryption
- [x] Backup exclusion (Android backup, cloud, device transfer)
- [x] `allowBackup="false"` in manifest

## Phase 5: Notifications ✅ Complete
- [x] Sunrise notification reminders
- [x] Sunset notification reminders
- [x] Boot receiver (reschedule alarms on device restart)
- [x] Notification channels
- [x] Exact alarm permissions

## Phase 6: Polish 🔄 In Progress
- [ ] Biometric unlock (BiometricPrompt API)
- [ ] Dawn/dusk transition themes (currently only light/dark)
- [ ] Gradle version catalog (`libs.versions.toml`)
- [ ] Android Keystore for SQLCipher passphrase (upgrade from SharedPreferences)
- [ ] Southern hemisphere solar validation

## Phase 7: Export & Import 🔮 Planned
- [x] JSON export of all data
- [ ] JSON import (restore from backup)
- [ ] Encrypted cloud backup (user-controlled encryption key)
- [ ] Cross-device migration

## Phase 8: Widgets & Integrations 🔮 Planned
- [ ] Home screen widget (today's mood + quick journal shortcut)
- [ ] Wear OS support (quick mood log from watch)
- [ ] Health Connect integration (mood data as health metric)
- [ ] Obsidian plugin (sync journal to Obsidian vault)

## Phase 9: Intelligence 🔮 Research
- [ ] Mood trend analysis ("your mood improves on weekdays")
- [ ] Prompt effectiveness tracking ("you respond most to gratitude prompts")
- [ ] Weekly/monthly insight summaries
- [ ] Pattern detection (CBT distortion frequency, mood triggers)

---

## How You Shape This Roadmap

Gloam is community-driven. Want a feature moved up? [Open a discussion](https://github.com/earnerbaymalay/gloam/discussions) — popular requests get prioritized.

---

*Every phase is a commitment to making Gloam the most mindful journaling experience on Android.*
