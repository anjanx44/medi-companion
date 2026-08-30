package com.medicompanion.app.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BpRepository(context: Context) {

    private val prefs = context.getSharedPreferences("medi_prefs", Context.MODE_PRIVATE)
    private val deviceId: String = prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("device_id", it).apply()
    }

    private val db = FirebaseFirestore.getInstance()
    private val col get() = db.collection("users").document(deviceId).collection("bp_entries")

    // Firebase is the single source of truth (live from the cloud, no local DB)
    fun observeAll(): Flow<List<BpEntry>> = callbackFlow {
        val reg = col.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val items = snap?.documents?.mapNotNull { it.toObject(BpEntry::class.java)?.copy(id = it.id) }
                ?.sortedWith(compareByDescending<BpEntry> { it.date }.thenByDescending { it.createdAt })
                ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }

    fun observeRange(from: String, to: String): Flow<List<BpEntry>> =
        observeAll().map { list -> list.filter { it.date in from..to } }

    suspend fun add(date: String, timeSlot: String, systolic: Int, diastolic: Int, pulse: Int?): Result<Unit> = try {
        val entry = BpEntry(id = UUID.randomUUID().toString(), date = date, timeSlot = timeSlot, systolic = systolic, diastolic = diastolic, pulse = pulse)
        col.document(entry.id).set(entry).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: String): Result<Unit> = try {
        col.document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(entry: BpEntry): Result<Unit> = try {
        col.document(entry.id).set(entry).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}