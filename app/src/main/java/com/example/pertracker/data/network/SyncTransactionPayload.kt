package com.example.pertracker.data.network

import com.google.gson.annotations.SerializedName

data class SyncTransactionRequest(
    @SerializedName("api_key") val apiKey: String,
    val transactions: List<TransactionPayload>
)

data class TransactionPayload(
    val id: Long,
    val category: String,
    val remarks: String,
    val nominal: Double,
    val goalId: Long? = null
)
