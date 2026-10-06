package com.example.stockmanagement.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.runBlocking
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stockmanagement.data.database.DatabaseProvider
import com.example.stockmanagement.ui.screen.HomeScreen
import com.example.stockmanagement.ui.screen.ManageDataScreen
import com.example.stockmanagement.ui.screen.ManageMasterScreen
import com.example.stockmanagement.ui.screen.SearchItemScreen
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
import com.example.stockmanagement.viewmodel.ItemViewModel
import com.example.stockmanagement.viewmodel.ItemViewModelFactory
import com.example.stockmanagement.ui.screen.ItemDetailScreen
import com.example.stockmanagement.viewmodel.ItemAttributeValueViewModel
import com.example.stockmanagement.viewmodel.ItemAttributeValueViewModelFactory

import com.example.stockmanagement.viewmodel.DataManagementViewModel
import com.example.stockmanagement.viewmodel.DataManagementViewModelFactory

import com.example.stockmanagement.ui.screen.DatabaseSelectionScreen
import com.example.stockmanagement.viewmodel.DatabaseSettingsViewModel
import com.example.stockmanagement.viewmodel.DatabaseSettingsViewModelFactory

import com.example.stockmanagement.ui.screen.LoginScreen
import com.example.stockmanagement.viewmodel.AuthViewModel
import com.example.stockmanagement.viewmodel.AuthViewModelFactory
import com.example.stockmanagement.data.network.NetworkModule
import com.example.stockmanagement.util.TokenManager

import com.example.stockmanagement.data.repository.SyncRepository
import com.example.stockmanagement.ui.screen.UserManagementScreen
import com.example.stockmanagement.viewmodel.SyncViewModel
import com.example.stockmanagement.viewmodel.SyncViewModelFactory
import com.example.stockmanagement.viewmodel.UserManagementViewModel
import com.example.stockmanagement.viewmodel.UserManagementViewModelFactory

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val isLoggedIn = remember { mutableStateOf(tokenManager.getToken() != null) }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn.value) "home" else "login"
    ) {
        composable("login") {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(NetworkModule.getApiService(), tokenManager)
            )
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    isLoggedIn.value = true
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val database = DatabaseProvider.getDatabase(context)
            val masterDb = DatabaseProvider.getMasterDatabase(context)
            val syncRepository = remember(database) {
                SyncRepository(
                    context = context,
                    database = database,
                    apiService = NetworkModule.getApiService(),
                    tokenManager = tokenManager
                )
            }
            val syncViewModel: SyncViewModel = viewModel(
                factory = SyncViewModelFactory(syncRepository)
            )
            val dbViewModel: DatabaseSettingsViewModel = viewModel(
                factory = DatabaseSettingsViewModelFactory(masterDb, NetworkModule.getApiService(), tokenManager)
            )

            HomeScreen(
                syncViewModel = syncViewModel,
                dbViewModel = dbViewModel,
                userEmail = tokenManager.getEmail() ?: "",
                userRole = tokenManager.getRole(),
                onSearchItemClick = {
                    navController.navigate("searchItem")
                },
                onManageMasterClick = {
                    navController.navigate("manageMaster")
                },
                onManageDataClick = {
                    navController.navigate("manageDatabase")
                },
                onSwitchDatabaseClick = {
                    navController.navigate("databaseSelection")
                },
                onManageUserClick = {
                    navController.navigate("userManagement")
                },
                onLogoutClick = {
                    syncViewModel.logout(
                        context = context,
                        database = database,
                        tokenManager = tokenManager,
                        onLoggedOut = {
                            isLoggedIn.value = false
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            )
        }

        composable("databaseSelection") {
            val context = LocalContext.current
            val database = DatabaseProvider.getMasterDatabase(context)
            val viewModel: DatabaseSettingsViewModel = viewModel(
                factory = DatabaseSettingsViewModelFactory(
                    database,
                    NetworkModule.getApiService(),
                    TokenManager(context)
                )
            )
            DatabaseSelectionScreen(
                viewModel = viewModel,
                onDatabaseSwitched = {
                    // 全画面をリフレッシュするためにホームに戻す
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("searchItem") {
            val context = LocalContext.current
            val database = DatabaseProvider.getDatabase(context)

            val categoryViewModel: CategoryViewModel = viewModel(
                factory = CategoryViewModelFactory(
                    database,
                    database.categoryDao(),
                    database.categoryAttributeDao()
                )
            )

            val itemViewModel: ItemViewModel = viewModel(
                factory = ItemViewModelFactory(
                    database,
                    database.itemDao()
                )
            )

            val categories by categoryViewModel.categories.collectAsState()

            SearchItemScreen(
                viewModel = itemViewModel,
                categories = categories,
                onItemClick = { itemId ->
                    navController.navigate("itemDetail/$itemId")
                }
            )
        }

        composable(
            route = "itemDetail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
            val context = LocalContext.current
            val database = DatabaseProvider.getDatabase(context)

            val itemViewModel: ItemViewModel = viewModel(
                factory = ItemViewModelFactory(database, database.itemDao())
            )
            val attributeValueViewModel: ItemAttributeValueViewModel = viewModel(
                factory = ItemAttributeValueViewModelFactory(
                    database.itemAttributeValueDao(),
                    database.categoryAttributeDao()
                )
            )
            val categoryViewModel: CategoryViewModel = viewModel(
                factory = CategoryViewModelFactory(
                    database,
                    database.categoryDao(),
                    database.categoryAttributeDao()
                )
            )
            val categoryAttributeViewModel: CategoryAttributeViewModel = viewModel(
                factory = CategoryAttributeViewModelFactory(
                    database.categoryAttributeDao(),
                    database.categoryDao(),
                    database.attributeDao()
                )
            )

            ItemDetailScreen(
                itemId = itemId,
                itemViewModel = itemViewModel,
                attributeValueViewModel = attributeValueViewModel,
                categoryAttributeViewModel = categoryAttributeViewModel,
                categories = categoryViewModel.categories.collectAsState().value,
                onBack = { navController.popBackStack() }
            )
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

        composable("manageDatabase") {
            val context = LocalContext.current
            val database = DatabaseProvider.getDatabase(context)
            val viewModel: DataManagementViewModel = viewModel(
                factory = DataManagementViewModelFactory(database)
            )
            ManageDataScreen(viewModel)
        }
        composable("categoryList") {
            val context = LocalContext.current
            val database = DatabaseProvider.getDatabase(context)
            val factory = CategoryViewModelFactory(
                database,
                database.categoryDao(),
                database.categoryAttributeDao()
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
                database = database,
                attributeDao = database.attributeDao(),
                dataTypeDao = database.dataTypeDao(),
                categoryAttributeDao = database.categoryAttributeDao()
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

        composable("userManagement") {
            val context = LocalContext.current
            val masterDb = DatabaseProvider.getMasterDatabase(context)
            // 画面表示時に毎回最新のアクティブDB情報を同期取得（remember による古い状態キャッシュを防止）
            val activeInfo = runBlocking { masterDb.databaseInfoDao().getActive() }

            val activeGroupId = when {
                activeInfo?.remoteGroupId != null -> activeInfo.remoteGroupId
                activeInfo?.displayName == "デフォルト" || activeInfo?.id == 1 -> 1
                else -> activeInfo?.id ?: 1
            }

            val userViewModel: UserManagementViewModel = viewModel(
                factory = UserManagementViewModelFactory(masterDb, NetworkModule.getApiService(), tokenManager)
            )
            UserManagementScreen(
                viewModel = userViewModel,
                groupId = activeGroupId,
                currentUserEmail = tokenManager.getEmail() ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
