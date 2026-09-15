package com.example.stockmanagement.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.stockmanagement.viewmodel.DataManagementViewModel

@Composable
fun ManageDataScreen(
    viewModel: DataManagementViewModel
) {
    val context = LocalContext.current
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImportUri by remember { mutableStateOf<Uri?>(null) }

    // --- DB操作用 Launcher ---
    val dbExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                viewModel.exportDatabase(context, outputStream)
            }
        }
    }

    val dbRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedRestoreUri = it
            showRestoreConfirm = true
        }
    }

    // --- CSV操作用 Launcher ---
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                viewModel.exportCsv(outputStream)
            }
        }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedImportUri = it
            showImportConfirm = true
        }
    }

    // イベント通知
    LaunchedEffect(Unit) {
        viewModel.event.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("データ管理", style = MaterialTheme.typography.headlineMedium)

        // CSVセクション
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CSV管理 (実用・一括更新)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Excel等で編集可能な形式でデータをやり取りします。既存データは上書き更新されます。",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { csvExportLauncher.launch("inventory_list.csv") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("在庫リストをCSVエクスポート")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { csvImportLauncher.launch(arrayOf("text/*", "application/octet-stream")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CSVからインポート・一括更新")
                }
            }
        }

        // DBバックアップセクション
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("DB完全バックアップ", style = MaterialTheme.typography.titleMedium)
                Text(
                    "システム全体の完全なコピーを作成します。機種変更時などに使用してください。",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { dbExportLauncher.launch("stock_management_backup.db") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DBファイルをエクスポート")
                }
            }
        }

        // リストア（危険）セクション
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("完全リストア (危険)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Text(
                    "注意：現在のすべてのデータが消去され、バックアップファイルの内容に置き換わります。",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { dbRestoreLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("DBファイルから全復元")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("すべてのデータを完全に消去（初期化）")
                }
            }
        }
    }

    // 全データ消去確認ダイアログ
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("データの全消去") },
            text = { Text("本当にすべてのデータを削除してアプリを初期状態に戻しますか？この操作は取り消せません。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("完全に消去") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("キャンセル") } }
        )
    }

    // CSVインポート確認ダイアログ
    if (showImportConfirm && selectedImportUri != null) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text("CSVインポートの確認") },
            text = { Text("選択したCSVからデータを読み込みます。同名のアイテムは上書き更新され、新しいアイテムは追加されます。よろしいですか？") },
            confirmButton = {
                Button(onClick = {
                    selectedImportUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.let { inputStream ->
                            viewModel.importCsv(inputStream)
                        }
                    }
                    showImportConfirm = false
                }) { Text("実行") }
            },
            dismissButton = { TextButton(onClick = { showImportConfirm = false }) { Text("キャンセル") } }
        )
    }

    // DBリストア確認ダイアログ
    if (showRestoreConfirm && selectedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("DB全復元の最終確認") },
            text = { Text("現在のすべてのデータが削除されます。本当に実行しますか？") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedRestoreUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                                viewModel.restoreDatabase(context, inputStream)
                            }
                        }
                        showRestoreConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("全削除して復元") }
            },
            dismissButton = { TextButton(onClick = { showRestoreConfirm = false }) { Text("キャンセル") } }
        )
    }
}


