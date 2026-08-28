package com.medicompanion.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.medicompanion.app.data.BpEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val displayFmt = DateTimeFormatter.ofPattern("dd/MM (EEE)")
private val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun HistoryScreen(entries: List<BpEntry>, onDelete: (String) -> Unit, onRange: (String?, String?) -> Unit, onReseed: (() -> Unit)? = null) {
    val ctx = LocalContext.current
    var from by remember { mutableStateOf<LocalDate?>(null) }
    var to by remember { mutableStateOf<LocalDate?>(null) }

    fun pickFrom() {
        val d = from ?: LocalDate.now()
        DatePickerDialog(ctx, { _, y, m, day -> from = LocalDate.of(y, m + 1, day); onRange(from?.format(isoFmt), to?.format(isoFmt)) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
    }
    fun pickTo() {
        val d = to ?: LocalDate.now()
        DatePickerDialog(ctx, { _, y, m, day -> to = LocalDate.of(y, m + 1, day); onRange(from?.format(isoFmt), to?.format(isoFmt)) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
    }

    // Group by date for chart-table: 1 row per date, 2 cols
    val byDate = entries.groupBy { it.date }
    val sortedDates = if (from != null && to != null) {
        byDate.keys.sorted()
    } else {
        // Show 22/08–05/09 range like paper chart, plus any extra dates
        val chartRange = (0..14).map { LocalDate.of(2026, 8, 22).plusDays(it.toLong()).format(isoFmt) }
        (chartRange + byDate.keys).distinct().sorted()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("History — 15-day Chart", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = ::pickFrom) { Text(from?.format(isoFmt) ?: "From") }
            OutlinedButton(onClick = ::pickTo) { Text(to?.format(isoFmt) ?: "To") }
            if (from != null || to != null) TextButton(onClick = { from = null; to = null; onRange(null, null) }) { Text("Clear") }
        }
        Spacer(Modifier.height(12.dp))

        if (entries.isEmpty() && from == null && to == null) {
            Text("No entries yet — seed loads 22–28/08 on first launch.", style = MaterialTheme.typography.bodyMedium)
        }
        if (onReseed != null && entries.size < 13) {
            Button(onClick = onReseed, modifier = Modifier.fillMaxWidth()) { Text("Re-seed 22–28/08 (13 entries)") }
            Spacer(Modifier.height(8.dp))
        }

        // Chart table header
        Card {
            Column(Modifier.horizontalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date", modifier = Modifier.width(110.dp), style = MaterialTheme.typography.labelLarge)
                    Text("Morning 09:15", modifier = Modifier.width(110.dp), style = MaterialTheme.typography.labelLarge)
                    Text("Evening 21:45", modifier = Modifier.width(110.dp), style = MaterialTheme.typography.labelLarge)
                    Text("", modifier = Modifier.width(40.dp))
                }
                Divider()
                LazyColumn {
                    items(sortedDates) { date ->
                        val morning = byDate[date]?.find { it.timeSlot == "MORNING" }
                        val evening = byDate[date]?.find { it.timeSlot == "EVENING" }
                        val isVisit = date == "2026-08-26" || date == "2026-09-05"
                        val label = try { LocalDate.parse(date).format(displayFmt) } catch (_: Exception) { date }
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (isVisit) "⭐ " else "") + label, modifier = Modifier.width(110.dp), style = MaterialTheme.typography.bodyMedium)
                            Box(Modifier.width(110.dp)) {
                                if (morning != null) {
                                    val high = morning.systolic > 150 || morning.diastolic > 90
                                    Text("${morning.systolic}/${morning.diastolic}" + (morning.pulse?.let { " · $it" } ?: ""), color = if (high) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                                } else Text("—", style = MaterialTheme.typography.bodyMedium)
                            }
                            Box(Modifier.width(110.dp)) {
                                if (evening != null) {
                                    val high = evening.systolic > 150 || evening.diastolic > 90
                                    Text("${evening.systolic}/${evening.diastolic}" + (evening.pulse?.let { " · $it" } ?: ""), color = if (high) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                                } else Text("—", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(Modifier.width(40.dp)) {
                                // Delete actions per slot if exists
                                if (morning != null) IconButton(onClick = { onDelete(morning.id) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete morning", modifier = Modifier.size(16.dp)) }
                                if (evening != null) IconButton(onClick = { onDelete(evening.id) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete evening", modifier = Modifier.size(16.dp)) }
                            }
                        }
                        if (sortedDates.last() != date) Divider()
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("⭐ = visit day · Tap morning/evening to edit via Input (same date+slot overwrites not yet, delete then re-add)", style = MaterialTheme.typography.bodySmall)
    }
}
