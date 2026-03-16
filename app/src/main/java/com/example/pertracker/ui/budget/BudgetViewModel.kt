package com.example.pertracker.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertracker.data.model.Budget
import com.example.pertracker.data.model.Category
import com.example.pertracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetViewModel(private val repository: FinanceRepository) : ViewModel() {
    val budgets: StateFlow<List<Budget>> = repository.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveBudget(budget: Budget) {
        viewModelScope.launch {
            val existing = repository.getBudget(budget.categoryId, budget.month, budget.year)
            if (existing != null) {
                repository.updateBudget(existing.copy(plannedAmount = budget.plannedAmount))
            } else {
                repository.insertBudget(budget)
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }
}
