package com.example.stockmanagement.ui.screen

import AttributeDropdown
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
import androidx.compose.ui.window.Dialog
import com.example.stockmanagement.data.entity.AttributeEntity
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.data.model.CategoryWithAttribute
import com.example.stockmanagement.viewmodel.CategoryAttributeViewModel
import com.example.stockmanagement.ui.component.CategoryDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAttributeListScreen(
    viewModel: CategoryAttributeViewModel
) {
    val categoryAttributes by viewModel.categoryAttributes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val attributes by viewModel.attributes.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var editingCategoryAttribute by remember { mutableStateOf<CategoryWithAttribute?>(null) }
    var inputUnit by remember { mutableStateOf("") }
    var selectedCategoryForEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedAttributeForEdit by remember { mutableStateOf<AttributeEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("紐付け管理 (カテゴリ・属性)") }) },
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
                items(categoryAttributes) { categoryAttribute ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = categoryAttribute.categoryName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = categoryAttribute.attributeName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (!categoryAttribute.unit.isNullOrEmpty()) {
                                    Text(
                                        text = "単位: ${categoryAttribute.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    editingCategoryAttribute = categoryAttribute
                                    inputUnit = categoryAttribute.unit ?: ""
                                    selectedCategoryForEdit = categories.find { it.name == categoryAttribute.categoryName }
                                    selectedAttributeForEdit = attributes.find { it.name == categoryAttribute.attributeName }
                                }
                            ) {
                                Text("編集")
                            }
                        }
                    }
                }
            }
        }

        if (editingCategoryAttribute != null) {
            Dialog(onDismissRequest = { editingCategoryAttribute = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // タイトルエリア（余白を詰める）
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 12.dp)
                        ) {
                            Text(
                                text = "カテゴリ・属性紐付け",
                                style = MaterialTheme.typography.headlineSmall,
                                softWrap = false
                            )
                        }

                        HorizontalDivider()

                        // スクロールエリア（weightを使って画面内に収める）
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(scrollState)
                                .padding(vertical = 12.dp)
                        ) {
                            CategoryDropdown(
                                categories = categories,
                                selected = selectedCategoryForEdit,
                                onSelected = { selectedCategoryForEdit = it }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            AttributeDropdown(
                                attributes = attributes,
                                selected = selectedAttributeForEdit,
                                onSelected = { selectedAttributeForEdit = it }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = inputUnit,
                                onValueChange = { inputUnit = it },
                                label = { Text("単位") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider()

                        // ボタンエリア（余白を詰める）
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { editingCategoryAttribute = null }) {
                                Text("キャンセル")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("削除")
                            }
                            Button(
                                onClick = {
                                    val cat = selectedCategoryForEdit
                                    val attr = selectedAttributeForEdit
                                    if (cat != null && attr != null) {
                                        viewModel.editCategoryAttribute(
                                            categoryAttribute = editingCategoryAttribute!!,
                                            newCategoryId = cat.categoryId,
                                            newAttributeId = attr.attributeId,
                                            newUnit = inputUnit
                                        )
                                        editingCategoryAttribute = null
                                    }
                                }
                            ) {
                                Text("保存")
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog && editingCategoryAttribute != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("紐付け削除", softWrap = false) },
                text = { Text("「${editingCategoryAttribute!!.categoryName} - ${editingCategoryAttribute!!.attributeName}」の関連を削除しますか？") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategoryAttribute(editingCategoryAttribute!!.categoryAttributeId)
                            showDeleteDialog = false
                            editingCategoryAttribute = null
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
            CategoryAttributeAddDialog(
                categories = categories,
                attributes = attributes,
                onDismiss = { showDialog = false },
                onSave = { categoryId, attributeId, unit ->
                    viewModel.addCategoryAttribute(categoryId, attributeId, unit)
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
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedAttribute by remember { mutableStateOf<AttributeEntity?>(null) }
    var unit by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // タイトルエリア
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "カテゴリ・属性紐付け",
                        style = MaterialTheme.typography.headlineSmall,
                        softWrap = false
                    )
                }

                HorizontalDivider()

                // スクロールエリア
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                        .padding(vertical = 12.dp)
                ) {
                    CategoryDropdown(
                        categories = categories,
                        selected = selectedCategory,
                        onSelected = { selectedCategory = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AttributeDropdown(
                        attributes = attributes,
                        selected = selectedAttribute,
                        onSelected = { selectedAttribute = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("単位") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()

                // ボタンエリア
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("キャンセル")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val category = selectedCategory
                            val attribute = selectedAttribute
                            if (category != null && attribute != null) {
                                onSave(category.categoryId, attribute.attributeId, unit)
                            }
                        }
                    ) {
                        Text("追加")
                    }
                }
            }
        }
    }
}
