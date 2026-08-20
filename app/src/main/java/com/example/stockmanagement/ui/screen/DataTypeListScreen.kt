package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.stockmanagement.viewmodel.DataTypeViewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.stockmanagement.data.entity.DataTypeEntity

@Composable
fun DataTypeListScreen(
    viewModel: DataTypeViewModel
) {
    val dataTypes by viewModel.dataTypes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingDataType by remember { mutableStateOf<DataTypeEntity?>(null) }
    var inputName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
    ) {
        Text("データタイプ一覧")
        LazyColumn {
            items(dataTypes) { dataType ->
                Row {
                    Text(
                        text = dataType.name
                    )
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

        if (editingDataType != null) {
            AlertDialog(
                onDismissRequest = {
                    editingDataType = null
                },
                title = {
                    Text("カテゴリ編集")
                },
                text = {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = {
                            inputName = it
                        },
                        label = {
                            Text("カテゴリ名")
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.editDataType(
                                dataType = editingDataType!!,
                                newName = inputName
                            )
                            editingDataType = null
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }


        FloatingActionButton(
            onClick = {
                showDialog = true
            }
        ) {
            Text("+")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = {
                    Text("データタイプ追加")
                },
                text = {
                    TextField(
                        value = inputName,
                        onValueChange = {
                            inputName = it
                        },
                        label = {
                            Text("データタイプ")
                        }
                    )
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
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    }
}
