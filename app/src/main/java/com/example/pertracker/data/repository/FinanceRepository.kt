package com.example.pertracker.data.repository

import androidx.room.withTransaction
import com.example.pertracker.data.dao.BudgetDao
import com.example.pertracker.data.dao.CategoryDao
import com.example.pertracker.data.dao.GoalDao
import com.example.pertracker.data.dao.TransactionDao
import com.example.pertracker.data.datastore.SettingsDataStore
import com.example.pertracker.data.db.AppDatabase
import com.example.pertracker.data.model.Budget
import com.example.pertracker.data.model.Category
import com.example.pertracker.data.model.Goal
import com.example.pertracker.data.model.TransactionEntity
import com.example.pertracker.data.network.SyncTransactionRequest
import com.example.pertracker.data.network.TransactionPayload
import com.example.pertracker.data.network.WebhookService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FinanceRepository(
    private val appDatabase: AppDatabase,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val settingsDataStore: SettingsDataStore,
    private val webhookService: WebhookService
) {

    // --- Category Operations ---
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        if (category.isSystemDefault) {
            throw IllegalArgumentException("Cannot delete a system default category.")
        }
        categoryDao.deleteCategory(category)
    }

    // --- Budget Operations ---
    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun getBudget(categoryId: Long, month: Int, year: Int): Budget? {
        return budgetDao.getBudget(categoryId, month, year)
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    // --- Goal Operations ---
    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()

    suspend fun insertGoal(goal: Goal) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }

    // --- Transaction Operations ---
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun getUnsyncedTransactions(): List<TransactionEntity> {
        return transactionDao.getUnsyncedTransactions()
    }

    /**
     * Inserts a transaction and updates the corresponding budget.
     * Also handles auto-sync logic if enabled.
     */
    suspend fun insertTransaction(transaction: TransactionEntity): Boolean? {
        var finalTransaction: TransactionEntity? = null
        appDatabase.withTransaction {
            val (month, year) = getMonthAndYear(transaction.transactionDate)

            // Update Budget
            val budget = budgetDao.getBudget(transaction.categoryId, month, year)
            if (budget != null) {
                val updatedBudget = budget.copy(currentAmount = budget.currentAmount + transaction.amount)
                budgetDao.updateBudget(updatedBudget)
            }

            // Insert Transaction (ensure it starts unsynced)
            val txToSave = transaction.copy(isSynced = false)
            val id = transactionDao.insertTransaction(txToSave)
            finalTransaction = txToSave.copy(transactionId = id)
        }

        return finalTransaction?.let { tx ->
            syncTransactionIfEnabled(tx)
        }
    }

    /**
     * Updates a transaction and adjusts the corresponding budgets correctly.
     * Also handles auto-sync logic if enabled.
     */
    suspend fun updateTransaction(newTransaction: TransactionEntity) {
        var needsSync = false
        appDatabase.withTransaction {
            val oldTransaction = transactionDao.getTransactionById(newTransaction.transactionId)
            if (oldTransaction != null) {
                // Revert old budget
                val (oldMonth, oldYear) = getMonthAndYear(oldTransaction.transactionDate)
                val oldBudget = budgetDao.getBudget(oldTransaction.categoryId, oldMonth, oldYear)
                if (oldBudget != null) {
                    budgetDao.updateBudget(
                        oldBudget.copy(currentAmount = oldBudget.currentAmount - oldTransaction.amount)
                    )
                }

                // Apply new budget
                val (newMonth, newYear) = getMonthAndYear(newTransaction.transactionDate)
                val newBudget = budgetDao.getBudget(newTransaction.categoryId, newMonth, newYear)
                if (newBudget != null) {
                    budgetDao.updateBudget(
                        newBudget.copy(currentAmount = newBudget.currentAmount + newTransaction.amount)
                    )
                }

                // Since local data changed, reset sync status
                val txToSave = newTransaction.copy(isSynced = false)
                transactionDao.updateTransaction(txToSave)
                needsSync = true
            }
        }

        if (needsSync) {
            syncTransactionIfEnabled(newTransaction.copy(isSynced = false))
        }
    }

    /**
     * Deletes a transaction and removes its amount from the corresponding budget.
     */
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        appDatabase.withTransaction {
            val (month, year) = getMonthAndYear(transaction.transactionDate)
            val budget = budgetDao.getBudget(transaction.categoryId, month, year)
            if (budget != null) {
                budgetDao.updateBudget(
                    budget.copy(currentAmount = budget.currentAmount - transaction.amount)
                )
            }

            transactionDao.deleteTransaction(transaction)
        }
    }

    // --- Sync Operations ---
    
    /**
     * Syncs a single transaction to the webhook.
     */
    private suspend fun syncTransactionIfEnabled(transaction: TransactionEntity): Boolean? {
        val autoSync = settingsDataStore.isAutoSyncEnabled.first()
        val syncUrl = settingsDataStore.syncUrl.first()
        val apiKey = settingsDataStore.apiKey.first()
        
        if (autoSync && syncUrl.isNotBlank()) {
            try {
                val category = categoryDao.getCategoryById(transaction.categoryId)
                val txPayload = TransactionPayload(
                    id = transaction.transactionId,
                    category = category?.name ?: "Unknown",
                    remarks = transaction.remarks,
                    nominal = transaction.amount
                )
                val requestPayload = SyncTransactionRequest(
                    apiKey = apiKey,
                    transactions = listOf(txPayload)
                )
                val response = webhookService.syncTransaction(syncUrl, requestPayload)
                return if (response.isSuccessful && response.body()?.status == "success") {
                    transactionDao.updateTransaction(transaction.copy(isSynced = true))
                    true
                } else {
                    transactionDao.updateTransaction(transaction.copy(isSynced = false))
                    false
                }
            } catch (e: Exception) {
                transactionDao.updateTransaction(transaction.copy(isSynced = false))
                return false
            }
        }
        return null
    }

    /**
     * Manually sync all unsynced transactions (For Phase 3).
     */
    suspend fun manualSyncUnsyncedTransactions() {
        val unsynced = transactionDao.getUnsyncedTransactions()
        if (unsynced.isEmpty()) return
        
        val syncUrl = settingsDataStore.syncUrl.first()
        val apiKey = settingsDataStore.apiKey.first()
        
        if (syncUrl.isBlank()) return // Cannot sync without URL
        
        try {
            val transactionsPayload = unsynced.map { tx ->
                val category = categoryDao.getCategoryById(tx.categoryId)
                TransactionPayload(
                    id = tx.transactionId,
                    category = category?.name ?: "Unknown",
                    remarks = tx.remarks,
                    nominal = tx.amount
                )
            }
            val requestPayload = SyncTransactionRequest(
                apiKey = apiKey,
                transactions = transactionsPayload
            )
            val response = webhookService.syncTransactionsBulk(syncUrl, requestPayload)
            if (response.isSuccessful && response.body()?.status == "success") {
                // Jika berhasil, update semua flag isSynced menjadi true
                appDatabase.withTransaction {
                    for (tx in unsynced) {
                        transactionDao.updateTransaction(tx.copy(isSynced = true))
                    }
                }
            }
        } catch (e: Exception) {
            // Error saat pengiriman bulk
        }
    }

    // --- Utility ---
    private fun getMonthAndYear(timestamp: Long): Pair<Int, Int> {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val month = calendar.get(Calendar.MONTH) + 1 // 1-based (Jan = 1, Dec = 12)
        val year = calendar.get(Calendar.YEAR)
        return Pair(month, year)
    }
}
