package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.stockmanagement.viewmodel.CategoryViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.stockmanagement.data.entity.CategoryEntity

@Composable
fun CategoryListScreen(
    viewModel: CategoryViewModel
) {
    val categories by viewModel.categories.collectAsState()
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
    ) {
        Text("カテゴリ一覧")
        LazyColumn {
            items(categories) { category ->
                Row {
                    Text(
                        text = category.name
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

        if (editingCategory != null) {
            AlertDialog(
                onDismissRequest = {
                    editingCategory = null
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
                            showDeleteDialog = true
                        }
                    ) {
                        Text("削除")
                    }
                    Button(
                        onClick = {
                            viewModel.editCategory(
                                category = editingCategory!!,
                                newName = inputName
                            )
                            editingCategory = null
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }

        if (showDeleteDialog && editingCategory != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                },
                title = {
                    Text("カテゴリ削除")
                },
                text = {
                    Text(
                        "「${editingCategory!!.name}」を削除しますか？\n" +
                                "このカテゴリに設定されている属性との関連も削除されます。"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(
                                editingCategory!!.categoryId
                            )
                            showDeleteDialog = false
                            editingCategory = null
                            inputName = ""
                        }
                    ) {
                        Text("削除")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                        }
                    ) {
                        Text("キャンセル")
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
}
