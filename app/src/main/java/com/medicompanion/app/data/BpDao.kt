package com.medicompanion.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BpDao {
    @Query("SELECT * FROM bp_entries WHERE date BETWEEN :from AND :to ORDER BY date DESC, createdAt DESC")
    fun observeRange(from: String, to: String): Flow<List<BpEntry>>

    @Query("SELECT * FROM bp_entries ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<BpEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BpEntry)

    @Query("DELETE FROM bp_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
