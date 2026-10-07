package com.example.stockinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.stockinventory.data.local.StockDatabase
import com.example.stockinventory.data.repository.StockRepository
import com.example.stockinventory.ui.StockViewModel
import com.example.stockinventory.ui.StockViewModelFactory
import com.example.stockinventory.ui.screens.*
import com.example.stockinventory.ui.theme.StockInventoryTheme
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Inventory : Screen("inventory")
    data object Transactions : Screen("transactions")
    data object Scanner : Screen("scanner")
    data class ItemDetail(val itemId: Long) : Screen("item_detail")
    data class AddEditItem(val itemId: Long? = null) : Screen("add_edit_item")
}

data class NavItem(
    val title: String,
    val iconRes: Int,
    val screen: Screen
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = StockDatabase.getDatabase(applicationContext)
        val repository = StockRepository(database.stockDao())

        setContent {
            StockInventoryTheme {
                val viewModel: StockViewModel = viewModel(
                    factory = StockViewModelFactory(repository)
                )

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.userMessage.collectLatest { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

                val navItems = listOf(
                    NavItem("Dashboard", R.drawable.ic_inventory, Screen.Dashboard),
                    NavItem("Inventory", R.drawable.ic_inventory, Screen.Inventory),
                    NavItem("Scan", R.drawable.ic_scanner, Screen.Scanner),
                    NavItem("History", R.drawable.ic_receipt, Screen.Transactions)
                )

                val isBottomBarVisible = currentScreen is Screen.Dashboard ||
                        currentScreen is Screen.Inventory ||
                        currentScreen is Screen.Scanner ||
                        currentScreen is Screen.Transactions

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (isBottomBarVisible) {
                            NavigationBar {
                                navItems.forEach { navItem ->
                                    val isSelected = currentScreen.route == navItem.screen.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = navItem.screen },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = navItem.iconRes),
                                                contentDescription = navItem.title
                                            )
                                        },
                                        label = { Text(navItem.title) }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (val screen = currentScreen) {
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToInventory = { currentScreen = Screen.Inventory },
                                onNavigateToItemDetail = { id -> currentScreen = Screen.ItemDetail(id) },
                                onNavigateToAddItem = { currentScreen = Screen.AddEditItem() },
                                onNavigateToScanner = { currentScreen = Screen.Scanner },
                                onNavigateToTransactions = { currentScreen = Screen.Transactions },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Inventory -> {
                            InventoryListScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { id -> currentScreen = Screen.ItemDetail(id) },
                                onNavigateToAddItem = { currentScreen = Screen.AddEditItem() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Transactions -> {
                            TransactionsScreen(
                                viewModel = viewModel,
                                onNavigateToItemDetail = { id -> currentScreen = Screen.ItemDetail(id) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Scanner -> {
                            ScannerScreen(
                                viewModel = viewModel,
                                onNavigateToItemDetail = { id -> currentScreen = Screen.ItemDetail(id) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.ItemDetail -> {
                            ItemDetailScreen(
                                itemId = screen.itemId,
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Inventory },
                                onNavigateToEdit = { id -> currentScreen = Screen.AddEditItem(id) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.AddEditItem -> {
                            AddEditItemScreen(
                                itemId = screen.itemId,
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Inventory },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
