package com.medicompanion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.medicompanion.app.ui.MediViewModel
import com.medicompanion.app.ui.screens.HistoryScreen
import com.medicompanion.app.ui.screens.InputScreen
import com.medicompanion.app.ui.theme.MediTheme

class MainActivity : ComponentActivity() {
    private val vm: MediViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediTheme {
                MainScreen(vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(vm: MediViewModel) {
    var tab by remember { mutableStateOf(0) }
    val entries by vm.entries.collectAsStateWithLifecycle()
    val msg by vm.msg.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(msg) { msg?.let { snackbar.showSnackbar(it); vm.consumeMsg() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Medi Companion") },
                actions = {
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(vm.deviceId))
                        scope.launch { snackbar.showSnackbar("Device ID copied: ${vm.deviceId.take(8)}…") }
                    }) { Icon(Icons.Default.Info, contentDescription = "Copy Device ID") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Add, null) }, label = { Text("Input") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.List, null) }, label = { Text("History") })
            }
        }
    ) { pad ->
        Surface(modifier = Modifier.padding(pad)) {
            when (tab) {
                0 -> InputScreen(onSave = { d, slot, s, dia, p -> vm.add(d, slot, s, dia, p) })
                else -> HistoryScreen(entries = entries, onDelete = vm::delete, onUpdate = vm::update, onRange = vm::setRange)
            }
        }
    }
}
