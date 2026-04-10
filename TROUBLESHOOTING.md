<div align="center">

# 🌗 G L O A M
### *Troubleshooting & Support.*

**[📖 Usage Guide](docs/USAGE.md)** · **[📲 Sideload Hub](https://earnerbaymalay.github.io/sideload/)** · **[↩ Back to README](README.md)**

</div>

---

## Installation & Build Issues

### APK won't install
- Enable "Install unknown apps" for your browser or file manager in Android settings.
- If installation fails, clear package installer cache and retry.

### Build fails from source
- Verify JDK 17 is installed and configured in Android Studio.
- Ensure Android SDK 26-34 is installed via SDK Manager.
- Run `./gradlew clean` then rebuild.
- Check that Gradle sync completes without errors.

---

## Location & Theme Issues

### Sun times are inaccurate
- Ensure location permission is granted for GPS-based calculations.
- If denied, the app defaults to Sydney, Australia.

### Theme does not change
- Theme transitions are gradual. Allow time for significant daylight changes.
- Verify that location permissions are correctly set for accurate solar calculations.

---

## PIN & Data Issues

### Forgot PIN
- The PIN is SHA-256 hashed and cannot be recovered.
- To regain access, you must uninstall and reinstall Gloam. This will result in permanent data loss.
- Alternatively, clear app data (this also results in data loss).

### No prompts showing
- Verify that default prompts were seeded during installation. A reinstall might be necessary if they are missing.

### JSON export fails
- Ensure storage permission is granted to allow the app to write files.

---

## Notification Issues

### Notifications do not fire
- Check Android notification permissions for Gloam.
- Verify that exact alarm permission is granted.
- Ensure the app is not battery optimized in a way that prevents background processes.

---

## Need More Help?

- **[📖 Read the Usage Guide](docs/USAGE.md)** for detailed instructions on journaling, mood tracking, and PIN setup.
- **[📲 Visit Sideload Hub](https://earnerbaymalay.github.io/sideload/)** for alternative installation methods and PWA access.
- **[🐛 Report a Bug](https://github.com/earnerbaymalay/Gloam/issues)** on GitHub with device details, Android version, and steps to reproduce.

---

[MIT License](LICENSE)
