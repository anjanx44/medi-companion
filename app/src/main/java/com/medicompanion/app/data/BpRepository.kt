package com.medicompanion.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BpRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun currentUid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    private fun col(uid: String) = db.collection("users").document(uid).collection("bp_entries")

    // Firebase is the single source of truth (live from the cloud, scoped per user)
    fun observeAll(): Flow<List<BpEntry>> = callbackFlow {
        val uid = currentUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val reg = col(uid).addSnapshotListener { snap, err ->
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

    suspend fun add(date: String, timeSlot: String, systolic: Int, diastolic: Int, pulse: Int?): Result<Unit> {
        return try {
            val uid = currentUid() ?: return Result.failure(IllegalStateException("Not signed in"))
            val entry = BpEntry(
                id = UUID.randomUUID().toString(),
                userId = uid,
                date = date,
                timeSlot = timeSlot,
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse
            )
            col(uid).document(entry.id).set(entry).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun delete(id: String): Result<Unit> {
        return try {
            val uid = currentUid() ?: return Result.failure(IllegalStateException("Not signed in"))
            val doc = col(uid).document(id)
            val snap = doc.get().await()
            if (snap.exists() && snap.getString("userId") != null && snap.getString("userId") != uid) {
                return Result.failure(IllegalStateException("Not your entry"))
            }
            doc.delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun update(entry: BpEntry): Result<Unit> {
        return try {
            val uid = currentUid() ?: return Result.failure(IllegalStateException("Not signed in"))
            col(uid).document(entry.id).set(entry.copy(userId = uid)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
