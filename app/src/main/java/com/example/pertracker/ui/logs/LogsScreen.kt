package com.example.pertracker.ui.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pertracker.ui.theme.IncomeGreen
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
            TopAppBar(
                title = { Text("Transaction Logs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Filter Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeMode == FilterMode.ALL,
                    onClick = { viewModel.setFilterMode(FilterMode.ALL) },
                    label = { Text("All") },
                    shape = RoundedCornerShape(16.dp)
                )
                FilterChip(
                    selected = activeMode == FilterMode.MONTHLY,
                    onClick = { 
                        val cal = java.util.Calendar.getInstance()
                        viewModel.setMonthlyFilter(cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR)) 
                    },
                    label = { Text("This Month") },
                    shape = RoundedCornerShape(16.dp)
                )
                FilterChip(
                    selected = activeMode == FilterMode.CUSTOM,
                    onClick = { viewModel.setFilterMode(FilterMode.CUSTOM) },
                    label = { Text("Custom Date") },
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            // Custom Range inputs
            if (activeMode == FilterMode.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartDatePicker = true }, 
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(viewModel.customStartDate.collectAsState().value?.let { dateFormat.format(Date(it)) } ?: "Start Date")
                    }
                    OutlinedButton(
                        onClick = { showEndDatePicker = true }, 
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(viewModel.customEndDate.collectAsState().value?.let { dateFormat.format(Date(it)) } ?: "End Date")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // List of transactions
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions, key = { it.transaction.transactionId }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.transaction.remarks.ifBlank { "No remark" }, 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant) {
                                        Text(item.categoryName, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                    Text(
                                        dateFormat.format(Date(item.transaction.transactionDate)), 
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (item.transaction.amount >= 0) "+ Rp ${item.transaction.amount}" else "- Rp ${-item.transaction.amount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.transaction.amount >= 0) IncomeGreen else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                if (transactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "No transactions found.", 
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
