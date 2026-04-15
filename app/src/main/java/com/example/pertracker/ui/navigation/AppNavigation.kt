package com.example.pertracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pertracker.ui.budget.BudgetScreen
import com.example.pertracker.ui.budget.BudgetViewModel
import com.example.pertracker.ui.category.CategoryScreen
import com.example.pertracker.ui.category.CategoryViewModel
import com.example.pertracker.ui.dashboard.DashboardScreen
import com.example.pertracker.ui.dashboard.DashboardViewModel
import com.example.pertracker.ui.settings.SettingsScreen
import com.example.pertracker.ui.settings.SettingsViewModel
import com.example.pertracker.ui.transaction.TransactionInputScreen
import com.example.pertracker.ui.transaction.TransactionViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Categories : Screen("categories")
    object Budgets : Screen("budgets")
    object AddTransaction : Screen("add_transaction")
    object Settings : Screen("settings")
    object Goals : Screen("goals")
    object Portfolio : Screen("portfolio")
    object Logs : Screen("logs")
    object AssetDetail : Screen("asset_detail/{assetId}") {
        fun createRoute(assetId: Long) = "asset_detail/$assetId"
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // --- Dashboard / Home ---
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = koinViewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToBudgets = { navController.navigate(Screen.Budgets.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToPortfolio = { navController.navigate(Screen.Portfolio.route) },
                onNavigateToLogs = { navController.navigate(Screen.Logs.route) }
            )
        }

        // --- Categories ---
        composable(Screen.Categories.route) {
            val viewModel: CategoryViewModel = koinViewModel()
            CategoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Budgets ---
        composable(Screen.Budgets.route) {
            val viewModel: BudgetViewModel = koinViewModel()
            BudgetScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Add Transaction ---
        composable(Screen.AddTransaction.route) {
            val viewModel: TransactionViewModel = koinViewModel()
            TransactionInputScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Settings ---
        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = koinViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Goals ---
        composable(Screen.Goals.route) {
            val viewModel: com.example.pertracker.ui.goal.GoalViewModel = koinViewModel()
            com.example.pertracker.ui.goal.GoalScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Portfolio ---
        composable(Screen.Portfolio.route) {
            val viewModel: com.example.pertracker.ui.portfolio.PortfolioViewModel = koinViewModel()
            com.example.pertracker.ui.portfolio.PortfolioScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { assetId ->
                    navController.navigate(Screen.AssetDetail.createRoute(assetId))
                }
            )
        }

        // --- Asset Detail ---
        composable(
            route = Screen.AssetDetail.route,
            arguments = listOf(navArgument("assetId") { type = NavType.LongType })
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getLong("assetId") ?: return@composable
            val viewModel: com.example.pertracker.ui.portfolio.PortfolioViewModel = koinViewModel()
            com.example.pertracker.ui.portfolio.AssetDetailScreen(
                viewModel = viewModel,
                assetId = assetId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Logs ---
        composable(Screen.Logs.route) {
            val viewModel: com.example.pertracker.ui.logs.LogsViewModel = koinViewModel()
            com.example.pertracker.ui.logs.LogsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
