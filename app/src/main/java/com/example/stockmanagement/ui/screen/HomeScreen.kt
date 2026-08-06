package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onManageMasterClick: () -> Unit,
    onManageDataClick: () -> Unit,
) {
    Column(
        modifier = Modifier
    ) {
        Button(
            onClick = onSearchClick
        ) {
            Text("Item検索")
        }

        Button(
            onClick = onManageMasterClick
        ) {
            Text("マスタ管理")
        }

        Button(
            onClick = onManageDataClick
        ) {
            Text("データ管理")
        }

    }
}