package com.example.stockmanagement.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.data.model.ItemWithDetails
import com.example.stockmanagement.viewmodel.ItemViewModel
import com.example.stockmanagement.ui.component.CategoryDropdown

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SearchItemScreen(
    viewModel: ItemViewModel,
    categories: List<CategoryEntity>,
    onItemClick: (Int) -> Unit,
) {
    val itemWithCategory by viewModel.itemWithCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputStock by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf<CategoryEntity?>(null)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // 横画面：左に検索・追加、右にリスト
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 左パネル (検索コントロール)
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Item検索",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        label = { Text("型番・スペック検索") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("カテゴリ絞り込み:", style = MaterialTheme.typography.bodySmall)
                        CategoryDropdown(
                            categories = categories,
                            selected = categories.find { it.categoryId == selectedCategoryId },
                            onSelected = { viewModel.setCategoryFilter(it?.categoryId) },
                            showUnselected = true
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("+ 新規追加", fontSize = 18.sp)
                    }
                }

                // 右パネル (リスト表示)
                Box(modifier = Modifier.weight(1f)) {
                    ItemList(
                        items = itemWithCategory,
                        onItemClick = onItemClick
                    )
                }
            }
        } else {
            // 縦画面 (従来通り)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Item検索画面",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    label = { Text("型番・スペック検索") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("カテゴリ: ", style = MaterialTheme.typography.bodyMedium)
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryDropdown(
                            categories = categories,
                            selected = categories.find { it.categoryId == selectedCategoryId },
                            onSelected = { viewModel.setCategoryFilter(it?.categoryId) },
                            showUnselected = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    ItemList(
                        items = itemWithCategory,
                        onItemClick = onItemClick
                    )
                }

                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                ) {
                    Text("+", fontSize = 24.sp)
                }
            }
        }
    }

    // ... (Dialog implementation)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text("アイテム", softWrap = false)
            },
            modifier = Modifier.widthIn(min = 360.dp),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                    Text("追加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
fun ItemList(
    items: List<ItemWithDetails>,
    onItemClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            ItemRow(
                item = item,
                onEditClick = { onItemClick(item.itemId) }
            )
        }
    }
}

@Composable
fun ItemRow(
    item: ItemWithDetails,
    onEditClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    // 簡易スペック表示 (要約)
                    if (!expanded && item.attributeDetails.isNotEmpty()) {
                        val summary = item.attributeDetails.joinToString(" / ") { 
                            "${it.value}${it.unit ?: ""}" 
                        }
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Text(
                    text = "在庫: ${item.stock}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.stock > 0) MaterialTheme.colorScheme.onSurface else Color.Red,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("編集", fontSize = 12.sp)
                }
            }

            // アコーディオン展開部分 (属性詳細)
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    item.attributeDetails.forEach { attr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = attr.attributeName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${attr.value}${attr.unit ?: ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
