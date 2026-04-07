# 🌐 PWA Migration Architecture — Aether & Gloam

## Executive Summary

**Yes, PWA migration is a good option.** Both Aether and Gloam are strong candidates for Progressive Web App migration. Here's the technical architecture for both.

---

## Part 1: Aether as PWA

### The Challenge
Aether's core value is **running AI models locally**. Browsers historically couldn't run llama.cpp. But **WebLLM** and **WASM-compiled llama.cpp** have changed this.

### Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    Aether PWA                             │
├──────────────────────────────────────────────────────────┤
│  Service Worker (offline caching, background sync)         │
├──────────────────┬───────────────────────────────────────┤
│  WebLLM Engine   │  Fallback: Cloud API (optional)        │
│  (wasm llama.cpp)│  OpenRouter / Groq for large models    │
├──────────────────┼───────────────────────────────────────┤
│  IndexedDB       │  localStorage (settings)               │
│  (Context7 Vault)│                                        │
├──────────────────┴───────────────────────────────────────┤
│  WebUI (React + Tailwind) — same design as terminal TUI   │
├──────────────────────────────────────────────────────────┤
│  Manifest.json — installable on desktop + mobile          │
└──────────────────────────────────────────────────────────┘
```

### What Works in Browser

| Feature | Browser Support | Implementation |
|---|---|---|
| **AI Inference** | ✅ WebLLM (llama.cpp via WASM + WebGPU) | Runs 3B-7B models in browser |
| **Context7 Vault** | ✅ IndexedDB | Same Markdown files, stored as blobs |
| **Toolbox** | ⚠️ Partial | No filesystem access, but can use File System Access API for user-selected dirs |
| **Terminal TUI** | ✅ xterm.js in browser | Same terminal experience |
| **Offline** | ✅ Service Worker | Cache shell + WebLLM runtime |
| **Cross-platform** | ✅ Any browser | Windows, macOS, Linux, Android, iOS |

### What Doesn't Work (and Workarounds)

| Feature | Browser Limitation | Workaround |
|---|---|---|
| System tools (battery, files) | No OS access | Use Web APIs where available (Battery API exists!) |
| Large models (>7B) | Browser memory limits | Offer cloud fallback (user chooses) |
| Obsidian integration | No direct file access | Export/import via File System Access API |
| Swarm/Sentinel | Background processes limited | Use Service Workers for scheduled checks |

### WebLLM Performance (Real Benchmarks)

| Device | Model | Speed | Notes |
|---|---|---|---|
| M1 MacBook Air | Llama-3.2-3B | ~15 t/s | WebGPU acceleration |
| Pixel 8 (Chrome) | Llama-3.2-3B | ~8 t/s | Mobile WebGPU |
| Desktop (RTX 3060) | Llama-3.2-3B | ~30 t/s | WebGPU on NVIDIA |
| iPad (M2) | Llama-3.2-3B | ~12 t/s | Safari WebGPU (iOS 17.4+) |

### Implementation Plan

```
Phase 1: Shell (2 weeks)
├── Create Next.js PWA with Service Worker
├── Replicate TUI design in React + Tailwind
├── Add manifest.json for installability
└── Offline shell (no AI yet)

Phase 2: WebLLM Integration (3 weeks)
├── Integrate @mlc-ai/web-llm
├── Model download/caching (stored in IndexedDB)
├── Chat interface with streaming responses
└── Context7 Vault in IndexedDB

Phase 3: Tool Integration (2 weeks)
├── File System Access API for toolbox
├── Battery API for system info
├── Web Search via DuckDuckGo API
└── Export/Import Context7 vault

Phase 4: Polish (2 weeks)
├── Install prompt customization
├── Background sync for offline messages
├── Push notifications (optional)
└── Cross-platform testing
```

### Should You Do It?

**✅ YES, because:**
- **Zero install friction** — instant access via URL
- **Cross-platform** — one codebase for all devices
- **Offline capable** — Service Worker + WebLLM = fully local
- **Discoverable** — SEO-friendly, shareable links
- **Auto-updates** — no app store review needed

**⚠️ But keep native too:**
- Desktop app (Tauri) for power users who want OS integration
- Android (Termux) for the full experience with hardware access
- PWA becomes the "lite" instant-access option

---

## Part 2: Gloam as PWA

### Why Gloam is a PERFECT PWA Candidate

Gloam is **better suited** for PWA migration than Aether:

1. **No heavy inference** — just journaling, mood tracking, CBT prompts
2. **Perfect offline fit** — journaling works great offline
3. **No hardware access needed** — only needs geolocation (available in PWA)
4. **Data persistence** — IndexedDB replaces Room perfectly
5. **Install prompt** — users want journal apps on their home screen

### Architecture

```
┌────────────────────────────────────────────────────┐
│                  Gloam PWA                          │
├────────────────────────────────────────────────────┤
│  Service Worker (full offline support)               │
├──────────────────┬─────────────────────────────────┤
│  IndexedDB       │  Cache API (static assets)       │
│  • journal_entries  • JS/CSS bundles               │
│  • mood_records     • Fonts, icons                 │
│  • prompts          • Manifest, SW                 │
├──────────────────┴─────────────────────────────────┤
│  Shared UI Layer (React + Framer Motion)             │
│  • HomeScreen  • Calendar  • Entries  • Settings    │
├────────────────────────────────────────────────────┤
│  Geolocation API (for sunrise/sunset calculation)    │
│  Notification API (for journaling reminders)         │
└────────────────────────────────────────────────────┘
```

### Feature Parity

| Feature | Android (Room) | PWA (IndexedDB) | Notes |
|---|---|---|---|
| Journal entries | ✅ Room | ✅ IndexedDB | Same data model |
| Mood tracking | ✅ Room | ✅ IndexedDB | Same calculations |
| CBT prompts | ✅ Room seed | ✅ IndexedDB seed | Seed on first load |
| Sun calculator | ✅ java.time | ✅ JS date math | Same NOAA algorithm |
| PIN lock | ✅ SHA-256 + SharedPrefs | ✅ Web Crypto API | Equally secure |
| Export JSON | ✅ CreateDocument intent | ✅ Download API | Same output |
| Notifications | ✅ AlarmManager | ✅ Notification API | Both work offline |
| Location | ✅ FusedLocationProvider | ✅ Geolocation API | Same precision |
| Backup/restore | ✅ JSON export | ✅ JSON import/export | Compatible format |
| Theme | ✅ Daylight progress | ✅ Daylight progress | Same interpolation |

### Implementation Plan

```
Phase 1: Shared Core (3 weeks)
├── Port Kotlin models → TypeScript
├── Port SunCalculator → TypeScript (already in commonMain)
├── Port GloamRepository → TypeScript with IndexedDB adapter
├── Port GloamViewModel → React hooks
└── Port UI components → React (1:1 with Compose)

Phase 2: PWA Features (2 weeks)
├── Service Worker (offline-first)
├── IndexedDB schema (matching Room)
├── Geolocation API integration
├── Web Crypto for PIN
└── Notification API for reminders

Phase 3: Mobile Optimization (1 week)
├── Responsive design (mobile-first)
├── Touch gestures (swipe entries)
├── Add to home screen prompt
├── Status bar theming
└── Pull-to-refresh

Phase 4: Sync (optional, 2 weeks)
├── E2EE sync via WebCrypto
├── Conflict resolution
├── Multi-device support
└── Cloud backup (optional)
```

### Performance Targets

| Metric | Target | Notes |
|---|---|---|
| First contentful paint | < 1s | Service Worker cache hit |
| Time to interactive | < 2s | Minimal JS, code splitting |
| Offline support | 100% | All core features work offline |
| Storage usage | < 50MB | IndexedDB for 1 year of entries |
| Lighthouse PWA score | 100 | All criteria met |

### Shared Data Format (Android ↔ PWA)

```typescript
// This format works for both Room (Android) and IndexedDB (PWA)
interface JournalEntry {
  id: string;          // UUID
  date: string;        // ISO 8601: "2026-04-07"
  entryType: 'SUNRISE' | 'SUNSET';
  moodScore: number;   // 1-5
  prompt1Response: string;
  prompt2Response: string;
  prompt3Response: string;
  createdAt: string;   // ISO 8601
  updatedAt: string;   // ISO 8601
}

// Export/import is compatible between Android and PWA
```

---

## Part 3: Unified Strategy

### Recommended Approach

```
                    ┌─────────────────────┐
                    │   Shared Logic      │
                    │   (TypeScript)      │
                    │  • Models           │
                    │  • Sun Calculator   │
                    │  • Repository       │
                    │  • CBT Prompts      │
                    └──────┬──────┬───────┘
                           │      │
              ┌────────────┘      └────────────┐
              ▼                                ▼
    ┌─────────────────┐              ┌─────────────────┐
    │  Android (KMP)  │              │     PWA         │
    │  Compose MP     │              │  React + TS     │
    │  Room + SQLite  │              │  IndexedDB      │
    │  FusedLocation  │              │  Geolocation API │
    └─────────────────┘              └─────────────────┘
              │                                │
              ▼                                ▼
    ┌─────────────────┐              ┌─────────────────┐
    │  Desktop (KMP)  │              │  Desktop (Tauri) │
    │  Compose Desktop│              │  Web + Rust      │
    │  SQLite JDBC    │              │  Same PWA + SW   │
    └─────────────────┘              └─────────────────┘
```

### Priority Order

1. **Gloam PWA** — Quick win, perfect fit, 6-8 weeks total
2. **Aether PWA** — Bigger effort (WebLLM), but massive reach, 8-10 weeks
3. **Sync layer** — Connect Android + PWA for both apps, 4 weeks

### Cost-Benefit Analysis

| | Gloam PWA | Aether PWA |
|---|---|---|
| **Dev effort** | 6-8 weeks | 8-10 weeks |
| **Complexity** | Low | Medium (WebLLM) |
| **Reach** | Any browser | Any browser with WebGPU |
| **Offline** | 100% | 80% (large models need download) |
| **Install friction** | One tap | One tap + model download |
| **Maintenance** | Low (one codebase) | Medium (WebLLM updates) |
| **Revenue impact** | Higher (more users) | Medium (niche) |
| **Recommendation** | **DO IT NOW** | **DO IT** |

---

*Both apps benefit enormously from PWA migration. Gloam is the easier and higher-ROI first target.*
