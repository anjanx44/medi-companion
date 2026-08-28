package com.medicompanion.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BpEntry::class], version = 1, exportSchema = false)
abstract class BpDatabase : RoomDatabase() {
    abstract fun bpDao(): BpDao

    companion object {
        @Volatile private var INSTANCE: BpDatabase? = null
        fun get(context: Context): BpDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, BpDatabase::class.java, "medi_companion.db")
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
