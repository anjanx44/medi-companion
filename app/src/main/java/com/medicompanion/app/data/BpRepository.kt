package com.medicompanion.app.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BpRepository(context: Context) {

    private val dao = BpDatabase.get(context).bpDao()
    private val prefs = context.getSharedPreferences("medi_prefs", Context.MODE_PRIVATE)
    private val deviceId: String = prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("device_id", it).apply()
    }

    private val db = FirebaseFirestore.getInstance()
    private val col get() = db.collection("users").document(deviceId).collection("bp_entries")

    // Room is source of truth for UI (offline-first)
    fun observeAll(): Flow<List<BpEntry>> = dao.observeAll()
    fun observeRange(from: String, to: String): Flow<List<BpEntry>> = dao.observeRange(from, to)

    // Firestore live sync (optional — merges into Room)
    fun observeFirestore(): Flow<List<BpEntry>> = callbackFlow {
        val reg = col.orderBy("createdAt", Query.Direction.DESCENDING).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val items = snap?.documents?.mapNotNull { it.toObject(BpEntry::class.java)?.copy(id = it.id) } ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }

    suspend fun add(date: String, timeSlot: String, systolic: Int, diastolic: Int, pulse: Int?): Result<Unit> = try {
        val entry = BpEntry(id = UUID.randomUUID().toString(), date = date, timeSlot = timeSlot, systolic = systolic, diastolic = diastolic, pulse = pulse)
        dao.upsert(entry)
        // best-effort Firestore sync — don't fail if offline
        try { col.document(entry.id).set(entry).await() } catch (_: Exception) {}
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: String): Result<Unit> = try {
        dao.deleteById(id)
        try { col.document(id).delete().await() } catch (_: Exception) {}
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(entry: BpEntry): Result<Unit> = try {
        dao.update(entry)
        try { col.document(entry.id).set(entry).await() } catch (_: Exception) {}
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun count(): Int = dao.count()
}
