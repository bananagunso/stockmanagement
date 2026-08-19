package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ManageMasterScreen(
    onCategoryListClick: () -> Unit,
    onAttributeListClick: () -> Unit,
    onCategoryAttributeListClick: () -> Unit,
    onDataTypeListClick: () -> Unit,
) {
    Column(
        modifier = Modifier
    ) {
        Text("マスタ管理画面")
        Button(
            onClick = onCategoryListClick
        ) {
            Text("カテゴリ管理")
        }
        Button(
            onClick = onAttributeListClick
        ) {
            Text("属性管理")
        }
        Button(
            onClick = onCategoryAttributeListClick
        ) {
            Text("カテゴリ・属性管理")
        }
        Button(
            onClick = onDataTypeListClick
        ) {
            Text("データタイプ管理")
        }
    }
}