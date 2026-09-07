package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.data.model.ItemWithCategory
import com.example.stockmanagement.viewmodel.ItemViewModel
import com.example.stockmanagement.ui.component.CategoryDropdown

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SearchItemScreen(
    viewModel: ItemViewModel,
    categories: List<CategoryEntity>,

    ) {
    val itemWithCategory by viewModel.itemWithCategory.collectAsState()
    var editingItem by remember { mutableStateOf<ItemWithCategory?>(null) }
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
        LazyColumn {
            items(itemWithCategory) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${item.categoryName} ${item.itemName} 在庫${item.stock}",
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            editingItem = item
                            inputName = item.itemName
                            selectedCategory =
                                categories.find { it.categoryId == item.categoryId }
                        }
                    ) {
                        Text("編集")
                    }
                }
            }
        }

        if (editingItem != null) {
            AlertDialog(
                onDismissRequest = {
                    editingItem = null
                },
                title = {
                    Text("item編集")
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
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = {
                                inputName = it
                            },
                            label = {
                                Text("item")
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val category = selectedCategory

                            editingItem?.let { item ->
                                if (category != null && inputName.isNotBlank()) {
                                    viewModel.editItem(
                                        itemId = item.itemId,
                                        newName = inputName,
                                        newCategoryId = category.categoryId
                                    )

                                    editingItem = null
                                }
                            }
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
