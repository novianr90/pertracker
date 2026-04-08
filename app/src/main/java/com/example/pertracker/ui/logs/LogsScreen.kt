package com.example.pertracker.ui.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: LogsViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val activeMode by viewModel.activeFilterMode.collectAsState()
    
    // State for dates
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDateState = rememberDatePickerState()
    val endDateState = rememberDatePickerState()

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transaction Logs") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            // Filter Selector
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = activeMode == FilterMode.ALL,
                    onClick = { viewModel.setFilterMode(FilterMode.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = activeMode == FilterMode.MONTHLY,
                    onClick = { 
                        val cal = java.util.Calendar.getInstance()
                        viewModel.setMonthlyFilter(cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR)) 
                    },
                    label = { Text("This Month") }
                )
                FilterChip(
                    selected = activeMode == FilterMode.CUSTOM,
                    onClick = { viewModel.setFilterMode(FilterMode.CUSTOM) },
                    label = { Text("Specific Date") }
                )
            }
            
            // Custom Range inputs
            if (activeMode == FilterMode.CUSTOM) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(viewModel.customStartDate.collectAsState().value?.let { dateFormat.format(Date(it)) } ?: "Start Date")
                    }
                    OutlinedButton(onClick = { showEndDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(viewModel.customEndDate.collectAsState().value?.let { dateFormat.format(Date(it)) } ?: "End Date")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // List of transactions
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transactions, key = { it.transaction.transactionId }) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(item.transaction.remarks.ifBlank { "No remark" }, style = MaterialTheme.typography.titleMedium)
                                Text(item.categoryName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(dateFormat.format(Date(item.transaction.transactionDate)), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                String.format(Locale.getDefault(), "Rp %,.2f", item.transaction.amount),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (item.transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                if (transactions.isEmpty()) {
                    item {
                        Text("No transactions found.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        
        // Date Pickers
        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setCustomDateFilter(startDateState.selectedDateMillis, viewModel.customEndDate.value)
                        showStartDatePicker = false
                    }) { Text("OK") }
                }
            ) {
                DatePicker(state = startDateState)
            }
        }
        
        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setCustomDateFilter(viewModel.customStartDate.value, endDateState.selectedDateMillis)
                        showEndDatePicker = false
                    }) { Text("OK") }
                }
            ) {
                DatePicker(state = endDateState)
            }
        }
    }
}
