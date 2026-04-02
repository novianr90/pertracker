package com.example.pertracker.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface WebhookService {
    @POST
    suspend fun syncTransaction(
        @Url url: String,
        @Body request: SyncTransactionRequest
    ): Response<SyncResponse>

    @POST
    suspend fun syncTransactionsBulk(
        @Url url: String,
        @Body request: SyncTransactionRequest
    ): Response<SyncResponse>
}
