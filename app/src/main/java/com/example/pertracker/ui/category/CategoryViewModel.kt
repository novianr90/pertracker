package com.example.pertracker.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertracker.data.model.Category
import com.example.pertracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: FinanceRepository) : ViewModel() {
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            if (category.categoryId == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
        }
    }

    fun deleteCategory(category: Category, onError: (String) -> Unit) {
        if (category.isSystemDefault) {
            onError("Cannot delete system default category.")
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete category.")
            }
        }
    }
}
