package com.example.pertracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigateBack: () -> Unit) {
    val autoSyncEnabled by viewModel.isAutoSyncEnabled.collectAsState()
    val syncUrl by viewModel.syncUrl.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val unsyncedTxs by viewModel.unsyncedTransactions.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUnsyncedTransactions()
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Settings & Sync Queue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Auto Sync", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = { viewModel.setAutoSync(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Webhook Configuration", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = syncUrl,
                onValueChange = { viewModel.setSyncUrl(it) },
                label = { Text("POST Sync Transaction URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { viewModel.setApiKey(it) },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Unsynced Transactions (${unsyncedTxs.size})", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { viewModel.manualSync {} },
                    enabled = unsyncedTxs.isNotEmpty()
                ) {
                    Text("Sync Now")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(unsyncedTxs) { tx ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Amount: ${tx.amount}")
                            Text("Remarks: ${tx.remarks}")
                            Text("Status: Pending Sync", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
