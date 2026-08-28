package com.medicompanion.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bp_entries")
data class BpEntry(
    @PrimaryKey val id: String = "",
    val date: String = "", // yyyy-MM-dd
    val timeSlot: String = "MORNING", // MORNING (09:15) / EVENING (21:45)
    val systolic: Int = 0,
    val diastolic: Int = 0,
    val pulse: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
