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
                onOpenSettings = { navController.navigate(Routes.Settings) }
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
            "${Routes.InventoryDetail}/{stockId}?itemNumber={itemNumber}",
            arguments = listOf(
                navArgument("stockId") { type = NavType.IntType },
                navArgument("itemNumber") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val stockId = backStackEntry.arguments?.getInt("stockId") ?: 0
            val itemNumber = backStackEntry.arguments?.getInt("itemNumber") ?: 0

            InventoryDetailScreen(
                stockId = stockId,
                initialItemNumber = itemNumber,
                onBack = { navController.popBackStack() },
                onOpenItemDetail = { itemId ->
                    navController.navigate(Routes.itemDetail(itemId))
                }
            )
        }

        // ---- LOCATIONS ----

        composable(Routes.Locations) {
            LocationsScreen(
                onBack = { navController.popBackStack() }
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

    fun itemDetail(id: Int): String = "$ItemDetail/$id"

    fun inventoryDetail(stockId: Int, itemNumber: Int): String {
        return "$InventoryDetail/$stockId?itemnumber=$itemNumber"
    }
}

