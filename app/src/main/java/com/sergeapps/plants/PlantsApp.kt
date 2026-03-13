package com.sergeapps.plants

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
import com.sergeapps.plants.ui.inventory.InventoryScreen
import com.sergeapps.plants.ui.inventory.InventoryDetailScreen
import com.sergeapps.plants.ui.location.LocationsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sergeapps.plants.vm.control.ControlViewModel
import androidx.compose.runtime.collectAsState

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

        composable(route = Routes.Control) {
            val controlViewModel: ControlViewModel = viewModel()
            val state = controlViewModel.state.collectAsState().value

            ControlScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onControllerChange = controlViewModel::onControllerChange,
                onZoneChange = controlViewModel::onZoneChange,
                onAddSchedule = controlViewModel::addScheduleRow,
                onDeleteSchedule = controlViewModel::deleteScheduleRow,
                onScheduleChange = controlViewModel::updateScheduleRow,
                onSearchHistory = controlViewModel::onSearchHistory,
                onWaterClick = controlViewModel::onWaterClick,
                onFeedClick = controlViewModel::onFeedClick
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

