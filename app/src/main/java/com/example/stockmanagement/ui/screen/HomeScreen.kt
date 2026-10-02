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
import com.example.stockmanagement.viewmodel.SyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    syncViewModel: SyncViewModel? = null,
    userEmail: String = "",
    onSearchItemClick: () -> Unit,
    onManageMasterClick: () -> Unit,
    onManageDataClick: () -> Unit,
    onSwitchDatabaseClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isSyncing by syncViewModel?.isSyncing?.collectAsState() ?: remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                        enabled = !isSyncing
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
                    .padding(vertical = 16.dp, horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                val menuItems: List<Pair<String, () -> Unit>> = listOf(
                    "Item検索" to onSearchItemClick,
                    "マスタ管理" to onManageMasterClick,
                    "データ同期" to { syncViewModel?.syncData() },
                    "データベース管理" to onManageDataClick,
                    "データベース切り替え" to onSwitchDatabaseClick
                )

                menuItems.forEach { (label, onClick) ->
                    Card(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(108.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        enabled = !isSyncing
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (label == "データ同期" && isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            } else {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
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
