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
import com.example.stockmanagement.data.entity.DataTypeEntity
import com.example.stockmanagement.viewmodel.DataTypeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataTypeListScreen(
    viewModel: DataTypeViewModel
) {
    val dataTypes by viewModel.dataTypes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingDataType by remember { mutableStateOf<DataTypeEntity?>(null) }
    var inputName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("データタイプ管理") }) },
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
                    items(dataTypes) { dataType ->
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
                                Text(text = dataType.name, style = MaterialTheme.typography.bodyLarge)
                                Button(
                                    onClick = {
                                        editingDataType = dataType
                                        inputName = dataType.name
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

        if (editingDataType != null) {
            AlertDialog(
                onDismissRequest = { editingDataType = null },
                title = { Text("データタイプ", softWrap = false) },
                modifier = Modifier.widthIn(min = 360.dp),
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("名前") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Row {
                        TextButton(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("削除") }
                        Button(
                            onClick = {
                                viewModel.editDataType(editingDataType!!, inputName)
                                editingDataType = null
                            }
                        ) { Text("保存") }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingDataType = null }) {
                        Text("キャンセル")
                    }
                }
            )
        }

        if (showDeleteDialog && editingDataType != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("データタイプ削除", maxLines = 1) },
                text = { Text("「${editingDataType!!.name}」を削除しますか？") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteDataType(editingDataType!!.dataTypeId)
                            showDeleteDialog = false
                            editingDataType = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("削除") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("キャンセル") }
                }
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("データタイプ", softWrap = false) },
                modifier = Modifier.widthIn(min = 360.dp),
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("名前") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputName.isNotBlank()) {
                                viewModel.addDataType(inputName)
                                inputName = ""
                                showDialog = false
                            }
                        }
                    ) { Text("追加") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("キャンセル") }
                }
            )
        }
    }
}
