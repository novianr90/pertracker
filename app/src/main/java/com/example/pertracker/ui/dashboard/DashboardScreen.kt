package com.example.pertracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBudgets: () -> Unit
) {
    val txs by viewModel.recentTransactions.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val summaries by viewModel.monthlySummary.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddTransaction) {
                Text("+ Transaction", modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onNavigateToCategories) { Text("Categories") }
                Button(onClick = onNavigateToBudgets) { Text("Budgets") }
                Button(onClick = onNavigateToSettings) { Text("Settings") }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                
                // 1. Monthly Summary
                item {
                    Text("Aggregated Summary:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(8.dp))
                }
                items(summaries) { summary ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Month ${summary.month}/${summary.year}", style = MaterialTheme.typography.titleMedium)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Income: +${summary.totalIncome}", color = Color(0xFF4CAF50))
                                Text("Expense: -${summary.totalExpense}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // 2. Goals
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Financial Goals:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(8.dp))
                }
                items(goals) { goal ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(goal.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                if (goal.isShariaCompliant) {
                                    Badge(containerColor = Color(0xFF4CAF50)) { Text("Halal") }
                                }
                            }
                            Text("Target: ${goal.targetAmount}")
                            Text("Current: ${goal.currentAmount}")
                        }
                    }
                }

                // 3. Recent Transactions
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Recent Transactions:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(8.dp))
                }
                items(txs) { tx ->
                    ListItem(
                        headlineContent = { Text("Amount: ${tx.amount}") },
                        supportingContent = { Text(tx.remarks) },
                        trailingContent = {
                            if (tx.isSynced) {
                                Text("Synced", color = Color(0xFF4CAF50)) // Green
                            } else {
                                Text("Unsynced", color = MaterialTheme.colorScheme.error) // Red
                            }
                        }
                    )
                }
            }
        }
    }
}
