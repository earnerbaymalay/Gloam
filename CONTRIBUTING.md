# Contributing to Gloam

Thank you for considering a contribution. **Wellness tools should be transparent**, and every improvement makes Gloam better for everyone.

## Getting Started

1. Fork the repository
2. Clone: `git clone https://github.com/YOUR_USERNAME/gloam.git`
3. Open in Android Studio
4. Sync Gradle and ensure the project builds
5. Create a branch: `git checkout -b feature/your-feature-name`

## Coding Standards

### Kotlin
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Compose best practices — composables should be stateless where possible
- State flows should be exposed via `StateFlow<T>`, collected via `collectAsState()`
- Room entities should never be mutable outside the DAO layer

### Architecture
- New data code goes in `data/`
- New UI code goes in `ui/`
- New utilities go in `util/`
- ViewModels are the bridge — no business logic in composables

### Commits
- Use [Conventional Commits](https://www.conventionalcommits.org/):
  - `feat: add biometric unlock support`
  - `fix: correct solar noon calculation for southern hemisphere`
  - `docs: update architecture documentation`
  - `refactor: extract mood stats into separate composable`

## Testing

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# Lint
./gradlew lintDebug
```

## Pull Request Process

1. Ensure all tests pass
2. Run lint: `./gradlew lintDebug`
3. Update documentation if your change affects user-facing features
4. Request review from a maintainer

## Where to Start

Good first contributions:
- 🎨 Refine the Compose theme and animations
- 🧪 Add unit tests (SunCalculator, Converters, Repository)
- 📝 Improve documentation
- 🐛 Fix UI bugs or edge cases
- 🧠 Suggest better CBT prompts

For experienced developers:
- 🔐 Implement biometric unlock (BiometricPrompt API)
- 🌅 Add dawn/dusk transition themes
- 📦 Migrate to Gradle version catalog
- 🔔 Improve notification scheduling reliability

---

*Gloam exists because everyone deserves a private place to think.*
