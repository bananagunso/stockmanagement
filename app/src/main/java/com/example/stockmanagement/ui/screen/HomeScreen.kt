package com.example.stockmanagement.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stockmanagement.data.database.DatabaseProvider
import com.example.stockmanagement.viewmodel.DatabaseSettingsViewModel
import com.example.stockmanagement.viewmodel.SyncViewModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    syncViewModel: SyncViewModel? = null,
    dbViewModel: DatabaseSettingsViewModel? = null,
    userEmail: String = "",
    userRole: String = "manager",
    onSearchItemClick: () -> Unit,
    onManageMasterClick: () -> Unit,
    onManageDataClick: () -> Unit,
    onSwitchDatabaseClick: () -> Unit,
    onManageUserClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isSyncing by syncViewModel?.isSyncing?.collectAsState() ?: remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    val masterDb = remember { DatabaseProvider.getMasterDatabase(context) }
    val initialActive = remember { runBlocking { masterDb.databaseInfoDao().getActive() } }
    val activeInfoState = masterDb.databaseInfoDao().getActiveFlow().collectAsState(initial = initialActive)
    val activeInfo = activeInfoState.value
    val hasActiveDb = activeInfo != null

    var showAutoCreateDialog by remember { mutableStateOf(false) }
    var autoDbName by remember { mutableStateOf("") }
    var hasCheckedGroups by remember { mutableStateOf(false) }

    // 画面初回表示時のみサーバー同期とDB存在チェックを実行（直列同期完了を待機）
    LaunchedEffect(Unit) {
        if (dbViewModel != null && !hasCheckedGroups && !isLoggingOut) {
            hasCheckedGroups = true
            val currentList = dbViewModel.refreshGroups()
            if (currentList.isEmpty()) {
                showAutoCreateDialog = true
            }
        }
    }

    LaunchedEffect(syncViewModel) {
        syncViewModel?.syncEvent?.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("在庫管理") },
                actions = {
                    TextButton(
                        onClick = { showLogoutDialog = true },
                        enabled = !isSyncing && !isLoggingOut
                    ) {
                        Text("ログアウト")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp, horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                if (!hasActiveDb && !isSyncing && !showLogoutDialog && !isLoggingOut) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Text(
                            text = "⚠️ データベースが作成されていません。\n「データベース切り替え」またはダイアログから作成してください。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                val menuItems = mutableListOf<Pair<String, () -> Unit>>(
                    "Item検索" to onSearchItemClick,
                    "マスタ管理" to onManageMasterClick,
                    "データ同期" to { syncViewModel?.syncData() },
                    "データベース管理" to onManageDataClick,
                    "データベース切り替え" to onSwitchDatabaseClick
                )

                // 管理者（manager）または全体管理者（system_admin）のみ「ユーザー管理」を表示
                if (userRole == "manager" || userRole == "system_admin") {
                    menuItems.add(3, "ユーザー管理" to onManageUserClick)
                }

                menuItems.forEach { (label, onClick) ->
                    val isDbSwitch = label == "データベース切り替え"
                    val isEnabled = !isSyncing && !isLoggingOut && (isDbSwitch || hasActiveDb)

                    Card(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(72.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 2.dp else 0.dp),
                        enabled = isEnabled
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (label == "データ同期" && isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAutoCreateDialog && !isLoggingOut) {
        AlertDialog(
            onDismissRequest = { showAutoCreateDialog = false },
            title = { Text("データベースの作成") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("現在利用可能なデータベースが存在しません。\n最初のデータベースを作成してください。")
                    OutlinedTextField(
                        value = autoDbName,
                        onValueChange = { autoDbName = it },
                        label = { Text("データベース名（例: メイン在庫）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (autoDbName.isNotBlank() && dbViewModel != null) {
                            dbViewModel.createDatabase(autoDbName)
                            showAutoCreateDialog = false
                        }
                    }
                ) { Text("作成") }
            },
            dismissButton = {
                TextButton(onClick = { showAutoCreateDialog = false }) {
                    Text("後で")
                }
            }
        )
    }

    if (showLogoutDialog) {
        val logoutText = if (userEmail.isNotBlank()) {
            "${userEmail}\nからログアウトしますか？\n\n未同期データは自動同期された後、安全のために端末内データが初期化されます。"
        } else {
            "ログアウトしますか？\n未同期データは自動同期された後、安全のために端末内データが初期化されます。"
        }

        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("ログアウト確認") },
            text = { Text(logoutText) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        isLoggingOut = true
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ログアウト")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
