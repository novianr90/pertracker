package com.example.pertracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertracker.data.datastore.SettingsDataStore
import com.example.pertracker.data.model.TransactionEntity
import com.example.pertracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: FinanceRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    val isAutoSyncEnabled: StateFlow<Boolean> = settingsDataStore.isAutoSyncEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _unsyncedTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val unsyncedTransactions: StateFlow<List<TransactionEntity>> = _unsyncedTransactions

    init {
        loadUnsyncedTransactions()
    }

    fun loadUnsyncedTransactions() {
        viewModelScope.launch {
            _unsyncedTransactions.value = repository.getUnsyncedTransactions()
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoSyncEnabled(enabled)
        }
    }

    fun manualSync(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.manualSyncUnsyncedTransactions()
            loadUnsyncedTransactions()
            onComplete()
        }
    }
}
