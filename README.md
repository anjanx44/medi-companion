# Medi Companion — BP Tracker & Pill Reminder

Worldwide, ad-free, free (☕ Buy Me a Coffee optional). Offline-first: Room (phone) + Firestore sync.

- **Package:** `com.medicompanion.app`
- **Firebase:** `medi-companion` (Firestore Spark free)
- **Stack:** Kotlin + Compose + Room + Firestore (SpendlyMvp pattern + Room)

## Feature 1 (current)
- BP input with date (date picker, systolic/diastolic/pulse) → Room + Firestore (`users/{deviceId}/bp_entries`)
- History page with date-range filter (from/to), sorted desc, delete, high-BP hint (>150/90)

## Run
1. Firebase: create project `medi-companion` → put `google-services.json` in `app/` (if not present)
2. `./gradlew :app:assembleDebug`

## Docs
`C:\Users\dev-pc\My Drive\obsidian\redVault\5.Projects\14.BP-Companion-BD\` — Index, Firebase-Free-MVP, Task-Plan
