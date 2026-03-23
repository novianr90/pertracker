package com.example.pertracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertracker.data.model.CategoryType
import com.example.pertracker.data.model.Goal
import com.example.pertracker.data.model.TransactionEntity
import com.example.pertracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class MonthlySummary(
    val month: Int,
    val year: Int,
    val totalExpense: Double,
    val totalIncome: Double
)

data class PieData(val categoryName: String, val amount: Float)

data class TodaySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val pieData: List<PieData>
)

data class TransactionWithCategory(
    val transaction: TransactionEntity,
    val categoryName: String
)

class DashboardViewModel(private val repository: FinanceRepository) : ViewModel() {
    
    // Original flows
    val goals: StateFlow<List<Goal>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySummary: StateFlow<List<MonthlySummary>> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories()
    ) { transactions, categories ->
        val categoryMap = categories.associateBy { it.categoryId }
        transactions.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
            Pair(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
        }.map { (period, txs) ->
            var income = 0.0
            var expense = 0.0
            for (tx in txs) {
                val type = categoryMap[tx.categoryId]?.type
                if (type == CategoryType.INCOME) income += tx.amount
                else if (type == CategoryType.EXPENSE) expense += tx.amount
            }
            MonthlySummary(period.first, period.second, expense, income)
        }.sortedWith(compareByDescending<MonthlySummary> { it.year }.thenByDescending { it.month })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // New flows for the revamped Dashboard UI
    val todaySummary: StateFlow<TodaySummary> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories()
    ) { transactions, categories ->
        val today = Calendar.getInstance()
        val categoryMap = categories.associateBy { it.categoryId }
        
        var totalIncome = 0.0
        var totalExpense = 0.0
        val pieDataMap = mutableMapOf<String, Float>()
        
        for (tx in transactions) {
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
            if (today.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) && 
                today.get(Calendar.DAY_OF_YEAR) == txCal.get(Calendar.DAY_OF_YEAR)) {
                
                val category = categoryMap[tx.categoryId]
                val type = category?.type
                val name = category?.name ?: "Unknown"
                
                if (type == CategoryType.INCOME) {
                    totalIncome += tx.amount
                } else if (type == CategoryType.EXPENSE) {
                    totalExpense += tx.amount
                    pieDataMap[name] = (pieDataMap[name] ?: 0f) + tx.amount.toFloat()
                }
            }
        }
        
        TodaySummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            pieData = pieDataMap.map { PieData(it.key, it.value) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodaySummary(0.0, 0.0, emptyList()))

    val topRecentTransactions: StateFlow<List<TransactionWithCategory>> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories()
    ) { transactions, categories ->
        val categoryMap = categories.associateBy { it.categoryId }
        transactions.sortedByDescending { it.transactionDate }
            .take(3)
            .map { tx ->
                TransactionWithCategory(
                    transaction = tx,
                    categoryName = categoryMap[tx.categoryId]?.name ?: "Unknown"
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
