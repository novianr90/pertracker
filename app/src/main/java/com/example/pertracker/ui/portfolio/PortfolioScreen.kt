package com.example.pertracker.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pertracker.data.model.AssetEntity
import com.example.pertracker.data.model.AssetType
import com.example.pertracker.data.repository.getUnrealizedProfitAmount
import com.example.pertracker.data.repository.getUnrealizedProfitPercentage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel,
    onNavigateBack: () -> Unit
) {
    val assets by viewModel.assets.collectAsState()
    val totalNetWorth by viewModel.totalNetWorth.collectAsState()
    val isSyariahOnly by viewModel.isSyariahOnly.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    // Dialog state
    var name by remember { mutableStateOf("") }
    var tickerSymbol by remember { mutableStateOf("") }
    var assetType by remember { mutableStateOf(AssetType.MUTUAL_FUND) }
    var isSyariah by remember { mutableStateOf(false) }
    var totalUnits by remember { mutableStateOf("") }
    var averageBuyPrice by remember { mutableStateOf("") }
    var currentMarketPrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Portfolio & Wealth") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Asset")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Net Worth", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = String.format(Locale.getDefault(), "Rp %,.2f", totalNetWorth ?: 0.0),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Filters
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                FilterChip(
                    selected = !isSyariahOnly,
                    onClick = { if (isSyariahOnly) viewModel.toggleSyariahFilter() },
                    label = { Text("All Assets") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = isSyariahOnly,
                    onClick = { if (!isSyariahOnly) viewModel.toggleSyariahFilter() },
                    label = { Text("Syariah Only") }
                )
            }

            // Asset List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(assets, key = { it.id }) { asset ->
                    AssetItemCard(asset, onDelete = { viewModel.deleteAsset(asset) })
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Asset") },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Asset Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            Text("Asset Type", style = MaterialTheme.typography.bodyMedium)
                            // Using a simple Column for Radios as requested
                            Column {
                                AssetType.values().forEach { type ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = assetType == type,
                                            onClick = { assetType = type }
                                        )
                                        Text(type.name)
                                    }
                                }
                            }
                        }
                        
                        if (assetType == AssetType.STOCK) {
                            item {
                                OutlinedTextField(
                                    value = tickerSymbol,
                                    onValueChange = { tickerSymbol = it },
                                    label = { Text("Ticker Symbol (e.g. BBCA)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isSyariah, onCheckedChange = { isSyariah = it })
                                Text("Syariah Compliant")
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = totalUnits,
                                onValueChange = { totalUnits = it },
                                label = { Text("Total Units") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = averageBuyPrice,
                                onValueChange = { averageBuyPrice = it },
                                label = { Text("Average Buy Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = currentMarketPrice,
                                onValueChange = { currentMarketPrice = it },
                                label = { Text("Current Market Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val parsedUnits = totalUnits.toDoubleOrNull()
                        val parsedBuy = averageBuyPrice.toDoubleOrNull()
                        val parsedCurrent = currentMarketPrice.toDoubleOrNull()

                        if (name.isNotBlank() && parsedUnits != null && parsedBuy != null && parsedCurrent != null) {
                            viewModel.saveAsset(
                                AssetEntity(
                                    name = name,
                                    tickerSymbol = if (assetType == AssetType.STOCK) tickerSymbol.uppercase() else null,
                                    assetType = assetType,
                                    isSyariah = isSyariah,
                                    totalUnits = parsedUnits,
                                    averageBuyPrice = parsedBuy,
                                    currentMarketPrice = parsedCurrent,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                            showDialog = false
                            name = ""
                            tickerSymbol = ""
                            totalUnits = ""
                            averageBuyPrice = ""
                            currentMarketPrice = ""
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetItemCard(asset: AssetEntity, onDelete: () -> Unit) {
    val profitAmount = asset.getUnrealizedProfitAmount()
    val profitPercentage = asset.getUnrealizedProfitPercentage()
    val isProfit = profitAmount >= 0

    val color = if (isProfit) Color(0xFF4CAF50) else Color(0xFFF44336)
    val sign = if (isProfit) "+" else ""

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (asset.assetType == AssetType.STOCK && !asset.tickerSymbol.isNullOrBlank()) {
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                            Text(asset.tickerSymbol.uppercase(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = asset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Type: ${asset.assetType.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (asset.isSyariah) {
                        Text(text = "Syariah Compliant", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Units: ${String.format(Locale.getDefault(), "%,.2f", asset.totalUnits)}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Avg Buy: Rp ${String.format(Locale.getDefault(), "%,.2f", asset.averageBuyPrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Total Value", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = String.format(Locale.getDefault(), "Rp %,.2f", asset.totalUnits * asset.currentMarketPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Unrealized P/L", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = String.format(Locale.getDefault(), "%sRp %,.2f (%s%.2f%%)", sign, profitAmount, sign, profitPercentage),
                        color = color,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
