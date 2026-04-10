<div align="center">

# ⬡ G L O A M
### *Usage Guide & Solar-Timed Journaling.*

</div>

---

## First launch

### Installation

```bash
git clone https://github.com/earnerbaymalay/gloam.git
cd gloam
# Open in Android Studio → Run
```

Alternatively, download the latest APK from [GitHub Releases](https://github.com/earnerbaymalay/gloam/releases).

### Initial setup

1.  **Location permission:** Gloam requests location access to accurately calculate local sunrise and sunset times. Granting permission ensures precise sun times; otherwise, it defaults to Sydney, Australia.
2.  **Ready to use:** No account registration, setup wizard, or onboarding screens are required. You will be directed to the Home screen immediately.

---

## Journaling

### Writing an entry

1.  **Home screen:** Navigate to the Home screen, where today's sun times and the current time are displayed.
2.  **Mood selector:** Tap an emoji from the scale: 😞 😐 🙂 😄 😊.
3.  **CBT prompts:** Three prompts appear, selected randomly based on the current solar position (sunrise or sunset).
4.  **Responses:** Tap each prompt field to enter your thoughts.
5.  **Save:** Tap 'Save' to encrypt and store your entry.

### Sunrise vs. sunset entries

Gloam automatically adjusts the entry prompts based on the solar position:

-   **Before solar noon:** Sunrise entries focus on intention, emotional check-ins, and cognitive reframing.
-   **After solar noon:** Sunset entries focus on reflection, gratitude, and closure.

You can create both a sunrise and a sunset entry each day.

### Editing an entry

1.  Go to the 'Entries' screen (via the bottom navigation bar).
2.  Tap on the entry you wish to modify.
3.  Tap 'Edit'.
4.  Adjust your mood selection or responses.
5.  Tap 'Save'.

### Deleting an entry

1.  Open the entry detail screen.
2.  Tap the 'Delete' icon (trash icon in the top bar).
3.  Confirm the deletion.

---

## Mood tracking

### The mood scale

| Emoji | Label | Score |
|-------|-------|-------|
| 😞     | Rough | 1     |
| 😐     | Meh   | 2     |
| 🙂     | Okay  | 3     |
| 😄     | Good  | 4     |
| 😊     | Great | 5     |

### Daily average

When you complete both a sunrise and a sunset entry, Gloam automatically calculates your daily average mood. This average is displayed in:

-   The 'Calendar' screen (mood statistics section).
-   The 'Year-in-pixels' heatmap, where each day is colored by its average mood.
-   The 'Entries' screen, showing individual moods for each entry.

### Year in pixels

The 'Calendar' screen presents a 12 × 31 grid, with each cell representing a day:

-   **Green:** Excellent mood (5)
-   **Teal:** Good mood (4)
-   **Gray:** Okay mood (3)
-   **Orange:** Meh mood (2)
-   **Red:** Rough mood (1)
-   **Empty:** No entry for that day

Tap any cell to view entries for that specific day.

---

## PIN lock

### Setting a PIN

1.  Go to the 'Settings' screen.
2.  Toggle 'PIN Lock' on.
3.  Enter a 4-digit PIN.
4.  Confirm the PIN.
5.  The PIN lock is active; you will need to enter it the next time you open Gloam.

### Changing your PIN

1.  Go to 'Settings'.
2.  Toggle 'PIN Lock' off, then on again.
3.  Enter your new PIN.

### Forgot your PIN

The PIN is SHA-256 hashed and cannot be recovered. To reset:

1.  Uninstall and reinstall Gloam.
2.  **Warning:** This action will permanently delete all your journal data.

---

## Exporting your data

1.  Go to 'Settings'.
2.  Tap 'Export Journal (JSON)'.
3.  The JSON file contains all your entries, mood records, and prompts.
4.  Share the file via Android's share sheet (e.g., email, Drive).

**Note:** Exported data is in plaintext; ensure it is stored securely.

---

## Notifications

Gloam can provide reminders to journal at sunrise and sunset:

1.  Go to 'Settings' and enable notifications (requires Android notification permissions).
2.  Gloam schedules alarms based on your calculated sun times.
3.  Alarms are automatically rescheduled upon device boot.
4.  Tapping a notification opens Gloam to the appropriate entry type.

---

## Troubleshooting

See the separate `TROUBLESHOOTING.md` for common issues and solutions.

---

## Privacy

-   **Offline operation:** Gloam functions entirely offline; no internet connection is required.
-   **No accounts:** No registration or passwords needed.
-   **No cloud storage:** Your journal data remains exclusively on your device, unless you choose to export it.
-   **No analytics:** No tracking, crash reporting, or telemetry data is collected.
-   **Encrypted data:** SQLCipher protects your database at rest.
-   **Backup exclusion:** Gloam data is excluded from Android backup, cloud backup, and device transfer processes.

---

[MIT License](LICENSE)
