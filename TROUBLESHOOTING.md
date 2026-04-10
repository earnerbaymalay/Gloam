<div align="center">

# ⬡ G L O A M
### *Troubleshooting & Support.*

</div>

---

## Location and theme issues

### Sun times are inaccurate
- Ensure location permission is granted for GPS-based calculations.
- If denied, the app defaults to Sydney, Australia.

### Theme does not change
- Theme transitions are gradual. Allow time for significant daylight changes.
- Verify that location permissions are correctly set for accurate solar calculations.

---

## PIN and data issues

### Forgot PIN
- The PIN is SHA-256 hashed and cannot be recovered.
- To regain access, you must uninstall and reinstall Gloam. This will result in permanent data loss.
- Alternatively, clear app data (this also results in data loss).

### No prompts showing
- Verify that default prompts were seeded during installation. A reinstall might be necessary if they are missing.

### JSON export fails
- Ensure storage permission is granted to allow the app to write files.

---

## Notification issues

### Notifications do not fire
- Check Android notification permissions for Gloam.
- Verify that exact alarm permission is granted.
- Ensure the app is not battery optimized in a way that prevents background processes.

---

[MIT License](LICENSE)
