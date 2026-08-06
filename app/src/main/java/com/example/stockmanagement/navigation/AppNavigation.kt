package com.example.stockmanagement.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockmanagement.ui.screen.HomeScreen
import com.example.stockmanagement.ui.screen.ManageDataScreen
import com.example.stockmanagement.ui.screen.ManageMasterScreen
import com.example.stockmanagement.ui.screen.SearchScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onSearchClick = {
                    navController.navigate("search")
                },
                onManageMasterClick = {
                    navController.navigate("manage_master")
                },
                onManageDataClick = {
                    navController.navigate("manage_data")
                },
            )
        }

        composable("search") {
            SearchScreen()
        }
        composable("manage_master") {
            ManageMasterScreen()
        }
        composable("manage_data") {
            ManageDataScreen()
        }
    }
}
