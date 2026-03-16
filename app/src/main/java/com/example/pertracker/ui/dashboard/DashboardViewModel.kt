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

class DashboardViewModel(private val repository: FinanceRepository) : ViewModel() {
    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .map { it.take(10) } // Get latest 10
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}
