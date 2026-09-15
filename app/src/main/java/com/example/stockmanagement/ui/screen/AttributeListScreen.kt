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
import com.example.stockmanagement.data.model.AttributeWithDataType
import com.example.stockmanagement.viewmodel.AttributeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeListScreen(
    viewModel: AttributeViewModel
) {
    val attributes by viewModel.attributes.collectAsState()
    val dataTypes by viewModel.dataTypes.collectAsState()
    var editingAttribute by remember { mutableStateOf<AttributeWithDataType?>(null) }
    var inputName by remember { mutableStateOf("") }
    var selectedDataTypeId by remember { mutableStateOf<Int?>(null) }
    var dataTypeExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("属性管理") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attributes) { attribute ->
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
                            Column {
                                Text(text = attribute.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "型: ${attribute.dataTypeName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Button(
                                onClick = {
                                    editingAttribute = attribute
                                    inputName = attribute.name
                                    selectedDataTypeId = attribute.dataTypeId
                                }
                            ) {
                                Text("編集")
                            }
                        }
                    }
                }
            }
        }

        if (editingAttribute != null) {
            AlertDialog(
                onDismissRequest = { editingAttribute = null },
                title = { Text("属性", softWrap = false) },
                modifier = Modifier.widthIn(min = 360.dp),
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("属性名") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = dataTypeExpanded,
                            onExpandedChange = { dataTypeExpanded = !dataTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = dataTypes.find { it.dataTypeId == selectedDataTypeId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("データ型") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dataTypeExpanded) },
                                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dataTypeExpanded,
                                onDismissRequest = { dataTypeExpanded = false }
                            ) {
                                dataTypes.forEach { dataType ->
                                    DropdownMenuItem(
                                        text = { Text(dataType.name) },
                                        onClick = {
                                            selectedDataTypeId = dataType.dataTypeId
                                            dataTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
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
                                selectedDataTypeId?.let { dataTypeId ->
                                    viewModel.editAttribute(editingAttribute!!, inputName, dataTypeId)
                                }
                                editingAttribute = null
                            }
                        ) { Text("保存") }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingAttribute = null }) {
                        Text("キャンセル")
                    }
                }
            )
        }

        if (showDeleteDialog && editingAttribute != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("属性削除", maxLines = 1) },
                text = { Text("「${editingAttribute!!.name}」を削除しますか？\n関連するカテゴリとの紐付けも削除されます。") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAttribute(editingAttribute!!.attributeId)
                            showDeleteDialog = false
                            editingAttribute = null
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
            AttributeAddDialog(
                dataTypes = dataTypes,
                onDismiss = { showDialog = false },
                onSave = { name, dataTypeId ->
                    viewModel.addAttribute(name, dataTypeId)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeAddDialog(
    dataTypes: List<DataTypeEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var selectedDataType by remember { mutableStateOf<DataTypeEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("属性", softWrap = false) },
        modifier = Modifier.widthIn(min = 360.dp),
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("属性名") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedDataType?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("データ型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        dataTypes.forEach { dataType ->
                            DropdownMenuItem(
                                text = { Text(dataType.name) },
                                onClick = {
                                    selectedDataType = dataType
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedDataType?.let { onSave(name, it.dataTypeId) } }
            ) { Text("追加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}
