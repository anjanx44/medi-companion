package com.medicompanion.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.medicompanion.app.data.BpEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(entries: List<BpEntry>, onDelete: (String) -> Unit, onRange: (String?, String?) -> Unit) {
    val ctx = LocalContext.current
    var from by remember { mutableStateOf<LocalDate?>(null) }
    var to by remember { mutableStateOf<LocalDate?>(null) }
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun pickFrom() {
        val d = from ?: LocalDate.now()
        DatePickerDialog(ctx, { _, y, m, day -> from = LocalDate.of(y, m + 1, day); onRange(from?.format(fmt), to?.format(fmt)) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
    }
    fun pickTo() {
        val d = to ?: LocalDate.now()
        DatePickerDialog(ctx, { _, y, m, day -> to = LocalDate.of(y, m + 1, day); onRange(from?.format(fmt), to?.format(fmt)) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = ::pickFrom) { Text(from?.format(fmt) ?: "From") }
            OutlinedButton(onClick = ::pickTo) { Text(to?.format(fmt) ?: "To") }
            if (from != null || to != null) TextButton(onClick = { from = null; to = null; onRange(null, null) }) { Text("Clear") }
        }
        Spacer(Modifier.height(12.dp))
        if (entries.isEmpty()) {
            Text("No entries yet — add your first BP.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { it.id }) { e ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${e.date} — ${e.systolic}/${e.diastolic}" + (e.pulse?.let { " · $it bpm" } ?: ""), style = MaterialTheme.typography.titleMedium)
                                if (e.systolic > 150 || e.diastolic > 90) Text("High", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onDelete(e.id) }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                        }
                    }
                }
            }
        }
    }
}
