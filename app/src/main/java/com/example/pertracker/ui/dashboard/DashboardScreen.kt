package com.example.pertracker.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBudgets: () -> Unit
) {
    val goals by viewModel.goals.collectAsState()
    val summaries by viewModel.monthlySummary.collectAsState()
    val todaySummary by viewModel.todaySummary.collectAsState()
    val topRecent by viewModel.topRecentTransactions.collectAsState()

    val todayStr = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddTransaction) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Date Today & Today Transactions
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Today, $todayStr", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Income", style = MaterialTheme.typography.bodyMedium)
                                Text("+${todaySummary.totalIncome}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Expense", style = MaterialTheme.typography.bodyMedium)
                                Text("-${todaySummary.totalExpense}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. 4 Buttons CardView
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardActionButton(
                        modifier = Modifier.weight(1f),
                        title = "Category",
                        onClick = onNavigateToCategories
                    )
                    DashboardActionButton(
                        modifier = Modifier.weight(1f),
                        title = "Budget",
                        onClick = onNavigateToBudgets
                    )
                    DashboardActionButton(
                        modifier = Modifier.weight(1f),
                        title = "Logs",
                        onClick = { /* Navigate to Transactions List later */ }
                    )
                    DashboardActionButton(
                        modifier = Modifier.weight(1f),
                        title = "Config",
                        onClick = onNavigateToSettings
                    )
                }
            }

            // 4. Mini Charts (Pie Chart)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Today's Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PieChart(
                            data = todaySummary.pieData,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            if (todaySummary.pieData.isEmpty()) {
                                Text("No expenses today.", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                val colors = listOf(Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFD54F), Color(0xFFBA68C8))
                                todaySummary.pieData.forEachIndexed { index, pieData ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp).padding(2.dp)) {
                                            Canvas(modifier = Modifier.matchParentSize()) {
                                                drawCircle(color = colors[index % colors.size])
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${pieData.categoryName}: ${pieData.amount}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. 3 Top Latest Transactions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (topRecent.isEmpty()) {
                            Text("No recent transactions", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remarks", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Category", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Amount", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                            Divider()
                            topRecent.forEach { item ->
                                val tx = item.transaction
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tx.remarks, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    Text(item.categoryName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = tx.amount.toString(),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (tx.amount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Monthly Summary
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aggregated Summary", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
            }
            items(summaries) { summary ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Month ${summary.month}/${summary.year}", style = MaterialTheme.typography.titleMedium)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Income: +${summary.totalIncome}", color = Color(0xFF2E7D32))
                            Text("Expense: -${summary.totalExpense}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Financial Goals
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Financial Goals", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
            }
            items(goals) { goal ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
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
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DashboardActionButton(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PieChart(
    data: List<PieData>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFD54F), Color(0xFFBA68C8))
) {
    if (data.isEmpty()) {
        Canvas(modifier = modifier) {
            drawCircle(Color.LightGray)
        }
        return
    }
    
    val total = data.map { it.amount }.sum().coerceAtLeast(1f)
    var startAngle = -90f
    
    Canvas(modifier = modifier) {
        data.forEachIndexed { index, item ->
            val sweepAngle = (item.amount / total) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                style = Fill
            )
            startAngle += sweepAngle
        }
    }
}
