package com.example.pertracker.di

import androidx.room.Room
import com.example.pertracker.data.datastore.SettingsDataStore
import com.example.pertracker.data.db.AppDatabase
import com.example.pertracker.data.network.WebhookService
import com.example.pertracker.data.repository.FinanceRepository
import com.example.pertracker.ui.budget.BudgetViewModel
import com.example.pertracker.ui.category.CategoryViewModel
import com.example.pertracker.ui.dashboard.DashboardViewModel
import com.example.pertracker.ui.settings.SettingsViewModel
import com.example.pertracker.ui.transaction.TransactionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    
    // Remote
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://example.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    single<WebhookService> {
        get<Retrofit>().create(WebhookService::class.java)
    }

    // Local DB
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "finance_database")
            .build()
    }
    
    // DAOs
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().budgetDao() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().assetDao() }

    // DataStore
    single { SettingsDataStore(androidContext()) }

    // Repository
    single {
        FinanceRepository(
            appDatabase = get(),
            categoryDao = get(),
            transactionDao = get(),
            budgetDao = get(),
            goalDao = get(),
            settingsDataStore = get(),
            webhookService = get(),
            assetDao = get()
        )
    }

    // ViewModels
    viewModel { DashboardViewModel(get()) }
    viewModel { CategoryViewModel(get()) }
    viewModel { BudgetViewModel(get()) }
    viewModel { TransactionViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { com.example.pertracker.ui.portfolio.PortfolioViewModel(get()) }
    viewModel { com.example.pertracker.ui.goal.GoalViewModel(get()) }
    viewModel { com.example.pertracker.ui.logs.LogsViewModel(get()) }
}
