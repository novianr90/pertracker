package com.example.pertracker.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertracker.data.model.AssetEntity
import com.example.pertracker.data.repository.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortfolioViewModel(private val repository: FinanceRepository) : ViewModel() {

    val isSyariahOnly = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val assets: StateFlow<List<AssetEntity>> = isSyariahOnly.flatMapLatest { syariahOnly ->
        if (syariahOnly) repository.getSyariahAssets() else repository.getAllAssets()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalNetWorth: StateFlow<Double?> = repository.getTotalNetWorth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun toggleSyariahFilter() {
        isSyariahOnly.value = !isSyariahOnly.value
    }

    fun saveAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.insertAsset(asset)
        }
    }

    fun updateAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.updateAsset(asset)
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }
}
