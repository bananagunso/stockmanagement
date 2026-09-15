package com.example.stockmanagement.ui.component

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.stockmanagement.data.entity.CategoryEntity
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<CategoryEntity>,
    selected: CategoryEntity?,
    onSelected: (CategoryEntity?) -> Unit,
    showUnselected: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selected?.name ?: if (showUnselected) "未選択" else "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text("カテゴリ")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.menuAnchor(
                ExposedDropdownMenuAnchorType.PrimaryNotEditable
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            if (showUnselected) {
                DropdownMenuItem(
                    text = {
                        Text("未選択")
                    },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    }
                )
            }
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(category.name)
                    },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
