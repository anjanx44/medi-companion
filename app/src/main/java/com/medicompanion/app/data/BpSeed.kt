package com.medicompanion.app.data

// From 7.bp-chart-15-day.md — 22/08 baseline → 28/08 (13 entries, morning/evening)
val BP_CHART_SEED = listOf(
    // 22/08 (Sat) baseline — evening only
    BpEntry(date = "2026-08-22", timeSlot = "EVENING", systolic = 120, diastolic = 80),
    // 23/08
    BpEntry(date = "2026-08-23", timeSlot = "MORNING", systolic = 110, diastolic = 80),
    BpEntry(date = "2026-08-23", timeSlot = "EVENING", systolic = 120, diastolic = 80),
    // 24/08
    BpEntry(date = "2026-08-24", timeSlot = "MORNING", systolic = 110, diastolic = 70),
    BpEntry(date = "2026-08-24", timeSlot = "EVENING", systolic = 100, diastolic = 70),
    // 25/08
    BpEntry(date = "2026-08-25", timeSlot = "MORNING", systolic = 120, diastolic = 90),
    BpEntry(date = "2026-08-25", timeSlot = "EVENING", systolic = 100, diastolic = 70),
    // 26/08 ⭐ visit
    BpEntry(date = "2026-08-26", timeSlot = "MORNING", systolic = 100, diastolic = 60),
    BpEntry(date = "2026-08-26", timeSlot = "EVENING", systolic = 100, diastolic = 70),
    // 27/08
    BpEntry(date = "2026-08-27", timeSlot = "MORNING", systolic = 115, diastolic = 80),
    BpEntry(date = "2026-08-27", timeSlot = "EVENING", systolic = 100, diastolic = 70),
    // 28/08
    BpEntry(date = "2026-08-28", timeSlot = "MORNING", systolic = 120, diastolic = 80),
    BpEntry(date = "2026-08-28", timeSlot = "EVENING", systolic = 115, diastolic = 75),
)
