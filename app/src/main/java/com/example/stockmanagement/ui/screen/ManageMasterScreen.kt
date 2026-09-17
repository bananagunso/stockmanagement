package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageMasterScreen(
    onCategoryListClick: () -> Unit,
    onAttributeListClick: () -> Unit,
    onCategoryAttributeListClick: () -> Unit,
    onDataTypeListClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // ステータスバーとの重なりを回避
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "マスタ管理",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 2
            ) {
                val menuItems = listOf(
                    "カテゴリ管理" to onCategoryListClick,
                    "属性管理" to onAttributeListClick,
                    "紐付け管理\n(カテゴリ・属性)" to onCategoryAttributeListClick,
                    "データタイプ管理" to onDataTypeListClick
                )

                menuItems.forEach { (label, onClick) ->
                    ElevatedCard(
                        onClick = onClick,
                        modifier = Modifier
                            .widthIn(min = 160.dp, max = 240.dp)
                            .height(100.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
