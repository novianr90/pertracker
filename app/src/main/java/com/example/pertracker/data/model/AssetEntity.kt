package com.example.pertracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetType {
    STOCK, MUTUAL_FUND, SUKUK, CASH, GOLD
}

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val tickerSymbol: String?,
    val assetType: AssetType,
    val isSyariah: Boolean = false,
    val totalUnits: Double,
    val averageBuyPrice: Double,
    val currentMarketPrice: Double,
    val lastUpdated: Long
)
