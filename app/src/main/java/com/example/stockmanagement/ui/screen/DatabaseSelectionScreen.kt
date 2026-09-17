package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockmanagement.data.entity.DatabaseInfoEntity
import com.example.stockmanagement.viewmodel.DatabaseSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseSelectionScreen(
    viewModel: DatabaseSettingsViewModel,
    onDatabaseSwitched: () -> Unit,
    onBack: () -> Unit
) {
    val databaseList by viewModel.databaseList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var editingDb by remember { mutableStateOf<DatabaseInfoEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<DatabaseInfoEntity?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("データベース切り替え") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("戻る") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                inputName = ""
                showAddDialog = true 
            }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(databaseList) { info ->
                    DatabaseItemRow(
                        info = info,
                        onSelect = { 
                            viewModel.switchDatabase(info.id)
                            onDatabaseSwitched()
                        },
                        onEdit = {
                            editingDb = info
                            inputName = info.displayName
                        },
                        onDelete = { showDeleteConfirm = info }
                    )
                }
            }
        }
    }

    // 作成・名前変更ダイアログ
    if (showAddDialog || editingDb != null) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                editingDb = null
            },
            title = { Text(if (editingDb != null) "名前の変更" else "新規データベース作成") },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("データベース名") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (inputName.isNotBlank()) {
                        if (editingDb != null) {
                            viewModel.renameDatabase(editingDb!!, inputName)
                        } else {
                            viewModel.createDatabase(inputName)
                        }
                        showAddDialog = false
                        editingDb = null
                    }
                }) {
                    Text(if (editingDb != null) "保存" else "作成")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    editingDb = null
                }) { Text("キャンセル") }
            }
        )
    }

    // 削除確認
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("データベースの削除") },
            text = { Text("「${showDeleteConfirm!!.displayName}」を削除しますか？\n中身のデータもすべて完全に消去されます。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDatabase(context, info = showDeleteConfirm!!)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("削除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("キャンセル") }
            }
        )
    }
}

@Composable
fun DatabaseItemRow(
    info: DatabaseInfoEntity,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (info.isActive) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (info.isActive) if (info.isActive) 4.dp else 1.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.displayName,
                    style = MaterialTheme.typography.titleLarge
                )
                if (info.isActive) {
                    Text(
                        text = "現在使用中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            OutlinedButton(
                onClick = onEdit,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("編集", fontSize = 12.sp)
            }
            
            if (!info.isActive) {
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("削除", fontSize = 12.sp)
                }
                Button(
                    onClick = onSelect,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("切替", fontSize = 12.sp)
                }
            }
        }
    }
}
