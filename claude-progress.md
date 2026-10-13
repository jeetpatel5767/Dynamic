# Pixel 9 Dynamic Island — Progress & Project Overview

> **Harness file for long-running coding agents.** Read this first every session, before touching code.
> Follows the pattern from Anthropic's "Effective harnesses for long-running agents" (Nov 2025).
> Pair file: `feature_list.json` — the single source of truth for what is done vs. not.
> Full spec: `pixel9_dynamic_island_spec.md` (in repo root).

---

## Session Startup Checklist (run every time)

1. `pwd` — confirm working directory.
2. `git log --oneline -20` — recent commits.
3. Read this file (`claude-progress.md`) in full.
4. Read `feature_list.json` — find the lowest-numbered feature with `passes: false` in the current milestone.
5. If `init.sh` exists → run it. It should: verify Android SDK path, run `./gradlew tasks` to confirm Gradle wiring, and install the debug APK on the connected Pixel 9 via `adb` if a device is attached.
6. Pick exactly **one** feature to work on. Do not batch.
7. When the feature is done + verified end-to-end on the actual Pixel 9, flip its `passes` to `true`, commit with a descriptive message, append a progress note to the "Session Log" section below.

**Hard rules (do not break):**
- You may ONLY change a feature's `passes` field in `feature_list.json`. Never remove, rename, or edit a feature's `description`, `category`, or `steps`.
- Never mark `passes: true` without running the feature's `steps` on the physical Pixel 9. "Compiles" is not "works."
- Commit after every completed feature. One feature = one commit (minimum).
- No hardcoded cutout coordinates — always read from `DisplayCutout` at runtime.
- Overlay window must be shown BEFORE starting the foreground service (Android 15 rule).
- Glass effect must be user-adjustable with a working "Classic Black" fallback.
- No paid dependencies, no SaaS. Everything runs on-device.

---

## Project Overview

**What this is:** A Pixel 9 app that renders a Dynamic Island — a pill-shaped UI hugging the front camera cutout at the top of the screen. It sits idle (small, barely visible) and expands when something is actively happening: notifications, media playback, timers, incoming calls, charging. Fully native, no backend, no account.

**Target:** Pixel 9 (model GR83Y). Should also work on Pixel 9 Pro / 9a / Pixel 10 since the position is read at runtime.

**Stack:** Kotlin + Jetpack Compose. Min SDK 31 (Android 12, needed for `RenderEffect`). Target SDK 35 (Android 15). Single app module.

**Core philosophy (cross-cutting, do not violate):**
- Hugs the camera cutout, never covers usable screen content at rest.
- Spring-animated morph between states — no linear/ease curves. Feels alive, not scripted.
- Glass effect is user-configurable; defaults to "Frosted"; classic black available as a preset.
- Permissions are one-time, user-granted, with clear in-app prompts.
- The resting pill is almost invisible. Expansion earns its space by being useful.
- Works offline. No analytics, no telemetry.
- Pixel 9 is primary — tested on real hardware before any feature flips to passing.

---

## Milestone Status Map

| Milestone | Scope | Status |
|---|---|---|
| M0 | Project scaffold, permissions UI, manifest wiring | 🟡 Not started |
| M1 | Overlay service + cutout detection + static pill at correct position | 🟡 Not started |
| M2 | Spring-animated expand/collapse on tap (hardcoded test state) | 🟡 Not started |
| M3 | Notification listener → pill expands on new notif → auto-collapse | 🟡 Not started |
| M4 | Media session (now playing, album art, scrubber, play/pause) | 🟡 Not started |
| M5 | Timer + alarm integration | 🟡 Not started |
| M6 | Incoming call UI + charging indicator | 🟡 Not started |
| M7 | Glass effect with live settings (sliders, presets, DataStore) | 🟡 Not started |
| M8 | Two-pill split layout (timer + media concurrently) + polish | 🟡 Not started |

Live counts come from `feature_list.json`. Use `jq '[.features[] | select(.passes == true)] | length' feature_list.json` for a quick done-count.

---

## M0 — Project Scaffold

**Goal:** Empty but running Android app with permission plumbing, manifest declarations, and the service skeleton that doesn't yet draw anything.

**Key work:** Gradle project with Compose BOM pinned, `AndroidManifest.xml` declaring all 6 permissions (SYSTEM_ALERT_WINDOW, BIND_NOTIFICATION_LISTENER_SERVICE, FOREGROUND_SERVICE + FOREGROUND_SERVICE_SPECIAL_USE, POST_NOTIFICATIONS, READ_PHONE_STATE, SCHEDULE_EXACT_ALARM), MainActivity with the permission request flow, empty `IslandService` that starts/stops cleanly.

**End-of-milestone deliverable:** APK installs on Pixel 9, requests permissions on first launch, service starts without crash, shows a persistent silent notification.

---

## M1 — Static Pill at Cutout

**Goal:** A black capsule drawn at the exact Pixel 9 camera position, read from `DisplayCutout` at runtime (never hardcoded).

**Key work:** `CutoutResolver` reads `windowManager.currentWindowMetrics.windowInsets.displayCutout`, computes center-x and top-y of the hole, returns a `CutoutGeometry`. Overlay window attached via `WindowManager` with correct layout params, Compose root hosting a static `PillShape`.

**End-of-milestone deliverable:** Screenshot of Pixel 9 shows the pill hugging the camera hole with no visible gap, no overlap, no shift on rotation.

---

## M2 — Spring Animation

**Goal:** Tap the pill → it morphs to a larger size with a snappy spring, tap again → back to resting. No real content yet, just the shape physics.

**Key work:** `animateFloatAsState` with `spring(dampingRatio = 0.75f, stiffness = 400f)` driving width + height + corner-radius (always kept at height/2 for perfect capsule). Scale pulse on content changes. `AnimatedContent` for the inner slot.

**End-of-milestone deliverable:** Side-by-side GIF compared to a real iOS Dynamic Island — the physics should feel the same.

---

## M3 — Notifications

**Goal:** New notification arrives → pill expands, shows app icon + title + one-line summary → auto-collapses after 3 seconds.

**Key work:** `NotifListener : NotificationListenerService`, StatusBarNotification parsing, state machine entry for `EXPANDED_NOTIF`, 3s timeout coroutine, dismiss on swipe.

**End-of-milestone deliverable:** Send a WhatsApp message to the phone, pill expands with WhatsApp icon + sender + preview, collapses after 3s.

---

## M4 — Media

**Goal:** Playing Spotify / YouTube Music → pill shows album art + waveform scrubber + play/pause.

**Key work:** `MediaSessionWatcher` using `MediaSessionManager.getActiveSessions()`, polling or callbacks for playback state, inner UI with Coil for album art.

**End-of-milestone deliverable:** Start Spotify, pill goes into persistent media state; pause via pill; see state change on phone screen.

---

## M5 — Timer

**Goal:** User sets a timer → pill shows countdown ring → pulses + sound at 0.

**Key work:** `TimerService` with `StateFlow<TimerState>`, Compose UI drawing an arc that depletes, foreground notification for process reliability, `AlarmManager.setExactAndAllowWhileIdle` for the terminal fire.

**End-of-milestone deliverable:** Start 60s timer from a settings screen, watch it count down on the pill, hear it at zero.

---

## M6 — Call + Charging

**Goal:** Incoming call → pill shows caller + answer/decline. Plug charger → pill pulses once with battery %.

**Key work:** `TelephonyCallback` on API 31+ for call state, `BatteryManager` broadcast receiver for `ACTION_POWER_CONNECTED`, pulse animation (scale 1.0 → 1.08 → 1.0 with spring).

**End-of-milestone deliverable:** Call the phone from another device, see pill; unplug and plug charger, see pulse.

---

## M7 — Glass Effect + Settings

**Goal:** Full settings screen with live-preview sliders for glass opacity, border, highlight, noise, tint, corner smoothing. 4 presets. Toggle to disable glass entirely (Classic Black).

**Key work:** `DataStore<Preferences>` for all settings, `StateFlow` from the service to live-update the overlay without restart, Settings Compose screen with a mini preview pill at the top.

**End-of-milestone deliverable:** Open settings, drag opacity slider, watch pill reskin in real time without restart.

---

## M8 — Two-Pill Split + Polish

**Goal:** Timer running AND media playing → pill splits into two capsules (iOS behavior). Final polish pass.

**Key work:** State machine resolves conflicts into `SPLIT` state, layout renders two capsules with a small gap, priority still enforced (incoming call collapses everything else).

**End-of-milestone deliverable:** Start a timer, start music → see two capsules; receive a call → both collapse, call UI takes over, returns to split when call ends.

---

## Testing Discipline (mandatory for every feature)

Before marking a feature `passes: true`:

1. **Install the debug APK on a real Pixel 9** via `adb install`. Emulator passes don't count — overlay + cutout + spring timing need real hardware.
2. **Run the feature's `steps` end-to-end**, in order, exactly as written.
3. **Screenshot or screen-record** the result and store under `docs/evidence/<feature_id>.png` (or `.mp4`). Reference the filename in the commit.
4. **UI features** → verify the pill's position doesn't drift between portrait and landscape (or confirm the service hides it in landscape, which is also acceptable).
5. **Service features** → leave the phone idle for 10 minutes with the service running, confirm it's still alive (no system kill, no OOM).
6. **Settings features** → change the setting, lock-unlock the phone, confirm the setting persisted.

Idempotency check: perform the trigger twice in a row (e.g., two notifications back-to-back). State machine must handle gracefully.

---

## Known Non-Goals (do not build)

- No Dynamic Island on anything other than the pill. No lock screen widgets. No home screen widgets. No launcher replacement.
- No cloud sync of settings. DataStore stays on-device.
- No Wear OS companion.
- No face unlock integration (system-only on Pixel).
- No root required, no Shizuku, no custom ROM features.

---

## Reference Documents

- Full spec: `pixel9_dynamic_island_spec.md` (in repo root)
- Feature list (machine-readable source of truth): `feature_list.json`
- Android 15 overlay-order gotcha: Section 10 of the spec

---

## Session Log

> Append a dated entry at the end of every session. Keep each entry under 10 lines. Format:
> `## YYYY-MM-DD — <short title>`
> `- what was worked on`
> `- what was finished (features flipped to passes: true)`
> `- blockers / notes for next agent`

### 2026-10-13 — Harness bootstrap
- Created `claude-progress.md` (this file) and `feature_list.json`.
- No code yet. All features start with `passes: false`.
- Next agent: begin M0 feature `0.1-1` (create Android project with Compose BOM, min SDK 31, target SDK 35).
