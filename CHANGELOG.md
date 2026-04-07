# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [1.0.0] - 2026-04-07

### Added
- Solar-timed journaling with NOAA calculator (real sunrise/sunset by GPS location)
- CBT prompt system: 18 prompts across 6 therapeutic categories, random selection per session
- Mood tracking: 5-point emoji scale, automatic daily averages, year-in-pixels heatmap
- SQLCipher-encrypted Room database (random 32-char passphrase, device-bound)
- SHA-256 PIN lock system (setup + verify, never stored in plaintext)
- 4 Compose screens: Home (solar header + journal), Calendar (stats + heatmap), Entries, Settings
- Location-based sunrise/sunset notification reminders with boot recovery
- JSON export of all journal data
- Gradle version catalog (`libs.versions.toml`)
- 22 unit tests (SunCalculator × 12, Converters × 10)
- GitHub Actions CI pipeline
- Full documentation: README, USAGE, ARCHITECTURE, ROADMAP, SECURITY, CONTRIBUTING, CODE_OF_CONDUCT, RELEASE
