# PerTracker (Personal Finance Tracker) Codebase Context

This document serves as an overview of the PerTracker app. Use this context to understand the app's architecture, feature set, and database patterns.

## 🎯 App Overview
PerTracker is an offline-first Android application for managing personal finances. It tracks categories, budgets, transactions, financial goals, and asset wealth portfolios. A key feature is its "Outbox Pattern" synchronization, which allows the user to record transactions offline and sync them to an external webhook when desired.

## 🛠️ Technology Stack
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3)
* **Architecture:** MVVM + Clean Architecture principles
* **Local Storage:** Jetpack Room (with KSP)
* **Preferences:** Jetpack DataStore (`SettingsDataStore`)
* **Dependency Injection:** Koin (`AppModule.kt`)
* **Networking:** Retrofit & OkHttp (for Webhook syncing)

## 📁 Project Structure (`app/src/main/java/com/example/pertracker/`)

### 1. `data/`
Contains everything related to data handling and external storage.
*   **`model/`**: Room Entities (`Budget`, `Category`, `Goal`, `TransactionEntity`, `AssetEntity`).
*   **`dao/`**: Room Data Access Objects (`BudgetDao`, `CategoryDao`, `GoalDao`, `TransactionDao`, `AssetDao`).
*   **`db/`**: `AppDatabase` (ties all DAOs and Entities together).
*   **`datastore/`**: `SettingsDataStore` (Preferences like Auto-Sync, API Key, Webhook URL).
*   **`network/`**: `WebhookService`, defining network payloads (`TransactionPayload`, `SyncTransactionRequest`).
*   **`repository/`**: `FinanceRepository` serves as the single source of truth. It wraps all DAOs and encapsulates business rules.

### 2. `di/`
*   **`AppModule.kt`**: Koin module defining singletons for Retrofit, Room DB, DAOs, DataStore, Repositories, and instantiating all ViewModels.

### 3. `ui/`
Organized by feature. Each package contains the Compose UI (`Screen`) and its corresponding `ViewModel`.
*   **`dashboard/`**: Aggregates data, displays monthly budget usage, recent transactions, goal progress, and houses a 3x2 navigation grid.
*   **`transaction/`**: Handles transaction entry and listing (supports dynamic Goal Funding dropdowns).
*   **`budget/`**: Manages budget limits per category (Month/Year).
*   **`category/`**: Core master data management (creating income/expense/transfer categories).
*   **`portfolio/`**: Investment & Wealth tracker showing Net Worth and Unrealized P/L per asset (with Syariah filters).
*   **`settings/`**: Controls UI for User Preferences (API keys, Sync behavior, Outbox manual sync triggers).
*   **`navigation/`**: Centralized compose navigation setup linking all screens.

## 🔄 Core Business Logic & Patterns

1.  **Denormalized Budgets**: When a new `TransactionEntity` is saved, the `FinanceRepository.insertTransaction` method automatically intercepts it and updates the corresponding `Budget.currentAmount`. **Always keep atomic budget tracking logic inside `FinanceRepository` using `withTransaction` blocks.**
2.  **Offline-first Syncing (Outbox)**: Transactions initially save with `isSynced = false`. The repository handles syncing them to the webhook (either manually in batch or automatically on saving).
3.  **Dependency Injection**: Koin passes `FinanceRepository` into all ViewModels. Do not instantiate the repository manually.
4.  **Flows & State**: The DAOs return `Flow` types. The Repository exposes these flows to the ViewModel, which in turn converts them to `StateFlow` for Compose to observe reactively.
5.  **UI State Handling During Sync**: When transactions are auto-syncing (via `FinanceRepository.insertTransaction` which suspends and returns a sync result), the Compose UI must track a local submission state (e.g., `isSubmitting`) to disable action buttons. This prevents double-clicks and perceived UI freezes. A `Snackbar` handles the result asynchronously, while ensuring transactions save smoothly offline (`isSynced = false`) on network failure or flag successfully (`isSynced = true`) on completion.
6.  **Dynamic Goal Funding**: When a transaction is categorized as `TRANSFER_GOAL`, `FinanceRepository` natively intercepts this to atomically update the `Goal.currentAmount` securely within a Room `withTransaction` block.
7.  **Wealth & Portfolio Logic**: The app supports tracking unrealized Profit & Loss across multiple asset types (Stock, Mutual Fund, Sukuk, Cash, Gold). To uphold Clean Architecture, calculation logics like `getUnrealizedProfitAmount()` live as extension functions on `AssetEntity`, enabling native `Flow` transformations.

## 🌊 Typical User Flow

Here is the core lifecycle of how data flows during typical usage of the app:

1.  **User Creates Category**: The user navigates to the Category screen and creates a custom Income or Expense category (e.g., "Groceries", "Salary").
2.  **User Creates Budget by Category**: In the Budget screen, the user sets a target monthly limit for a specific category.
3.  **User Adds Transactions**: The user records daily transactions. As a transaction is inserted, the `FinanceRepository` automatically aggregates and updates the `currentAmount` of the corresponding budget.
4.  **Sync Transactions (Auto or Manual)**: 
    *   **Auto-Sync**: If enabled in Settings (with valid API Key and Webhook URL), the transaction immediately attempts to sync to the remote server.
    *   **Manual-Sync**: If auto-sync is off or network is unavailable, the offline data accumulates with an `isSynced = false` flag. The user can manually push all unsynced data from the Settings screen via an Outbox bulk sync approach.
