# Gloam

A minimal, iOS-styled journaling app with CBT-inspired prompts and mood tracking. Features a dynamic theme that transitions from light to dark based on your local sunrise and sunset times.

## Features

- **Solar-timed prompts**: Morning and evening reflection prompts triggered at actual sunrise/sunset
- **Dynamic theme**: UI gradually shifts from light (day) to dark (night) based on sun position
- **5-point mood tracking**: Color-coded mood scale with emoji indicators
- **Year in pixels**: 365-day calendar view showing your mood patterns
- **CBT-inspired prompts**: Emotional check-ins, gratitude prompts, and cognitive reframing questions
- **Encrypted storage**: All entries stored in SQLCipher encrypted database
- **PIN lock**: Optional 4-digit PIN protection
- **Offline-first**: No cloud sync, complete privacy

## Building

### Using GitHub Actions (Recommended)

1. Fork this repository
2. Push to `main` branch
3. GitHub Actions will build the APK automatically
4. Download from Actions artifacts or Releases

### Local Build (Termux)

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/gloam.git
cd gloam

# Make gradlew executable
chmod +x gradlew

# Build (requires JDK 17)
./gradlew assembleDebug
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`

## Development in Termux

```bash
# Install required packages
pkg install git openjdk-17

# Clone and edit
git clone https://github.com/YOUR_USERNAME/gloam.git
cd gloam

# Edit files with your preferred editor (nano, vim, etc.)
nano app/src/main/java/com/gloam/MainActivity.kt

# Commit and push to trigger build
git add .
git commit -m "Your changes"
git push
```

## Project Structure

```
gloam/
├── app/src/main/
│   ├── java/com/gloam/
│   │   ├── data/
│   │   │   ├── db/          # Room database, DAOs
│   │   │   ├── model/       # Data classes
│   │   │   └── repository/  # Data access layer
│   │   ├── ui/
│   │   │   ├── components/  # Reusable UI components
│   │   │   ├── screens/     # App screens
│   │   │   └── theme/       # Dynamic theming
│   │   ├── util/            # Utilities (sun calculator, notifications)
│   │   └── viewmodel/       # ViewModels
│   └── res/                 # Resources
├── .github/workflows/       # CI/CD
└── gradle/                  # Gradle wrapper
```

## Customization

### Adding Prompts

Edit `GloamDatabase.kt` and add to the `populateDefaultPrompts()` function:

```kotlin
Prompt(
    text = "Your custom prompt here",
    category = PromptCategory.EMOTIONAL_CHECKIN, // or other category
    entryType = EntryType.SUNRISE // or SUNSET
)
```

### Changing Colors

Edit `app/src/main/java/com/gloam/ui/theme/Color.kt`:

```kotlin
object MoodColors {
    val Struggling = Color(0xFFE74C3C)  // 1 - Red
    val Low = Color(0xFFF39C12)         // 2 - Orange  
    // ... customize as needed
}
```

## License

MIT License - feel free to modify and distribute.

## Credits

Built with:
- Jetpack Compose
- Room + SQLCipher
- Material Design 3
- NOAA Solar Calculator algorithm
