package com.example.pertracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertracker.data.model.Category
import com.example.pertracker.data.model.TransactionEntity
import com.example.pertracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: FinanceRepository) : ViewModel() {
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<com.example.pertracker.data.model.Goal>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTransaction(transaction: TransactionEntity, onResult: (Boolean?) -> Unit) {
        viewModelScope.launch {
            val syncResult = repository.insertTransaction(transaction)
            onResult(syncResult)
        }
    }
}
