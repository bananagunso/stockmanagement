package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.stockmanagement.data.entity.DataTypeEntity
import com.example.stockmanagement.viewmodel.AttributeViewModel


@Composable
fun AttributeListScreen(
    viewModel: AttributeViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val attributes by viewModel.attributes.collectAsState()
    val dataTypes by viewModel.dataTypes.collectAsState()

    Column(
        modifier = Modifier
    ) {
        Text("属性一覧")
        LazyColumn {
            items(attributes) { attribute ->
                Text(
                    text = "${attribute.name} (${attribute.dataTypeName})"
                )
            }
        }
        FloatingActionButton(
            onClick = {
                showDialog = true
            }
        ) {
            Text("+")
        }

        if (showDialog) {
            AttributeAddDialog(
                dataTypes = dataTypes,
                onDismiss = {
                    showDialog = false
                },
                onSave = { name, dataTypeId ->

                    viewModel.addAttribute(
                        name,
                        dataTypeId
                    )

                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AttributeAddDialog(
    dataTypes: List<DataTypeEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var selectedDataType by remember {
        mutableStateOf<DataTypeEntity?>(null)
    }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("属性追加")
        },

        text = {
            Column {

                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("属性名")
                    }
                )

                DataTypeDropdown(
                    dataTypes = dataTypes,
                    selected = selectedDataType,
                    onSelected = {
                        selectedDataType = it
                    }
                )
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    selectedDataType?.let {
                        onSave(
                            name,
                            it.dataTypeId
                        )
                    }
                }
            ) {
                Text("保存")
            }
        },

        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("キャンセル")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataTypeDropdown(
    dataTypes: List<DataTypeEntity>,
    selected: DataTypeEntity?,
    onSelected: (DataTypeEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text("データ型")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.menuAnchor(
                ExposedDropdownMenuAnchorType.PrimaryNotEditable
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            dataTypes.forEach { dataType ->
                DropdownMenuItem(
                    text = {
                        Text(dataType.name)
                    },
                    onClick = {
                        onSelected(dataType)
                        expanded = false
                    }
                )
            }
        }
    }
}
