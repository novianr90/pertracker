package com.example.pertracker.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pertracker.data.model.AssetEntity
import com.example.pertracker.data.model.AssetType
import com.example.pertracker.data.repository.getUnrealizedProfitAmount
import com.example.pertracker.data.repository.getUnrealizedProfitPercentage
import com.example.pertracker.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    viewModel: PortfolioViewModel,
    assetId: Long,
    onNavigateBack: () -> Unit
) {
    val assets by viewModel.assets.collectAsState()
    val asset = assets.find { it.id == assetId }

    if (asset == null) {
        // Asset not found, navigate back
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    // Editable fields — everything except name and type
    var tickerSymbol by remember(asset.id) { mutableStateOf(asset.tickerSymbol ?: "") }
    var isSyariah by remember(asset.id) { mutableStateOf(asset.isSyariah) }
    var totalUnits by remember(asset.id) { mutableStateOf(asset.totalUnits.toString()) }
    var averageBuyPrice by remember(asset.id) { mutableStateOf(asset.averageBuyPrice.toString()) }
    var currentMarketPrice by remember(asset.id) { mutableStateOf(asset.currentMarketPrice.toString()) }

    var showSuccessSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar("Asset updated successfully")
            showSuccessSnackbar = false
        }
    }

    // Live preview for P/L calculation
    val parsedUnits = totalUnits.toDoubleOrNull() ?: asset.totalUnits
    val parsedBuy = averageBuyPrice.toDoubleOrNull() ?: asset.averageBuyPrice
    val parsedCurrent = currentMarketPrice.toDoubleOrNull() ?: asset.currentMarketPrice
    val previewAsset = asset.copy(
        totalUnits = parsedUnits,
        averageBuyPrice = parsedBuy,
        currentMarketPrice = parsedCurrent
    )
    val profitAmount = previewAsset.getUnrealizedProfitAmount()
    val profitPercentage = previewAsset.getUnrealizedProfitPercentage()
    val isProfit = profitAmount >= 0
    val profitColor = if (isProfit) IncomeGreen else MaterialTheme.colorScheme.error
    val sign = if (isProfit) "+" else ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asset Detail", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Summary Header Card ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = asset.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            asset.assetType.name,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (asset.isSyariah) {
                                        Badge(
                                            containerColor = Color(0xFFF0FDF4),
                                            contentColor = IncomeGreen
                                        ) {
                                            Text(
                                                "Syariah",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            if (asset.assetType == AssetType.STOCK && !asset.tickerSymbol.isNullOrBlank()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        asset.tickerSymbol.uppercase(),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(20.dp))

                        // Total Value & P/L live preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    "Total Value",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "Rp %,.2f", parsedUnits * parsedCurrent),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Unrealized P/L",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%sRp %,.2f (%s%.2f%%)",
                                        sign, profitAmount, sign, profitPercentage
                                    ),
                                    color = profitColor,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Last updated: ${
                                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                    .format(Date(asset.lastUpdated))
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // --- Read-only Info ---
            item {
                Text(
                    "Asset Info (Read-only)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReadOnlyField(label = "Asset Name", value = asset.name)
                        ReadOnlyField(label = "Asset Type", value = asset.assetType.name)
                    }
                }
            }

            // --- Editable Fields ---
            item {
                Text(
                    "Update Asset Data",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Ticker Symbol (editable only for STOCK)
            if (asset.assetType == AssetType.STOCK) {
                item {
                    OutlinedTextField(
                        value = tickerSymbol,
                        onValueChange = { tickerSymbol = it.uppercase() },
                        label = { Text("Ticker Symbol") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Syariah toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Syariah Compliant", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Mark as a Sharia-compliant asset", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isSyariah, onCheckedChange = { isSyariah = it })
                    }
                }
            }

            // Total Units
            item {
                OutlinedTextField(
                    value = totalUnits,
                    onValueChange = { totalUnits = it },
                    label = { Text("Total Units") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Current: ${String.format(Locale.getDefault(), "%,.4f", asset.totalUnits)}") },
                    singleLine = true
                )
            }

            // Average Buy Price
            item {
                OutlinedTextField(
                    value = averageBuyPrice,
                    onValueChange = { averageBuyPrice = it },
                    label = { Text("Average Buy Price (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Current: ${String.format(Locale.getDefault(), "Rp %,.2f", asset.averageBuyPrice)}") },
                    singleLine = true
                )
            }

            // Current Market Price
            item {
                OutlinedTextField(
                    value = currentMarketPrice,
                    onValueChange = { currentMarketPrice = it },
                    label = { Text("Current Market Price (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Current: ${String.format(Locale.getDefault(), "Rp %,.2f", asset.currentMarketPrice)}") },
                    singleLine = true
                )
            }

            // Save Button
            item {
                val isValid = totalUnits.toDoubleOrNull() != null
                        && averageBuyPrice.toDoubleOrNull() != null
                        && currentMarketPrice.toDoubleOrNull() != null

                Button(
                    onClick = {
                        val updatedAsset = asset.copy(
                            tickerSymbol = if (asset.assetType == AssetType.STOCK) tickerSymbol.ifBlank { null } else asset.tickerSymbol,
                            isSyariah = isSyariah,
                            totalUnits = totalUnits.toDoubleOrNull() ?: asset.totalUnits,
                            averageBuyPrice = averageBuyPrice.toDoubleOrNull() ?: asset.averageBuyPrice,
                            currentMarketPrice = currentMarketPrice.toDoubleOrNull() ?: asset.currentMarketPrice,
                            lastUpdated = System.currentTimeMillis()
                        )
                        viewModel.updateAsset(updatedAsset)
                        showSuccessSnackbar = true
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
