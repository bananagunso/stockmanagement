package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
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

@Composable
fun DataTypeListScreen(
    viewModel: DataTypeViewModel
) {
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
    ) {
        Text("データタイプ一覧")
        LazyColumn {
            items(categories) { data_type ->
                Text(
                    text = data_type.name
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
