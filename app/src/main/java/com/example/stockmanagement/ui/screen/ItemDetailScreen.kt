package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.data.entity.ItemEntity
import com.example.stockmanagement.ui.component.CategoryDropdown
import com.example.stockmanagement.viewmodel.CategoryAttributeViewModel
import com.example.stockmanagement.viewmodel.ItemAttributeValueViewModel
import com.example.stockmanagement.viewmodel.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    itemViewModel: ItemViewModel,
    attributeValueViewModel: ItemAttributeValueViewModel,
    categoryAttributeViewModel: CategoryAttributeViewModel,
    categories: List<CategoryEntity>,
    onBack: () -> Unit
) {
    val items by itemViewModel.items.collectAsState()
    val item = items.find { it.itemId == itemId } ?: return

    val categoryAttributes by categoryAttributeViewModel.categoryAttributes.collectAsState()
    val itemValues by attributeValueViewModel.getItemValues(itemId).collectAsState(initial = emptyList())

    // 編集用状態
    var inputName by remember(item) { mutableStateOf(item.name) }
    var inputStock by remember(item) { mutableStateOf(item.stock.toString()) }
    var selectedCategory by remember(item) {
        mutableStateOf(categories.find { it.categoryId == item.categoryId })
    }

    // 属性値の入力状態（categoryAttributeId -> value）
    val attributeInputs = remember(itemValues, categoryAttributes) {
        mutableStateMapOf<Int, String>().apply {
            itemValues.forEach { put(it.categoryattributeId, it.value) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item詳細・管理") },
                navigationIcon = {
                    Button(onClick = onBack) { Text("戻る") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {
                // 基本情報セクション
                Text("基本情報", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("アイテム名") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                CategoryDropdown(
                    categories = categories,
                    selected = selectedCategory,
                    onSelected = { selectedCategory = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("在庫数: ", modifier = Modifier.width(80.dp))
                    Button(onClick = {
                        val current = inputStock.toIntOrNull() ?: 0
                        if (current > 0) inputStock = (current - 1).toString()
                    }) { Text("-") }
                    TextField(
                        value = inputStock,
                        onValueChange = { if (it.all { c -> c.isDigit() }) inputStock = it },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(onClick = {
                        val current = inputStock.toIntOrNull() ?: 0
                        inputStock = (current + 1).toString()
                    }) { Text("+") }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 属性値セクション
                Text("スペック詳細 (属性)", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            val relevantAttributes = categoryAttributes.filter { it.categoryName == selectedCategory?.name }

            items(relevantAttributes) { attr ->
                val currentValue = attributeInputs[attr.categoryAttributeId] ?: ""
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${attr.attributeName}: ",
                        modifier = Modifier.width(120.dp)
                    )
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = { attributeInputs[attr.categoryAttributeId] = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("値を入力") },
                        trailingIcon = {
                            if (!attr.unit.isNullOrEmpty()) {
                                Text(attr.unit, modifier = Modifier.padding(end = 8.dp))
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // 基本情報の保存
                        val stock = inputStock.toIntOrNull() ?: item.stock
                        selectedCategory?.let { cat ->
                            itemViewModel.editItem(itemId, inputName, cat.categoryId, stock)
                        }

                        // 各属性値の保存
                        attributeInputs.forEach { (attrId, value) ->
                            attributeValueViewModel.saveAttributeValue(itemId, attrId, value)
                        }
                        
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("すべて保存")
                }
            }
        }
    }
}
