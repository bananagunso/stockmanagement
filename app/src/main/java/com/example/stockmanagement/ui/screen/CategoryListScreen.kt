package com.example.stockmanagement.ui.screen

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.stockmanagement.ui.viewmodel.CategoryViewModel
import androidx.compose.material3.Button
@Composable
fun CategoryListScreen(
    viewModel: CategoryViewModel
) {
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }

    Text("カテゴリ一覧")
    LazyColumn {
        items(categories) { category ->
            Text(
                text = category.name
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
                Text("カテゴリ追加")
            },
            text = {
                TextField(
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
                        if (inputName.isNotBlank()) {
                            viewModel.addCategory(inputName)
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
