package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.example.stockmanagement.data.entity.DataTypeEntity
import com.example.stockmanagement.data.model.AttributeWithDataType
import com.example.stockmanagement.viewmodel.AttributeViewModel


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AttributeListScreen(
    viewModel: AttributeViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val attributes by viewModel.attributes.collectAsState()
    val dataTypes by viewModel.dataTypes.collectAsState()
    var editingAttribute by remember {mutableStateOf<AttributeWithDataType?>(null)}
    var inputName by remember { mutableStateOf("") }
    var selectedDataTypeId by remember { mutableStateOf<Int?>(null) }
    var dataTypeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
    ) {
        Text("属性一覧")
        LazyColumn {
            items(attributes) { attribute ->
                Row {
                    Text(
                        text = "${attribute.name} (${attribute.dataTypeName})"
                    )
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

        if (editingAttribute != null) {
            AlertDialog(
                onDismissRequest = {
                    editingAttribute = null
                },
                title = {
                    Text("属性編集")
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = {
                                inputName = it
                            },
                            label = {
                                Text("属性")
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = dataTypeExpanded,
                            onExpandedChange = {
                                dataTypeExpanded = !dataTypeExpanded
                            }
                        ) {
                            OutlinedTextField(
                                value = dataTypes
                                    .find { it.dataTypeId == selectedDataTypeId }
                                    ?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text("データタイプ")
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = dataTypeExpanded
                                    )
                                },
                                modifier = Modifier.menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = dataTypeExpanded,
                                onDismissRequest = {
                                    dataTypeExpanded = false
                                }
                            ) {
                                dataTypes.forEach { dataType ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(dataType.name)
                                        },
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
                    Button(
                        onClick = {
                            selectedDataTypeId?.let { dataTypeId ->
                                viewModel.editAttribute(
                                    attribute = editingAttribute!!,
                                    newName = inputName,
                                    newDataTypeId = dataTypeId
                                )
                            }

                            editingAttribute = null
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
