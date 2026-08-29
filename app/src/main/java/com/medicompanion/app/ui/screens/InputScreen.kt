package com.medicompanion.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun InputScreen(onSave: (date: String, timeSlot: String, sys: Int, dia: Int, pulse: Int?) -> Unit) {
    val ctx = LocalContext.current
    var date by remember { mutableStateOf(LocalDate.now()) }
    var slot by remember { mutableStateOf("MORNING") }
    var sys by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun pickDate() {
        DatePickerDialog(ctx, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Add Blood Pressure", style = MaterialTheme.typography.titleLarge)
        OutlinedButton(onClick = ::pickDate) { Text(date.format(fmt)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = slot == "MORNING", onClick = { slot = "MORNING" }, label = { Text("Morning 09:15") })
            FilterChip(selected = slot == "EVENING", onClick = { slot = "EVENING" }, label = { Text("Evening 21:45") })
        }
        OutlinedTextField(value = sys, onValueChange = { sys = it.filter(Char::isDigit).take(3) }, label = { Text("Systolic (e.g. 120)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dia, onValueChange = { dia = it.filter(Char::isDigit).take(3) }, label = { Text("Diastolic (e.g. 80)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pulse, onValueChange = { pulse = it.filter(Char::isDigit).take(3) }, label = { Text("Pulse (optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val s = sys.toIntOrNull(); val d = dia.toIntOrNull()
            if (s != null && d != null) {
                onSave(date.format(fmt), slot, s, d, pulse.toIntOrNull())
                sys = ""; dia = ""; pulse = ""
            }
        }, modifier = Modifier.fillMaxWidth(), enabled = sys.toIntOrNull() != null && dia.toIntOrNull() != null) { Text("Save") }
        Text("Tip: same hand, seated, 5 min rest. High >150/90 shows hint.", style = MaterialTheme.typography.bodySmall)
    }
}
