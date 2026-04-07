# 🌗 Gloam Usage Guide
### *Your Complete Guide to Solar-Timed Journaling*

---

## First Launch

### Installation
```bash
git clone https://github.com/earnerbaymalay/gloam.git
cd gloam
# Open in Android Studio → Run
```

Or download the latest APK from [GitHub Releases](https://github.com/earnerbaymalay/gloam/releases).

### First Run
1. **Location permission** — Gloam asks for location to calculate your local sunrise/sunset. Grant it for accurate sun times, or deny it and it defaults to Sydney, Australia.
2. **That's it.** No account, no setup wizard, no onboarding screens. You're at the Home screen.

---

## Journaling

### Writing an Entry

1. **Open the Home screen** — you'll see today's sun times and current time
2. **The mood selector** — tap an emoji from 😞 😐 🙂 😄 😊
3. **Three CBT prompts appear** — they're randomly selected based on whether it's sunrise or sunset
4. **Write your responses** — tap each prompt field and type
5. **Tap Save** — your entry is encrypted and stored

### Sunrise vs Sunset Entries

Gloam automatically determines which type of entry to show based on the solar position:

- **Before solar noon** → Sunrise entry (prompts about intention, emotional check-in, cognitive reframing)
- **After solar noon** → Sunset entry (prompts about reflection, gratitude, closure)

You can write both a sunrise and sunset entry each day.

### Editing an Entry

1. Go to **Entries** screen (bottom nav)
2. Tap any entry
3. Tap **Edit**
4. Modify mood or responses
5. Tap **Save**

### Deleting an Entry

1. Open the entry detail screen
2. Tap **Delete** (trash icon in top bar)
3. Confirm deletion

---

## Mood Tracking

### The Mood Scale

| Emoji | Label | Score |
|---|---|---|
| 😞 | Rough | 1 |
| 😐 | Meh | 2 |
| 🙂 | Okay | 3 |
| 😄 | Good | 4 |
| 😊 | Great | 5 |

### Daily Average

When you write both a sunrise and sunset entry, Gloam automatically calculates your daily average mood. This appears in:
- **Calendar screen** — mood statistics section
- **Year-in-pixels** — each day colored by average mood
- **Entries screen** — both entries show their individual moods

### Year in Pixels

The calendar screen shows a 12 × 31 grid where each cell represents a day:
- **Green** = great mood (5)
- **Teal** = good (4)
- **Gray** = okay (3)
- **Orange** = meh (2)
- **Red** = rough (1)
- **Empty** = no entry that day

Tap any cell to see that day's entries.

---

## PIN Lock

### Setting a PIN

1. Go to **Settings** screen
2. Toggle **PIN Lock** on
3. Enter a 4-digit PIN
4. Confirm the same PIN
5. Done — next time you open Gloam, you'll need the PIN

### Changing Your PIN

1. Go to **Settings**
2. Toggle PIN off, then on again
3. Enter your new PIN

### Forgot Your PIN

Unfortunately, the PIN is SHA-256 hashed and cannot be recovered. To reset:
1. Uninstall and reinstall Gloam
2. **Warning:** This deletes all your journal data

---

## Exporting Your Data

1. Go to **Settings**
2. Tap **Export Journal (JSON)**
3. The JSON file contains all your entries, mood records, and prompts
4. Share it via Android's share sheet (email, Drive, etc.)

**Note:** Exported data is plaintext. Keep it secure.

---

## Notifications

Gloam can remind you to journal at sunrise and sunset:

1. Go to **Settings** → enable notifications (via Android notification permissions)
2. Gloam schedules alarms based on your calculated sun times
3. On boot, alarms are automatically rescheduled
4. Tap a notification → opens Gloam to the appropriate entry type

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Sun times are wrong | Grant location permission for accurate GPS-based calculation |
| App asks for PIN I forgot | Uninstall/reinstall (data will be lost) — or clear app data |
| No prompts showing | Check that default prompts were seeded (reinstall if needed) |
| JSON export fails | Ensure storage permission is granted |
| Notifications don't fire | Check Android notification permissions and exact alarm permission |
| Theme doesn't change | Theme transitions are gradual — you may need to wait for significant daylight change |

---

## Privacy

- **No internet required** — Gloam works 100% offline
- **No accounts** — nothing to register, no password to forget
- **No cloud** — your journal never leaves your device (except when you export)
- **No analytics** — no tracking, no crash reporting, no telemetry
- **Encrypted at rest** — SQLCipher protects your database
- **Backup excluded** — Android backup, cloud backup, and device transfer all exclude Gloam data

---

<div align="center">

*The sun rises. Your journal follows.*

</div>
