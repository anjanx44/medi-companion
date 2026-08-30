package com.medicompanion.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medicompanion.app.data.BpEntry
import com.medicompanion.app.data.BpRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MediViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BpRepository(app)

    private val _from = MutableStateFlow<String?>(null)
    private val _to = MutableStateFlow<String?>(null)

    val entries: StateFlow<List<BpEntry>> = combine(_from, _to) { f, t -> f to t }
        .flatMapLatest { (f, t) -> if (f != null && t != null) repo.observeRange(f, t) else repo.observeAll() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRange(from: String?, to: String?) { _from.value = from; _to.value = to }
    fun clearRange() { _from.value = null; _to.value = null }

    private val _msg = MutableStateFlow<String?>(null)
    val msg: StateFlow<String?> = _msg

    fun add(date: String, timeSlot: String, sys: Int, dia: Int, pulse: Int?) {
        viewModelScope.launch {
            val r = repo.add(date, timeSlot, sys, dia, pulse)
            _msg.value = if (r.isSuccess) {
                if (sys > 150 || dia > 90) "Saved — high BP, consider Amlocal" else "Saved"
            } else "Failed: ${r.exceptionOrNull()?.message}"
        }
    }

    fun delete(id: String) { viewModelScope.launch { repo.delete(id) } }
    fun update(entry: com.medicompanion.app.data.BpEntry) { viewModelScope.launch { repo.update(entry); _msg.value = "Updated" } }
    fun consumeMsg() { _msg.value = null }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    fun sync() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            val r = repo.sync()
            _isSyncing.value = false
            _msg.value = r.fold(
                onSuccess = { s -> "Sync OK — ${s.added} added, ${s.updated} updated" },
                onFailure = { e -> "Sync failed: ${e.message ?: "unknown error"}" }
            )
        }
    }
}
