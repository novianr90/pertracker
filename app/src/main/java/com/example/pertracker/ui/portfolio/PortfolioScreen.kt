package com.example.pertracker.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pertracker.data.model.AssetEntity
import com.example.pertracker.data.repository.getUnrealizedProfitAmount
import com.example.pertracker.data.repository.getUnrealizedProfitPercentage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel) {
    val assets by viewModel.assets.collectAsState()
    val totalNetWorth by viewModel.totalNetWorth.collectAsState()
    val isSyariahOnly by viewModel.isSyariahOnly.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Portfolio & Wealth") }) }
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
                        style = MaterialTheme.typography.headlineMedium
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
                    AssetItemCard(asset)
                }
            }
        }
    }
}

@Composable
fun AssetItemCard(asset: AssetEntity) {
    val profitAmount = asset.getUnrealizedProfitAmount()
    val profitPercentage = asset.getUnrealizedProfitPercentage()
    val isProfit = profitAmount >= 0

    val color = if (isProfit) Color(0xFF4CAF50) else Color(0xFFF44336)
    val sign = if (isProfit) "+" else ""

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = asset.name, style = MaterialTheme.typography.bodyLarge)
                asset.tickerSymbol?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = "Units: ${asset.totalUnits}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "Rp %,.2f", asset.totalUnits * asset.currentMarketPrice),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = String.format(Locale.getDefault(), "%sRp %,.2f (%s%.2f%%)", sign, profitAmount, sign, profitPercentage),
                    color = color,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
