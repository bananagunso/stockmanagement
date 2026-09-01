package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.viewmodel.ItemViewModel
import com.example.stockmanagement.ui.component.CategoryDropdown

@Composable
fun SearchItemScreen(
    viewModel: ItemViewModel,
    categories: List<CategoryEntity>,

    ) {
    var showDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputStock by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf<CategoryEntity?>(null)
    }

    Column(
        modifier = Modifier
    ) {
        Text("Item検索画面")

        FloatingActionButton(
            onClick = {
                showDialog = true
            }
        ) {
            Text("+")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text("Item追加")
            },
            text = {
                Column {
                    CategoryDropdown(
                        categories = categories,
                        selected = selectedCategory,
                        onSelected = {
                            selectedCategory = it
                        }
                    )

                    TextField(
                        value = inputName,
                        onValueChange = {
                            inputName = it
                        },
                        label = {
                            Text("Item名")
                        }
                    )
                    TextField(
                        value = inputStock,
                        onValueChange = {
                            inputStock = it
                        },
                        label = {
                            Text("在庫数")
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val category = selectedCategory
                        val stock = inputStock.toIntOrNull()

                        if (category != null &&
                            inputName.isNotBlank() &&
                            stock != null
                        ) {
                            viewModel.addItem(
                                inputName,
                                stock,
                                category.categoryId
                            )

                            inputName = ""
                            inputStock = ""
                            selectedCategory = null
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
