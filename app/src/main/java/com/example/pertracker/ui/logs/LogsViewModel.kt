package com.example.pertracker.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertracker.data.model.TransactionEntity
import com.example.pertracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class FilterMode {
    ALL, MONTHLY, CUSTOM
}

data class TransactionLogItem(
    val transaction: TransactionEntity,
    val categoryName: String
)

class LogsViewModel(private val repository: FinanceRepository) : ViewModel() {

    val activeFilterMode = MutableStateFlow(FilterMode.ALL)
    
    // For Monthly filter (Default to current month/year)
    private val currentCalendar = Calendar.getInstance()
    val selectedMonth = MutableStateFlow(currentCalendar.get(Calendar.MONTH) + 1) // 1-based
    val selectedYear = MutableStateFlow(currentCalendar.get(Calendar.YEAR))

    // For Custom Date filter
    val customStartDate = MutableStateFlow<Long?>(null)
    val customEndDate = MutableStateFlow<Long?>(null)

    val transactions: StateFlow<List<TransactionLogItem>> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories(),
        activeFilterMode,
        selectedMonth,
        selectedYear,
        customStartDate,
        customEndDate
    ) { txs, categories, mode, month, year, startDate, endDate ->
        val categoryMap = categories.associateBy { it.categoryId }
        
        val filteredTxs = when (mode) {
            FilterMode.ALL -> txs
            FilterMode.MONTHLY -> {
                txs.filter { tx ->
                    val cal = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
                    val txMonth = cal.get(Calendar.MONTH) + 1
                    val txYear = cal.get(Calendar.YEAR)
                    txMonth == month && txYear == year
                }
            }
            FilterMode.CUSTOM -> {
                txs.filter { tx ->
                    val afterStart = startDate?.let { tx.transactionDate >= it } ?: true
                    // To include the whole end date, we normally add 24 hours to it conceptually if it's start-of-day.
                    // But usually, the date picker returns start of day for both. Let's just do a basic <= comparison for now.
                    val beforeEnd = endDate?.let { tx.transactionDate <= (it + 86400000L - 1) } ?: true
                    afterStart && beforeEnd
                }
            }
        }
        
        filteredTxs.map { tx ->
            TransactionLogItem(
                transaction = tx,
                categoryName = categoryMap[tx.categoryId]?.name ?: "Unknown"
            )
        }.sortedByDescending { it.transaction.transactionDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilterMode(mode: FilterMode) {
        activeFilterMode.value = mode
    }

    fun setMonthlyFilter(month: Int, year: Int) {
        selectedMonth.value = month
        selectedYear.value = year
        setFilterMode(FilterMode.MONTHLY)
    }

    fun setCustomDateFilter(start: Long?, end: Long?) {
        customStartDate.value = start
        customEndDate.value = end
        setFilterMode(FilterMode.CUSTOM)
    }
}
