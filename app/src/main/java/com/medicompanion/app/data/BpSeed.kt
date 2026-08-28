package com.medicompanion.app.data

// From 7.bp-chart-15-day.md — 22/08 baseline → 28/08
val BP_CHART_SEED = listOf(
    // 22/08 (Sat) baseline — evening only
    BpEntry(date = "2026-08-22", systolic = 120, diastolic = 80),
    // 23/08
    BpEntry(date = "2026-08-23", systolic = 110, diastolic = 80),
    BpEntry(date = "2026-08-23", systolic = 120, diastolic = 80),
    // 24/08
    BpEntry(date = "2026-08-24", systolic = 110, diastolic = 70),
    BpEntry(date = "2026-08-24", systolic = 100, diastolic = 70),
    // 25/08
    BpEntry(date = "2026-08-25", systolic = 120, diastolic = 90),
    BpEntry(date = "2026-08-25", systolic = 100, diastolic = 70),
    // 26/08 ⭐ visit
    BpEntry(date = "2026-08-26", systolic = 100, diastolic = 60),
    BpEntry(date = "2026-08-26", systolic = 100, diastolic = 70),
    // 27/08
    BpEntry(date = "2026-08-27", systolic = 115, diastolic = 80),
    BpEntry(date = "2026-08-27", systolic = 100, diastolic = 70),
    // 28/08
    BpEntry(date = "2026-08-28", systolic = 120, diastolic = 80),
    BpEntry(date = "2026-08-28", systolic = 115, diastolic = 75),
)
