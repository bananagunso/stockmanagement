package com.example.stockmanagement.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockmanagement.data.database.DatabaseProvider
import com.example.stockmanagement.ui.screen.HomeScreen
import com.example.stockmanagement.ui.screen.ManageDataScreen
import com.example.stockmanagement.ui.screen.ManageMasterScreen
import com.example.stockmanagement.ui.screen.SearchScreen
import com.example.stockmanagement.ui.screen.CategoryListScreen
import com.example.stockmanagement.viewmodel.CategoryViewModel
import com.example.stockmanagement.viewmodel.CategoryViewModelFactory
import com.example.stockmanagement.ui.screen.DataTypeListScreen
import com.example.stockmanagement.viewmodel.DataTypeViewModel
import com.example.stockmanagement.viewmodel.DataTypeViewModelFactory
import com.example.stockmanagement.ui.screen.AttributeListScreen
import com.example.stockmanagement.ui.screen.CategoryAttributeListScreen
import com.example.stockmanagement.viewmodel.AttributeViewModel
import com.example.stockmanagement.viewmodel.AttributeViewModelFactory
import com.example.stockmanagement.viewmodel.CategoryAttributeViewModel
import com.example.stockmanagement.viewmodel.CategoryAttributeViewModelFactory

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
                    navController.navigate("manageMaster")
                },
                onManageDataClick = {
                    navController.navigate("manageData")
                },
            )
        }

        composable("search") {
            SearchScreen()
        }
        composable("manageMaster") {
            ManageMasterScreen(
                onCategoryListClick = {
                    navController.navigate("categoryList")
                },
                onAttributeListClick = {
                    navController.navigate("attributeList")
                },
                onCategoryAttributeListClick = {
                    navController.navigate("categoryattributeList")
                },
                onDataTypeListClick = {
                    navController.navigate("datatypeList")
                },
            )
        }

        composable("manageData") {
            ManageDataScreen()
        }
        composable("categoryList") {
            val context = LocalContext.current
            val database = DatabaseProvider.getDatabase(context)
            val factory = CategoryViewModelFactory(
                database.categoryDao()
            )
            val viewModel: CategoryViewModel = viewModel(
                factory = factory
            )
            CategoryListScreen(viewModel)
        }

        composable("AttributeList") {
            val context = LocalContext.current
            val database = DatabaseProvider.getDatabase(context)
            val factory = AttributeViewModelFactory(
                database.attributeDao(),
                database.dataTypeDao(),
            )
            val viewModel: AttributeViewModel = viewModel(
                factory = factory
            )
            AttributeListScreen(viewModel)
        }

        composable("CategoryAttributeList") {
            val context = LocalContext.current
            val database = DatabaseProvider.getDatabase(context)
            val factory = CategoryAttributeViewModelFactory(
                database.categoryAttributeDao(),
                database.categoryDao(),
                database.attributeDao(),
            )
            val viewModel: CategoryAttributeViewModel = viewModel(
                factory = factory
            )
            CategoryAttributeListScreen(viewModel)
        }


        composable("DataTypeList") {
            val context = LocalContext.current
            val database = DatabaseProvider.getDatabase(context)
            val factory = DataTypeViewModelFactory(
                database.dataTypeDao()
            )
            val viewModel: DataTypeViewModel = viewModel(
                factory = factory
            )
            DataTypeListScreen(viewModel)
        }
    }
}
