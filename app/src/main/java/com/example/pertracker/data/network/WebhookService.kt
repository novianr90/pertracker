package com.example.pertracker.data.network

import com.example.pertracker.data.model.TransactionEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface WebhookService {
    @POST("webhook/transactions")
    suspend fun syncTransaction(@Body transaction: TransactionEntity): Response<Unit>

    @POST("webhook/transactions/bulk")
    suspend fun syncTransactionsBulk(@Body transactions: List<TransactionEntity>): Response<Unit>
}
