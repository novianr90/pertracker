package com.example.pertracker.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertracker.data.model.TransactionEntity
import com.example.pertracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class FilterMode {
    ALL, MONTHLY, CUSTOM
}

data class TransactionLogItem(
    val transaction: TransactionEntity,
    val categoryName: String
)

data class FilterState(
    val mode: FilterMode = FilterMode.ALL,
    val month: Int,
    val year: Int,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null
)

class LogsViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val currentCalendar = Calendar.getInstance()
    
    // Store everything in a single state class to prevent "combine" type erasure with too many params
    val filterState = MutableStateFlow(
        FilterState(
            month = currentCalendar.get(Calendar.MONTH) + 1,
            year = currentCalendar.get(Calendar.YEAR)
        )
    )

    val activeFilterMode: StateFlow<FilterMode> = filterState.map { it.mode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), filterState.value.mode)

    val customStartDate: StateFlow<Long?> = filterState.map { it.customStartDate }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), filterState.value.customStartDate)

    val customEndDate: StateFlow<Long?> = filterState.map { it.customEndDate }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), filterState.value.customEndDate)

    val transactions: StateFlow<List<TransactionLogItem>> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories(),
        filterState
    ) { txs, categories, state ->
        val categoryMap = categories.associateBy { it.categoryId }
        
        val filteredTxs = when (state.mode) {
            FilterMode.ALL -> txs
            FilterMode.MONTHLY -> {
                txs.filter { tx ->
                    val cal = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
                    val txMonth = cal.get(Calendar.MONTH) + 1
                    val txYear = cal.get(Calendar.YEAR)
                    txMonth == state.month && txYear == state.year
                }
            }
            FilterMode.CUSTOM -> {
                txs.filter { tx ->
                    val afterStart = state.customStartDate?.let { tx.transactionDate >= it } ?: true
                    val beforeEnd = state.customEndDate?.let { tx.transactionDate <= (it + 86400000L - 1) } ?: true
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
        filterState.value = filterState.value.copy(mode = mode)
    }

    fun setMonthlyFilter(month: Int, year: Int) {
        filterState.value = filterState.value.copy(mode = FilterMode.MONTHLY, month = month, year = year)
    }

    fun setCustomDateFilter(start: Long?, end: Long?) {
        filterState.value = filterState.value.copy(mode = FilterMode.CUSTOM, customStartDate = start, customEndDate = end)
    }
}
