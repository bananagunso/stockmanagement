package com.example.stockmanagement.ui.screen

import AttributeDropdown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.stockmanagement.data.entity.AttributeEntity
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.data.model.CategoryWithAttribute
import com.example.stockmanagement.viewmodel.CategoryAttributeViewModel
import com.example.stockmanagement.ui.component.CategoryDropdown

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryAttributeListScreen(
    viewModel: CategoryAttributeViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val categoryAttributes by viewModel.categoryAttributes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val attributes by viewModel.attributes.collectAsState()
    var editingCategoryAttribute by remember {mutableStateOf<CategoryWithAttribute?>(null)}
    var inputUnit by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
    ) {
        Text("カテゴリ・属性一覧")


        LazyColumn {
            items(categoryAttributes) { categoryAttribute ->
                Row {
                    Text(
                        text = "${categoryAttribute.categoryAttributeId} " +
                                "${categoryAttribute.categoryName} " +
                                "${categoryAttribute.attributeName} " +
                                (categoryAttribute.unit ?: "")
                    )
                    Button(
                        onClick = {
                            editingCategoryAttribute = categoryAttribute
                            inputUnit = categoryAttribute.unit ?: ""
                        }
                    ) {
                        Text("編集")
                    }
                }
            }
        }

        if (editingCategoryAttribute != null) {
            AlertDialog(
                onDismissRequest = {
                    editingCategoryAttribute = null
                },
                title = {
                    Text("カテゴリ・属性単位編集")
                },
                text = {
                    OutlinedTextField(
                        value = inputUnit,
                        onValueChange = {
                            inputUnit = it
                        },
                        label = {
                            Text("単位")
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.editCategoryAttribute(
                                categoryAttribute = editingCategoryAttribute!!,
                                newUnit = inputUnit
                            )
                            editingCategoryAttribute = null
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
            CategoryAttributeAddDialog(
                categories = categories,
                attributes = attributes,
                onDismiss = {
                    showDialog = false
                },
                onSave = { categoryId, attributeId, unit  ->

                    viewModel.addCategoryAttribute(
                        categoryId = categoryId,
                        attributeId = attributeId,
                        unit = unit
                    )

                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun CategoryAttributeAddDialog(
    categories: List<CategoryEntity>,
    attributes: List<AttributeEntity>,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String) -> Unit
) {
    var selectedCategory by remember {
        mutableStateOf<CategoryEntity?>(null)
    }
    var selectedAttribute by remember {
        mutableStateOf<AttributeEntity?>(null)
    }
    var unit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("カテゴリ・属性追加")
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

                AttributeDropdown(
                    attributes = attributes,
                    selected = selectedAttribute,
                    onSelected = {
                        selectedAttribute = it
                    }
                )

                TextField(
                    value = unit,
                    onValueChange = {
                        unit = it
                    },
                    label = {
                        Text("単位")
                    }
                )
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    val category = selectedCategory
                    val attribute = selectedAttribute

                    if (category != null && attribute != null) {
                        onSave(
                            category.categoryId,
                            attribute.attributeId,
                            unit
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
