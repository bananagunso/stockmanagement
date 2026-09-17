package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: CategoryViewModel
) {
    val categories by viewModel.categories.collectAsState()
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("カテゴリ管理") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Button(
                                    onClick = {
                                        editingCategory = category
                                        inputName = category.name
                                    }
                                ) {
                                    Text("編集")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (editingCategory != null) {
            AlertDialog(
                onDismissRequest = { editingCategory = null },
                title = { Text("カテゴリ", softWrap = false) },
                modifier = Modifier.widthIn(min = 360.dp),
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("カテゴリ名") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Row {
                        TextButton(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("削除")
                        }
                        Button(
                            onClick = {
                                viewModel.editCategory(editingCategory!!, inputName)
                                editingCategory = null
                            }
                        ) {
                            Text("保存")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingCategory = null }) {
                        Text("キャンセル")
                    }
                }
            )
        }

        if (showDeleteDialog && editingCategory != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("カテゴリ削除", maxLines = 1) },
                text = { Text("「${editingCategory!!.name}」を削除しますか？\n関連する属性の紐付けも削除されます。") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(editingCategory!!.categoryId)
                            showDeleteDialog = false
                            editingCategory = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("削除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("キャンセル")
                    }
                }
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("カテゴリ", softWrap = false) },
                modifier = Modifier.widthIn(min = 360.dp),
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("カテゴリ名") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputName.isNotBlank()) {
                                viewModel.addCategory(inputName)
                                inputName = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("追加")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("キャンセル")
                    }
                }
            )
        }
    }
}
