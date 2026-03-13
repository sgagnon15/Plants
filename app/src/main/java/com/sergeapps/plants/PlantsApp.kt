package com.sergeapps.plants

import android.service.controls.Control
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sergeapps.plants.ui.HomeScreen
import com.sergeapps.plants.ui.item.ItemDetailScreen
import com.sergeapps.plants.ui.item.ItemsListScreen
import com.sergeapps.plants.ui.SettingsScreen
import com.sergeapps.plants.ui.batchtransfer.BatchTransferScreen
import com.sergeapps.plants.ui.control.ControlScreen
import com.sergeapps.plants.ui.control.ControlUiState
import com.sergeapps.plants.ui.control.GeneralParamsUi
import com.sergeapps.plants.ui.control.HistoryRowUi
import com.sergeapps.plants.ui.control.ScheduleRowUi
import com.sergeapps.plants.ui.inventory.InventoryScreen
import com.sergeapps.plants.ui.inventory.InventoryDetailScreen
import com.sergeapps.plants.ui.location.LocationsScreen

@Composable
fun plantsApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable(Routes.Home) {
            HomeScreen(
                onOpenItems = { navController.navigate(Routes.ItemsList) },
                onOpenInventory = { navController.navigate(Routes.Inventory) },
                onOpenLocations = { navController.navigate(Routes.Locations) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenBatchTransfer = { navController.navigate(Routes.BatchTransfer) },
                onOpenControl = { navController.navigate(Routes.Control) }
            )
        }

        // ---- ITEMS ----

        composable(Routes.ItemsList) {
            ItemsListScreen(
                onBack = { navController.popBackStack() },
                onOpenItem = { itemId ->
                    navController.navigate(Routes.itemDetail(itemId))
                }
            )
        }

        composable(
            route = Routes.ItemDetailRoute,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("id") ?: 0
            ItemDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onOpenInventoryDetail = { stockId -> navController.navigate("${Routes.InventoryDetail}/$stockId") },
                onAddToInventory = { itemNumber -> navController.navigate("${Routes.InventoryDetail}/0?itemNumber=$itemNumber") }
            )
        }

        // ---- INVENTORY ----

        composable(Routes.Inventory) {
            InventoryScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { stockId ->
                    navController.navigate("${Routes.InventoryDetail}/$stockId")
                }
            )
        }

        composable(
            route = "${Routes.InventoryDetail}/{stockId}?itemNumber={itemNumber}",
            arguments = listOf(
                navArgument("stockId") {
                    type = NavType.IntType
                },
                navArgument("itemNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val stockId = backStackEntry.arguments?.getInt("stockId") ?: 0
            val itemNumber = backStackEntry.arguments?.getString("itemNumber").orEmpty()

            InventoryDetailScreen(
                stockId = stockId,
                initialItemNumber = itemNumber,
                onBack = { navController.popBackStack() },
                onOpenItemDetail = { itemId ->
                    navController.navigate("${Routes.ItemDetail}/$itemId")
                }
            )
        }

        // ---- LOCATIONS ----

        composable(Routes.Locations) {
            LocationsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Routes.BatchTransfer) {
            BatchTransferScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ---- CONTROL ----

        composable("control") {
            val state = ControlUiState(
                zone = "Zone 1",
                availableZones = listOf("Zone 1", "Zone 2", "Zone 3"),
                waterFlow = "0.00",
                scheduleRows = listOf(
                    ScheduleRowUi(id = 1, startTime = "08:00", duration = "30", fertilizer = false),
                    ScheduleRowUi(id = 2, startTime = "18:00", duration = "20", fertilizer = true)
                ),
                historyRows = listOf(
                    HistoryRowUi("2026-03-12", "Arrosage", "120 ml"),
                    HistoryRowUi("2026-03-11", "Fertilisation", "50 ml"),
                    HistoryRowUi("2026-03-10", "Arrosage", "110 ml")
                ),
                generalParams = GeneralParamsUi(
                    autoStart = "07:00",
                    waterDuration = "30 sec",
                    manualDuration = "20 sec",
                    feedEnabled = true,
                    feedDuration = "10 sec",
                    updateFrequency = "15 min"
                )
            )

            ControlScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onZoneChange = {},
                onAddSchedule = {},
                onDeleteSchedule = {},
                onScheduleChange = { _, _ -> },
                onSearchHistory = {}
            )
        }

        // ---- SETTINGS ----

        composable(Routes.Settings) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

object Routes {
    const val Home = "home"
    const val ItemsList = "items_list"
    const val Settings = "settings"

    const val ItemDetail = "item_detail"
    const val ItemDetailRoute = "item_detail/{id}"

    const val Inventory = "inventory"
    const val Locations = "locations"
    const val InventoryDetail = "inventoryDetail"
    const val BatchTransfer = "batch_transfer"
    const val Control = "control"

    fun itemDetail(id: Int): String = "$ItemDetail/$id"

    fun inventoryDetail(stockId: Int, itemNumber: Int): String {
        return "$InventoryDetail/$stockId?itemnumber=$itemNumber"
    }
}

